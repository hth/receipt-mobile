package com.receiptofi.mobile.web.controller;

import com.receiptofi.mobile.domain.MobileApi;
import com.receiptofi.mobile.domain.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * User: hitender
 * Date: 6/5/14 1:49 AM
 */
@Controller
public final class IsWorkingController {
    private static final Logger log = LoggerFactory.getLogger(IsWorkingController.class);

    /**
     * Supports HTML call
     * @return
     */
    @RequestMapping(
            value = "/isWorking",
            method = RequestMethod.GET,
            produces = {
                    MediaType.TEXT_HTML_VALUE + ";charset=UTF-8",
            }
    )
    @ResponseStatus(HttpStatus.OK)
    public String isWorking() {
        log.info("isWorking");
        return "isWorking";
    }

    /**
     * Supports JSON call
     * @return
     */
    @RequestMapping(
            value = "/healthCheck",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public @ResponseBody
    MobileApi healthCheck() {
        //TODO should perform some kind of health check like connecting to mongo
        return MobileApi.newInstance(true);
    }
}
