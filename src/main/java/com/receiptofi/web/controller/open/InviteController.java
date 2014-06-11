package com.receiptofi.web.controller.open;

import com.receiptofi.domain.InviteEntity;
import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.repository.UserProfileManager;
import com.receiptofi.service.AccountService;
import com.receiptofi.service.InviteService;
import com.receiptofi.service.LoginService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.HashText;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.utils.RandomString;
import com.receiptofi.web.form.InviteAuthenticateForm;
import com.receiptofi.web.validator.InviteAuthenticateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 6/9/13
 * Time: 1:48 PM
 */
@Controller
@RequestMapping(value = "/open/invite")
public final class InviteController {
    private static final Logger log = LoggerFactory.getLogger(ForgotController.class);

    @Value("${authenticatePage:/invite/authenticate}")
    private String authenticatePage;

    @Value("${authenticateResult:redirect:/open/invite/result.htm}")
    private String authenticateResult;

    @Value("${authenticateConfirmPage:/invite/authenticateConfirm}")
    private String authenticateConfirmPage;

    /** Used in JSP page /invite/authenticateConfirm */
    private static final String SUCCESS = "success";

    @Autowired private AccountService accountService;
    @Autowired private LoginService loginService;
    @Autowired private InviteService inviteService;
    @Autowired private InviteAuthenticateValidator inviteAuthenticateValidator;
    @Autowired private UserProfileManager userProfileManager;

    @RequestMapping(method = RequestMethod.GET, value = "authenticate")
    public String loadForm(
            @RequestParam("authenticationKey")
            String key,

            @ModelAttribute("inviteAuthenticateForm")
            InviteAuthenticateForm inviteAuthenticateForm
    ) {
        InviteEntity inviteEntity = inviteService.findInviteAuthenticationForKey(key);
        if(inviteEntity != null) {
            inviteAuthenticateForm.setEmailId(inviteEntity.getEmailId());
            inviteAuthenticateForm.setFirstName(inviteEntity.getInvited().getFirstName());
            inviteAuthenticateForm.setLastName(inviteEntity.getInvited().getLastName());
            inviteAuthenticateForm.getForgotAuthenticateForm().setAuthenticationKey(key);
            inviteAuthenticateForm.getForgotAuthenticateForm().setReceiptUserId(inviteEntity.getInvited().getReceiptUserId());
        }
        return authenticatePage;
    }

    /**
     * Completes user invitation sent through email
     * @param inviteAuthenticateForm
     * @param redirectAttrs
     * @param result
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "authenticate", params = {"confirm_invitation"})
    public String completeInvitation(
            @ModelAttribute("inviteAuthenticateForm")
            InviteAuthenticateForm inviteAuthenticateForm,
            RedirectAttributes redirectAttrs,
            BindingResult result
    ) {
        DateTime time = DateUtil.now();
        inviteAuthenticateValidator.validate(inviteAuthenticateForm, result);
        if (result.hasErrors()) {
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), " failure");
            return authenticatePage;
        } else {
            InviteEntity inviteEntity = inviteService.findInviteAuthenticationForKey(inviteAuthenticateForm.getForgotAuthenticateForm().getAuthenticationKey());
            if(inviteEntity != null) {
                UserProfileEntity userProfileEntity = inviteEntity.getInvited();
                userProfileEntity.setFirstName(inviteAuthenticateForm.getFirstName());
                userProfileEntity.setLastName(inviteAuthenticateForm.getLastName());
                userProfileEntity.active();

                UserAuthenticationEntity userAuthenticationEntity = UserAuthenticationEntity.newInstance(
                        HashText.computeBCrypt(inviteAuthenticateForm.getForgotAuthenticateForm().getPassword()),
                        HashText.computeBCrypt(RandomString.newInstance().nextString())
                );

                UserAccountEntity userAccountEntity = loginService.findByReceiptUserId(userProfileEntity.getReceiptUserId());

                userAuthenticationEntity.setId(userAccountEntity.getUserAuthentication().getId());
                userAuthenticationEntity.setVersion(userAccountEntity.getUserAuthentication().getVersion());
                userAuthenticationEntity.setCreated(userAccountEntity.getUserAuthentication().getCreated());
                userAuthenticationEntity.setUpdated();
                try {
                    userProfileManager.save(userProfileEntity);
                    accountService.updateAuthentication(userAuthenticationEntity);

                    userAccountEntity.setFirstName(userProfileEntity.getFirstName());
                    userAccountEntity.setLastName(userProfileEntity.getLastName());
                    userAccountEntity.active();
                    userAccountEntity.setAccountValidated(true);
                    userAccountEntity.setUserAuthentication(userAuthenticationEntity);
                    accountService.saveUserAccount(userAccountEntity);

                    inviteService.invalidateAllEntries(inviteEntity);
                    redirectAttrs.addFlashAttribute(SUCCESS, "true");
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), " success");
                } catch (Exception e) {
                    log.error("Error during updating of the old authentication keys={}", e.getLocalizedMessage(), e);
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), " failure");
                    redirectAttrs.addFlashAttribute(SUCCESS, "false");
                }
            } else {
                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), " failure");
                redirectAttrs.addFlashAttribute(SUCCESS, "false");
            }
            return authenticateResult;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/result")
    public String success(
            @ModelAttribute("success")
            String success,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) throws IOException {
        if(StringUtils.isNotBlank(success) && StringUtils.isNotBlank(httpServletRequest.getHeader("Referer"))) {
            return authenticateConfirmPage;
        }
        log.warn("ah! some just tried access={}", authenticateConfirmPage);
        httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }
}
