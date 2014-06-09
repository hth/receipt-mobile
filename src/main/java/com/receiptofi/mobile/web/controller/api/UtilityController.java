package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
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

    private AccountService accountService;

    @Autowired
    public UtilityController(AccountService accountService) {
        this.accountService = accountService;
    }

    @RequestMapping(
            value = "/haveAccess",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8"
    )
    public @ResponseBody
    String haveAccess(
            @RequestHeader("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        log.debug("email={}, auth={}", mail, "*********");
        if(accountService.hasAccess(mail, auth)) {
            return "Access";
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return "Unauthorized";
        }
    }
}
