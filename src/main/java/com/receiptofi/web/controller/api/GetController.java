package com.receiptofi.web.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: hitender
 * Date: 7/2/14 11:54 PM
 */
@Controller
@RequestMapping(value = "/webapi/mobile/get")
public class GetController {
    private static Logger log = LoggerFactory.getLogger(GetController.class);

    @Value("${web.access.api.token}")
    private String webApiAccessToken;

    /**
     * Dummy call to populate header with CSRF token in filter
     *
     * @return
     */
    @RequestMapping(
            method = RequestMethod.GET,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    String get(
            @RequestHeader("X-R-API-MOBILE") String apiAccessToken,
            HttpServletResponse httpServletResponse) throws IOException {
        log.debug("getter called to populate session and CSRF");
        if(webApiAccessToken.equals(apiAccessToken)) {
            return "{}";
        }
        log.warn("not matching X-R-API-MOBILE key={}", apiAccessToken);
        httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "");
        return null;
    }
}