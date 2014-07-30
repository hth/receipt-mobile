package com.receiptofi.web.controller.access;

import com.receiptofi.social.domain.site.ReceiptUser;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.web.util.PerformanceProfiling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 5/10/14 10:31 AM
 */
@Controller
@RequestMapping(value = "/access/completeprofile")
public final class CompleteProfileController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Value("${maxSkipProfileUpdate:5}")
    private int maxSkipProfileUpdate;

    /**
     * Loads initial form
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public String completeProfile() {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return "completeprofile";
    }

    //XXX TODO complete this to update profile; can skip max of 5 times should be configurable
    @RequestMapping(method = RequestMethod.POST)
    public String updateProfile() {
        return "redirect:/access/landing.htm";
    }
}
