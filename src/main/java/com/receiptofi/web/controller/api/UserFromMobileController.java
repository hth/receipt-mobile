package com.receiptofi.web.controller.api;

import com.receiptofi.domain.types.ProviderEnum;
import com.receiptofi.service.CustomUserDetailsService;
import com.receiptofi.utils.ParseJsonStringToMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * User: hitender
 * Date: 6/29/14 7:56 PM
 */
@Controller
@RequestMapping(value = "/webapi/mobile")
public class UserFromMobileController {
    private static Logger log = LoggerFactory.getLogger(UserFromMobileController.class);

    @Value("${web.access.api.token}")
    private String webApiAccessToken;

    @Autowired private CustomUserDetailsService customUserDetailsService;

    @RequestMapping(
            value = "/auth-create",
            method = RequestMethod.POST
    )
    public
    @ResponseBody
    String authenticateOrCreate(
            @RequestBody String authenticationJson,
            @RequestHeader("X-R-API-MOBILE") String apiAccessToken,
            HttpServletResponse httpServletResponse) throws IOException {
        log.info("webApiAccessToken={}", webApiAccessToken);

        if(webApiAccessToken.equals(apiAccessToken)) {
            Map<String, String> map = new HashMap<>();
            try {
                map = ParseJsonStringToMap.jsonStringToMap(authenticationJson);
            } catch (IOException e) {
                log.error("could not parse authenticationJson={} reason={}", authenticationJson, e.getLocalizedMessage(), e);
            }
            Assert.notNull(map);
            try {
                return customUserDetailsService.signInOrSignup(ProviderEnum.valueOf(map.get("pid")), map.get("at"));
            } catch(HttpClientErrorException e) {
                log.error("error pid={} reason={}", map.get("pid"), e.getLocalizedMessage(), e);

                JsonObject error = new JsonObject();
                error.addProperty("httpStatusCode", e.getStatusCode().value());
                error.addProperty("httpStatus", e.getStatusCode().name());
                error.addProperty("reason", "denied by provider" + (StringUtils.isBlank(map.get("pid")) ? StringUtils.EMPTY : " " + map.get("pid")));

                JsonObject result = new JsonObject();
                result.add("error", error);

                return new Gson().toJson(result);
            }
        }
        log.warn("not matching X-R-API-MOBILE key={}", apiAccessToken);
        httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "");
        return null;
    }
}