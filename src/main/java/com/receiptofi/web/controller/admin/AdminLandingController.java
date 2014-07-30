/**
 *
 */
package com.receiptofi.web.controller.admin;

import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.service.AdminLandingService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.web.form.UserSearchForm;
import com.receiptofi.web.util.PerformanceProfiling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.joda.time.DateTime;

/**
 * Redirect to prevent re-submit.
 *
 * @author hitender
 * @since Mar 26, 2013 1:14:24 AM
 */
@Controller
@RequestMapping(value = "/admin")
public final class AdminLandingController {
    private static final Logger log = LoggerFactory.getLogger(AdminLandingController.class);
    private static final String nextPage = "/admin/landing";

    @Autowired private AdminLandingService adminLandingService;

    @RequestMapping(value = "/landing", method = RequestMethod.GET)
    public ModelAndView loadForm(@ModelAttribute("userSearchForm") UserSearchForm userSearchForm) {
        ModelAndView modelAndView = new ModelAndView(nextPage);
        modelAndView.addObject("userSearchForm", userSearchForm);
        return modelAndView;
    }

    /**
     * @param name Search for user name
     * @return
     */
    @RequestMapping(value = "/find_user", method = RequestMethod.GET)
    public
    @ResponseBody
    List<String> findUser(@RequestParam("term") String name) throws IOException {
        return adminLandingService.findMatchingUsers(name);
    }

    /**
     * @param userSearchForm
     * @return
     */
    @RequestMapping(value = "/landing", method = RequestMethod.POST)
    public String loadUser(@ModelAttribute("userLoginForm") UserSearchForm userSearchForm, RedirectAttributes redirectAttrs) {
        DateTime time = DateUtil.now();
        List<UserProfileEntity> userProfileEntities = adminLandingService.findAllUsers(userSearchForm.getUserName());

        List<UserSearchForm> userSearchForms = new ArrayList<>();
        for(UserProfileEntity user : userProfileEntities) {
            UserSearchForm userForm = UserSearchForm.newInstance(user);
            userSearchForms.add(userForm);
        }

        redirectAttrs.addFlashAttribute("users", userSearchForms);
        redirectAttrs.addFlashAttribute("userSearchForm", userSearchForm);

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());

        //Re-direct to prevent resubmit
        return "redirect:" + nextPage + ".htm";
    }
}