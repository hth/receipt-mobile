package com.receiptofi.mobile.web.controller;

import com.receiptofi.mobile.domain.SocialAuthenticationResponse;
import com.receiptofi.mobile.service.SocialAuthenticationService;
import com.receiptofi.utils.ParseJsonStringToMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: hitender
 * Date: 6/27/14 1:03 AM
 */
@Controller
public class SocialAuthenticationController {
    private static Logger log = LoggerFactory.getLogger(SocialAuthenticationController.class);

    @Autowired SocialAuthenticationService socialAuthenticationService;

    /**
     * Supports Social provider call
     * @return
     */
    @RequestMapping(
            value = "/authenticate.json",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public @ResponseBody
    String authenticateUser(@RequestBody String authenticationJson) {
        try {
            Map<String, String> map = ParseJsonStringToMap.jsonStringToMap(authenticationJson);
            return socialAuthenticationService.authenticateWeb(map.get("pid"), map.get("at")).asJson();
        } catch (IOException e) {
            log.error("could not parse authenticationJson={} reason={}", authenticationJson, e.getLocalizedMessage(), e);
        }
        return SocialAuthenticationResponse.newInstance().asJson();
    }
}
