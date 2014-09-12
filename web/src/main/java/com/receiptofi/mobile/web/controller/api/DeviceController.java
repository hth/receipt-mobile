package com.receiptofi.mobile.web.controller.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.receiptofi.mobile.domain.DeviceRegistered;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.DeviceService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: hitender
 * Date: 8/9/14 2:22 PM
 */
@Controller
@RequestMapping (value = "/api")
public final class DeviceController {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceController.class);

    @SuppressWarnings ({"PMD.BeanMembersShouldSerialize"})
    private DeviceService deviceService;

    @SuppressWarnings ({"PMD.BeanMembersShouldSerialize"})
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
     * @param deviceId - Device id
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            method = RequestMethod.GET,
            value = "/update",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    String hasUpdate(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestHeader ("X-R-DID")
            String deviceId,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return null;
        } else {
            try {
                return deviceService.hasUpdate(rid, deviceId).asJson();
            } catch (Exception e) {
                LOG.error("fetching update for device failed deviceId={} reason={}", deviceId, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put("reason", "something went wrong");
                errors.put("did", deviceId);
                errors.put("systemError", MobileSystemErrorCodeEnum.USER_INPUT.name());
                errors.put("systemErrorCode", MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);
            }
        }
    }

    /**
     * Finds if device exists or saves the device when does not exists. Most likely this call would not be required.
     *
     * @param mail
     * @param auth
     * @param did
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            method = RequestMethod.POST,
            value = "/register",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    DeviceRegistered registerDevice(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestHeader ("X-R-DID")
            String did,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return null;
        } else {
            return DeviceRegistered.newInstance(deviceService.registerDevice(rid, did));
        }
    }
}
