package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.json.JsonFriend;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.DeviceService;
import com.receiptofi.mobile.service.ReceiptMobileService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import com.receiptofi.service.FriendService;
import com.receiptofi.service.ReceiptService;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 9/25/15 4:48 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Controller
@RequestMapping (value = "/api/split")
public class SplitController {
    private static final Logger LOG = LoggerFactory.getLogger(SplitController.class);

    private FriendService friendService;
    private AuthenticateService authenticateService;
    private ReceiptService receiptService;
    private DeviceService deviceService;
    private ReceiptMobileService receiptMobileService;

    @Autowired
    public SplitController(
            FriendService friendService,
            AuthenticateService authenticateService,
            ReceiptService receiptService,
            DeviceService deviceService,
            ReceiptMobileService receiptMobileService
    ) {
        this.friendService = friendService;
        this.authenticateService = authenticateService;
        this.receiptService = receiptService;
        this.deviceService = deviceService;
        this.receiptMobileService = receiptMobileService;
    }

    @Timed
    @ExceptionMetered
    @RequestMapping (
            value = "/friends",
            method = RequestMethod.GET,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    @ResponseBody
    public Collection<JsonFriend> getFriends(
            @RequestHeader ("X-R-MAIL")
            ScrubbedInput mail,

            @RequestHeader ("X-R-AUTH")
            ScrubbedInput auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail.getText(), auth.getText());
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return Collections.emptyList();
        } else {
            return friendService.getFriends(rid).values();
        }
    }

    /**
     * Original owner of the receipt can add or remove friends from split.
     *
     * @param mail
     * @param auth
     * @param deviceId
     * @param requestBodyJson
     * @param httpServletResponse
     * @return
     * @throws IOException
     */
    @Timed
    @ExceptionMetered
    @RequestMapping (
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ResponseBody
    public String split(
            @RequestHeader ("X-R-MAIL")
            ScrubbedInput mail,

            @RequestHeader ("X-R-AUTH")
            ScrubbedInput auth,

            @RequestHeader ("X-R-DID")
            ScrubbedInput deviceId,

            @RequestBody
            String requestBodyJson,

            HttpServletResponse httpServletResponse
    ) throws IOException {
        LOG.info("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail.getText(), auth.getText());
        if (rid == null) {
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            try {
                LOG.info("complete auth mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
                Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
                String fidAdd = map.containsKey("fidAdd") ? map.get("fidAdd").getText() : null;
                String receiptId = map.containsKey("receiptId") ? map.get("receiptId").getText() : null;
                LOG.info("Receipt id={} fidAdd={}", receiptId, fidAdd);

                ReceiptEntity receipt = receiptService.findReceipt(receiptId, rid);
                if (null == receipt) {
                    LOG.warn("No Receipt found to Split with receiptId={}", receiptId);
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "NotFound");
                    return null;
                } else if (StringUtils.isNotBlank(receipt.getReferReceiptId())) {
                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Cannot spilt shared receipt.");
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.MOBILE.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.MOBILE.getCode());

                    return ErrorEncounteredJson.toJson(errors);
                } else {
                    receiptMobileService.executeSplit(fidAdd, receiptId, receipt);
                }

                return deviceService.getUpdates(rid, deviceId.getText()).asJson();
            } catch (Exception e) {
                LOG.error("Failure during split rid={} reason={}", rid, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Something went wrong. Engineers are looking into this.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);
            }
        }
    }
}
