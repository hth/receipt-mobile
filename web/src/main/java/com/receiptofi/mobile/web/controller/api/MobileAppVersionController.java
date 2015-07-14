package com.receiptofi.mobile.web.controller.api;

import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

/**
 * User: hitender
 * Date: 7/14/15 1:35 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api")
public class MobileAppVersionController {
    private static final Logger LOG = LoggerFactory.getLogger(MobileAppVersionController.class);
    private JsonObject json;

    @Autowired
    public MobileAppVersionController(@Value ("${APK.Latest.Version}") String latestAPKVersion) {
        json = new JsonObject();
        json.addProperty("apk", latestAPKVersion);
    }

    /**
     * Supports JSON call.
     *
     * @return
     */
    @Timed
    @ExceptionMetered
    @RequestMapping (
            method = RequestMethod.GET,
            value = "/latestAPK",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String getLatestAPK(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth
    ) {
        LOG.debug("Latest APK mail={}", mail);
        return json.toString();
    }
}
