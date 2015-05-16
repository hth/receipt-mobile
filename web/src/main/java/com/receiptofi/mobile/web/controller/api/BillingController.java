package com.receiptofi.mobile.web.controller.api;

import com.google.gson.JsonObject;

import com.receiptofi.domain.types.PaymentGatewayEnum;
import com.receiptofi.mobile.domain.ReceiptofiPlan;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.BillingMobileService;
import com.receiptofi.mobile.service.DeviceService;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 5/9/15 6:12 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api")
public class BillingController {
    private static final Logger LOG = LoggerFactory.getLogger(BillingController.class);

    private DeviceService deviceService;
    private AuthenticateService authenticateService;
    private BillingMobileService billingMobileService;

    @Autowired
    public BillingController(
            DeviceService deviceService,
            AuthenticateService authenticateService,
            BillingMobileService billingMobileService
    ) {
        this.deviceService = deviceService;
        this.authenticateService = authenticateService;
        this.billingMobileService = billingMobileService;
    }

    /**
     * Get all available plans.
     *
     * @param mail
     * @param auth
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/plans",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<ReceiptofiPlan> getPlans(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            LOG.info("Plans fetched for rid={} did={}", rid);
            return billingMobileService.getAllPlans();
        }
    }

    /**
     * Generate BrainTree client token for mobile payments. This initializes Braintree in mobile device.
     * Should obtain a new client token often, at least as often as your app restarts. For the best experience,
     * you should kick off this network operation before it would block a user interaction. Preferably when
     * user its a payment screen.
     * <p>
     * You must generate a client token on your server once per user checkout session.
     *
     * @param mail
     * @param auth
     * @param did
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/token",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String brainTreeClientToken(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestHeader ("X-R-DID")
            String did,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            if (deviceService.isDeviceRegistered(rid, did)) {
                LOG.info("Generating client token for rid={} did={}", rid, did);
                return billingMobileService.getBrianTreeClientToken(rid);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
                return null;
            }
        }
    }

    /**
     * Submit payment.
     *
     * @param mail
     * @param auth
     * @param did
     * @param requestBodyJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/payment",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String brainTreePayment(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestHeader ("X-R-DID")
            String did,

            @RequestBody
            String requestBodyJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            if (deviceService.isDeviceRegistered(rid, did)) {
                LOG.info("Submitting payment for rid={} did={}", rid, did);

                Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
                String planId = map.containsKey("planId") ? map.get("planId").getText() : null;
                String firstName = map.containsKey("firstName") ? map.get("firstName").getText() : null;
                String lastName = map.containsKey("lastName") ? map.get("lastName").getText() : null;
                String cardNumber = map.containsKey("cardNumber") ? map.get("cardNumber").getText() : null;
                String month = map.containsKey("month") ? map.get("month").getText() : null;
                String year = map.containsKey("year") ? map.get("year").getText() : null;
                String cvv = map.containsKey("cvv") ? map.get("cvv").getText() : null;
                String postal = map.containsKey("postal") ? map.get("postal").getText() : null;
                String company = map.containsKey("company") ? map.get("company").getText() : null;

                //TODO add validation

                boolean status = billingMobileService.payment(
                        rid,
                        planId,
                        firstName,
                        lastName,
                        company,
                        cardNumber,
                        month,
                        year,
                        cvv,
                        postal);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("status", status);
                return jsonObject.toString();
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
                return null;
            }
        }
    }
}
