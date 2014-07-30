package com.receiptofi.web.controller.access;

import com.receiptofi.social.domain.site.ReceiptUser;
import com.receiptofi.repository.NotificationManager;
import com.receiptofi.service.NotificationService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.web.util.PerformanceProfiling;
import com.receiptofi.web.form.NotificationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 7/1/13
 * Time: 9:51 PM
 */
@Controller
@RequestMapping(value = "/access/notification")
public final class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(LandingController.class);

    @Autowired NotificationService notificationService;

    /**
     * Refers to landing.jsp
     */
    private static final String NEXT_PAGE_IS_CALLED_NOTIFICATION = "/notification";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView loadForm(@ModelAttribute("notificationForm") NotificationForm notificationForm) {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("LandingController loadForm: " + receiptUser.getRid());

        ModelAndView modelAndView = new ModelAndView(NEXT_PAGE_IS_CALLED_NOTIFICATION);
        notificationForm.setNotifications(notificationService.notifications(receiptUser.getRid(), NotificationManager.ALL));

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return modelAndView;
    }
}
