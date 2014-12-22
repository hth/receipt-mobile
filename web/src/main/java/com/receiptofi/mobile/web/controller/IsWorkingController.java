package com.receiptofi.mobile.web.controller;

import com.receiptofi.mobile.domain.MobileApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Controller
public class IsWorkingController {
    private static final Logger LOG = LoggerFactory.getLogger(IsWorkingController.class);

    /**
     * Supports HTML call.
     *
     * @return
     */
    @RequestMapping (
            value = "/isWorking",
            method = RequestMethod.GET,
            produces = {
                    MediaType.TEXT_HTML_VALUE + ";charset=UTF-8",
            }
    )
    public String isWorking() {
        LOG.info("isWorking");
        return "isWorking";
    }

    /**
     * Supports JSON call.
     *
     * @return
     */
    @RequestMapping (
            value = "/healthCheck",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    @ResponseStatus (HttpStatus.OK)
    @ResponseBody
    public MobileApi healthCheck() {
        //TODO(hth) should perform some kind of health check like connecting to mongo
        return MobileApi.newInstance(true);
    }
}
