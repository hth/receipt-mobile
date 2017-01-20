package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;

import com.receiptofi.domain.types.DeviceTypeEnum;
import com.receiptofi.mobile.domain.DeviceRegistered;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.DeviceService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import com.receiptofi.utils.ScrubbedInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 8/9/14 2:22 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api")
public class DeviceController {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceController.class);

    private DeviceService deviceService;
    private AuthenticateService authenticateService;

    @Autowired
    public DeviceController(DeviceService deviceService, AuthenticateService authenticateService) {
        this.deviceService = deviceService;
        this.authenticateService = authenticateService;
    }

    /**
     * For a device, find all available updates on server.
     *
     * @param mail
     * @param auth
     * @param deviceId   - Device id
     * @param deviceType iPhone or Android
     * @param response
     * @return
     * @throws IOException
     */
    @Timed
    @ExceptionMetered
    @RequestMapping (
            method = RequestMethod.GET,
            value = "/update",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String updates(
            @RequestHeader ("X-R-MAIL")
            ScrubbedInput mail,

            @RequestHeader ("X-R-AUTH")
            ScrubbedInput auth,

            @RequestHeader ("X-R-DID")
            ScrubbedInput deviceId,

            @RequestHeader (value = "X-R-DT", required = false, defaultValue = "A")
            ScrubbedInput deviceType,

            @RequestHeader (value = "X-R-TK", required = false, defaultValue = "")
            ScrubbedInput deviceToken,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("Update for mail={}, auth={} token={}", mail, UtilityController.AUTH_KEY_HIDDEN, deviceToken);
        String rid = authenticateService.getReceiptUserId(mail.getText(), auth.getText());
        if (null == rid) {
            LOG.info("Un-authorized access to /api/update by mail={}", mail);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        }

        DeviceTypeEnum deviceTypeEnum;
        try {
            deviceTypeEnum = DeviceTypeEnum.valueOf(deviceType.getText());
        } catch (Exception e) {
            LOG.error("Failed parsing deviceType, reason={}", e.getLocalizedMessage(), e);
            return getErrorReason("Incorrect device type.", USER_INPUT);
        }

        try {
            return deviceService.getUpdates(rid, deviceId.getText(), deviceTypeEnum, deviceToken.getText()).asJson();
        } catch (Exception e) {
            LOG.error("fetching updates for device failed rid={} deviceId={} reason={}",
                    rid, deviceId, e.getLocalizedMessage(), e);

            Map<String, String> errors = new HashMap<>();
            errors.put(ErrorEncounteredJson.REASON, "Something went wrong. Engineers are looking into this.");
            errors.put("did", deviceId.getText());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());

            return ErrorEncounteredJson.toJson(errors);
        }
    }

    /**
     * Irrespective of the device. Get all the data for an account.
     *
     * @param mail
     * @param auth
     * @param response
     * @return
     * @throws IOException
     */
    @Timed
    @ExceptionMetered
    @RequestMapping (
            method = RequestMethod.GET,
            value = "/all",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String all(
            @RequestHeader ("X-R-MAIL")
            ScrubbedInput mail,

            @RequestHeader ("X-R-AUTH")
            ScrubbedInput auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("All for mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail.getText(), auth.getText());
        if (null == rid) {
            LOG.info("Un-authorized access to /api/all by mail={}", mail);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        }

        try {
            return deviceService.getAll(rid).asJson();
        } catch (Exception e) {
            LOG.error("fetching all for rid={} reason={}", rid, e.getLocalizedMessage(), e);
            return getErrorReason("Something went wrong. Engineers are looking into this.", USER_INPUT);
        }
    }

    /**
     * Finds if device exists or saves the device when does not exists. Most likely this call would not be required.
     *
     * @param mail
     * @param auth
     * @param did
     * @param deviceType iPhone or Android
     * @param response
     * @return
     * @throws IOException
     */
    @Timed
    @ExceptionMetered
    @RequestMapping (
            method = RequestMethod.POST,
            value = "/register",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String registerDevice(
            @RequestHeader ("X-R-MAIL")
            ScrubbedInput mail,

            @RequestHeader ("X-R-AUTH")
            ScrubbedInput auth,

            @RequestHeader ("X-R-DID")
            ScrubbedInput did,

            @RequestHeader (value = "X-R-DT", required = false, defaultValue = "A")
            ScrubbedInput deviceType,

            @RequestHeader (value = "X-R-TK", required = false, defaultValue = "")
            ScrubbedInput deviceToken,

            HttpServletResponse response
    ) throws IOException {
        LOG.info("Register mail={}, auth={} token={}", mail, UtilityController.AUTH_KEY_HIDDEN, deviceToken);
        String rid = authenticateService.getReceiptUserId(mail.getText(), auth.getText());
        if (null == rid) {
            LOG.info("Un-authorized access to /api/register by mail={}", mail);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        }

        DeviceTypeEnum deviceTypeEnum;
        try {
            deviceTypeEnum = DeviceTypeEnum.valueOf(deviceType.getText());
        } catch (Exception e) {
            LOG.error("Failed parsing deviceType, reason={}", e.getLocalizedMessage(), e);
            return getErrorReason("Incorrect device type.", USER_INPUT);
        }

        try {
            if (deviceService.lastAccessed(rid, did.getText()) != null) {
                LOG.info("Device already registered rid={} did={}", rid, did);
                return DeviceRegistered.newInstance(true).asJson();
            } else {
                return DeviceRegistered.newInstance(deviceService.registerDevice(rid, did.getText(), deviceTypeEnum, deviceToken.getText())).asJson();
            }
        } catch (Exception e) {
            LOG.error("Failed registering deviceType={}, reason={}", deviceTypeEnum, e.getLocalizedMessage(), e);
            return getErrorReason("Something went wrong. Engineers are looking into this.", USER_INPUT);
        }
    }

    static String getErrorReason(String reason, MobileSystemErrorCodeEnum mobileSystemErrorCode) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ErrorEncounteredJson.REASON, reason);
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, mobileSystemErrorCode.name());
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, mobileSystemErrorCode.getCode());

        return ErrorEncounteredJson.toJson(errors);
    }
}
