package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.DeviceService;
import com.receiptofi.mobile.service.NotificationMobileService;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

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
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 6/7/16 11:40 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api/notification")
public class NotificationController {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationController.class);

    private AuthenticateService authenticateService;
    private NotificationMobileService notificationMobileService;
    private DeviceService deviceService;

    @Autowired
    public NotificationController(
            AuthenticateService authenticateService,
            NotificationMobileService notificationMobileService,
            DeviceService deviceService
    ) {
        this.authenticateService = authenticateService;
        this.notificationMobileService = notificationMobileService;
        this.deviceService = deviceService;
    }

    @RequestMapping (
            value = "/read.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String markNotificationRead(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestHeader ("X-R-DID")
            String deviceId,

            @RequestBody
            String requestBodyJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        }

        Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
        notificationMobileService.markNotificationRead(map.get("notificationIds").getText(), rid);
        return deviceService.getUpdates(rid, deviceId).asJson();
    }
}
