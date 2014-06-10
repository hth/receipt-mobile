package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.mobile.domain.UserAccess;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: hitender
 * Date: 6/8/14 2:31 PM
 */
@Controller
@RequestMapping(value = "/api")
public class UtilityController {

    private static final Logger log = LoggerFactory.getLogger(UtilityController.class);

    private AuthenticateService authenticateService;

    @Autowired
    public UtilityController(AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @RequestMapping(
            value = "/hasAccess",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8"
    )
    public @ResponseBody
    UserAccess hasAccess(
            @RequestHeader("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        log.debug("email={}, auth={}", mail, "*********");
        if(authenticateService.hasAccess(mail, auth)) {
            return UserAccess.newInstance("granted");
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }
}
