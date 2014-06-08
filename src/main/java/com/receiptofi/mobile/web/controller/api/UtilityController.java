package com.receiptofi.mobile.web.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @RequestMapping(
            value = "/haveAccess",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8"
    )
    public @ResponseBody
    boolean getVersion(
            @RequestHeader("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth
    ) {
        log.debug("email={}, auth={}", mail, "*********");
        return true;
    }
}
