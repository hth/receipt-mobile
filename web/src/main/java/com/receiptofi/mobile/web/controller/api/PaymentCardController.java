package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_JSON;

import com.receiptofi.domain.PaymentCardEntity;
import com.receiptofi.domain.types.CardNetworkEnum;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.DeviceService;
import com.receiptofi.mobile.service.PaymentCardMobileService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.web.validator.PaymentCardValidator;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;
import com.receiptofi.utils.Validate;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 11/28/16 12:35 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api/paymentCard")
public class PaymentCardController {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentCardController.class);

    private AuthenticateService authenticateService;
    private PaymentCardMobileService paymentCardMobileService;
    private DeviceService deviceService;
    private PaymentCardValidator paymentCardValidator;

    @Autowired
    public PaymentCardController(
            AuthenticateService authenticateService,
            PaymentCardMobileService paymentCardMobileService,
            DeviceService deviceService,
            PaymentCardValidator paymentCardValidator) {
        this.authenticateService = authenticateService;
        this.paymentCardMobileService = paymentCardMobileService;
        this.deviceService = deviceService;
        this.paymentCardValidator = paymentCardValidator;
    }

    /**
     * Create, Update, Delete coupons.
     *
     * @param mail
     * @param auth
     * @param requestBodyJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/update.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String update(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestHeader ("X-R-DID")
            String deviceId,

            @RequestBody
            String requestBodyJson,

            HttpServletResponse response
    ) throws IOException, ParseException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map;
            try {
                map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
            } catch (IOException e) {
                LOG.error("Could not parse json={} reason={}", requestBodyJson, e.getLocalizedMessage(), e);
                return ErrorEncounteredJson.toJson("Could not parse JSON", MOBILE_JSON);
            }

            if (map.isEmpty()) {
                /** Validation failure as there is no data in the map. */
                return ErrorEncounteredJson.toJson(paymentCardValidator.validateEmptyFailure());
            } else {
                try {
                    map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
                    String id = map.containsKey("id") ? map.get("id").getText() : "";
                    String cardName = map.containsKey("nm") ? map.get("nm").getText() : "";
                    String cardNetwork = map.containsKey("cn") ? map.get("cn").getText() : "";
                    String cardDigit = map.containsKey("cd") ? map.get("cd").getText() : "";
                    String active = map.containsKey("a") ? map.get("a").toString() : "";

                    if (StringUtils.isNotBlank(id) && !Validate.isValidObjectId(id)) {
                        Map<String, String> errors = new HashMap<>();
                        errors.put(ErrorEncounteredJson.REASON, "Invalid Data");
                        errors.put("id", id);
                        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MOBILE_JSON.name());
                        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MOBILE_JSON.getCode());
                        return ErrorEncounteredJson.toJson(errors);
                    }

                    Map<String, String> errors = paymentCardValidator.validate(cardName, cardNetwork, cardDigit, active);
                    if (!errors.isEmpty()) {
                        return ErrorEncounteredJson.toJson(errors);
                    }

                    PaymentCardEntity paymentCard = paymentCardMobileService.populateCard(
                            id,
                            cardName,
                            CardNetworkEnum.valueOf(cardNetwork),
                            cardDigit,
                            BooleanUtils.toBoolean(Integer.parseInt(active)),
                            rid);

                    paymentCardMobileService.save(paymentCard);
                    return deviceService.getUpdates(rid, deviceId).asJson();
                } catch (Exception e) {
                    LOG.error("Failure during payment card save rid={} reason={}", rid, e.getLocalizedMessage(), e);
                    Map<String, String> errors = ExpenseTagController.getErrorSevere("Something went wrong. Engineers are looking into this.");
                    return ErrorEncounteredJson.toJson(errors);
                }
            }
        }
    }
}
