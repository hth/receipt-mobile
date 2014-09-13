package com.receiptofi.mobile.web.controller;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.receiptofi.mobile.service.SocialAuthenticationService;
import com.receiptofi.utils.ParseJsonStringToMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
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
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal"
})
@Controller
public final class SocialAuthenticationController {
    private static final Logger LOG = LoggerFactory.getLogger(SocialAuthenticationController.class);

    @Autowired SocialAuthenticationService socialAuthenticationService;

    /**
     * Social provider signup or login.
     *
     * http http://localhost:9090/receipt-mobile/authenticate.json < ~/Downloads/pid.json
     * pid.json
     *  {
     *      "pid": "SOCIAL",
     *      "at": "XXXXXXXXX"
     *  }
     *
     *  On Success
     *
     *
     *
     *
     *  On failure
     *
     *  Cache-Control: no-cache, no-store, max-age=0, must-revalidate
     *  Content-Length: 97
     *  Content-Type: application/json;charset=UTF-8
     *  Date: Fri, 11 Jul 2014 04:44:43 GMT
     *  Expires: 0
     *  Pragma: no-cache
     *  Server: Apache-Coyote/1.1
     *  X-Content-Type-Options: nosniff
     *  X-Frame-Options: DENY
     *  X-XSS-Protection: 1; mode=block
     *
     *  {
     *      "error": {
     *          "httpStatus": "UNAUTHORIZED",
     *          "httpStatusCode": 401,
     *          "reason": "denied by provider GOOGLE"
     *      }
     *  }
     *
     * @return
     */
    @RequestMapping (
            value = "/authenticate.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    String authenticateUser(@RequestBody String authenticationJson, HttpServletResponse response) {
        String credential = "{}";
        try {
            Map<String, String> map = ParseJsonStringToMap.jsonStringToMap(authenticationJson);
            //TODO(hth) remove this log or set to debug
            LOG.info("pid={} at={}", map.get("pid"), map.get("at"));
            credential = socialAuthenticationService.authenticateWeb(map.get("pid"), map.get("at"));
            if (credential.length() == 0 || credential.contains("systemError") || credential.contains("401")) {
                return credential;
            }

            Map<String, String> credentialMap = ParseJsonStringToMap.jsonStringToMap(credential);
            response.addHeader("X-R-AUTH", credentialMap.get("X-R-AUTH"));
            response.addHeader("X-R-MAIL", credentialMap.get("X-R-MAIL"));

            return credential;
        } catch (IOException e) {
            LOG.error("could not parse authenticationJson={} reason={}", authenticationJson, e.getLocalizedMessage(), e);
            return credential;
        }
    }
}
