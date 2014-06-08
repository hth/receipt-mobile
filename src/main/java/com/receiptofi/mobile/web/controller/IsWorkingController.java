package com.receiptofi.mobile.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: hitender
 * Date: 6/5/14 1:49 AM
 */
@Controller
public final class IsWorkingController {
    private static final Logger log = LoggerFactory.getLogger(IsWorkingController.class);

    @RequestMapping(value = "/isWorking", method = RequestMethod.GET)
    public String index() {
        log.info("isWorking");
        return "isWorking";
    }
}
