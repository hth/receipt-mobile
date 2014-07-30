package com.receiptofi.web.controller.open;

import com.receiptofi.domain.ForgotRecoverEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.service.AccountService;
import com.receiptofi.service.LoginService;
import com.receiptofi.service.MailService;
import com.receiptofi.service.UserProfilePreferenceService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.HashText;
import com.receiptofi.web.util.PerformanceProfiling;
import com.receiptofi.utils.RandomString;
import com.receiptofi.web.form.ForgotAuthenticateForm;
import com.receiptofi.web.form.ForgotRecoverForm;
import com.receiptofi.web.form.UserRegistrationForm;
import com.receiptofi.web.validator.ForgotAuthenticateValidator;
import com.receiptofi.web.validator.ForgotRecoverValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 6/4/13
 * Time: 9:44 AM
 */
@Controller
@RequestMapping(value = "/open/forgot")
public final class ForgotController {
    private static final Logger log = LoggerFactory.getLogger(ForgotController.class);

    @Value("${password:/forgot/password}")
    private String passwordPage;

    @Value("${recoverPage:/forgot/recover}")
    private String recoverPage;

    @Value("${recoverConfirmPage:/forgot/recoverConfirm}")
    private String recoverConfirmPage;

    @Value("${recoverConfirm:redirect:/open/forgot/recoverConfirm.htm}")
    private String recoverConfirm;

    @Value("${authenticatePage:/forgot/authenticate}")
    private String authenticatePage;

    @Value("${authenticationConfirmPage:/forgot/authenticateConfirm}")
    private String authenticateConfirm;

    /** Used in RedirectAttributes */
    private static final String SUCCESS_EMAIL   = "success_email";

    /** Used in JSP page /forgot/authenticateConfirm */
    private static final String SUCCESS         = "success";

    @Autowired private AccountService accountService;
    @Autowired private ForgotRecoverValidator forgotRecoverValidator;
    @Autowired private UserProfilePreferenceService userProfilePreferenceService;
    @Autowired private ForgotAuthenticateValidator forgotAuthenticateValidator;
    @Autowired private MailService mailService;
    @Autowired private LoginService loginService;

    @RequestMapping(method = RequestMethod.GET, value = "password")
    public String onPasswordLinkClicked(@ModelAttribute("forgotRecoverForm") ForgotRecoverForm forgotRecoverForm) {
        log.info("Password recovery page invoked");
        return passwordPage;
    }

    @RequestMapping(method = RequestMethod.POST, value = "password", params = {"forgot_password"})
    public String emailUserForPasswordRecovery(
            @ModelAttribute("forgotRecoverForm")
            ForgotRecoverForm forgotRecoverForm,

            BindingResult result,
            RedirectAttributes redirectAttrs
    ) throws IOException {

        DateTime time = DateUtil.now();
        forgotRecoverValidator.validate(forgotRecoverForm, result);
        if(result.hasErrors()) {
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "validation error");
            return passwordPage;
        }

        boolean status = mailService.mailRecoverLink(forgotRecoverForm.getEmailId());
        if(!status) {
            log.error("Failed to send recovery email for user={}", forgotRecoverForm.getEmailId());
        }

        redirectAttrs.addFlashAttribute(SUCCESS_EMAIL, Boolean.toString(status));
        return recoverConfirm;
    }

    /**
     * Method just for changing the URL, hence have to use re-direct.
     * This could be an expensive call because of redirect.
     *
     * Its redirected from RequestMethod.POST from
     * @see AccountRegistrationController#recover(UserRegistrationForm, RedirectAttributes)
     *
     * @param userRegistrationForm
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "recover")
    public ModelAndView whenAccountAlreadyExists(
            @ModelAttribute("userRegistrationForm")
            UserRegistrationForm userRegistrationForm,

            HttpServletResponse httpServletResponse
    ) throws IOException {
        log.info("Recover password process initiated for user={}", userRegistrationForm.getEmailId());
        if(StringUtils.isEmpty(userRegistrationForm.getEmailId())) {
            httpServletResponse.sendError(SC_FORBIDDEN, "Cannot access recover directly");
            return null;
        }

        ForgotRecoverForm forgotRecoverForm = ForgotRecoverForm.newInstance();
        forgotRecoverForm.setEmailId(userRegistrationForm.getEmailId());
        forgotRecoverForm.setCaptcha(userRegistrationForm.getEmailId());

        return new ModelAndView(recoverPage, "forgotRecoverForm", forgotRecoverForm);
    }

    /**
     * Add this gymnastic to make sure the page does not process when refreshed again or bookmarked.
     *
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "recoverConfirm")
    public String showConfirmationPageForProcessingPasswordRecovery(
            @ModelAttribute(SUCCESS_EMAIL)
            String success,

            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) throws IOException {

        //referer is a weak check; strong check would be to check against the actual value of referer
        if(StringUtils.isNotBlank(success) && StringUtils.isNotBlank(httpServletRequest.getHeader("Referer"))) {
            return recoverConfirmPage;
        }
        log.warn("ah! some just tried access={}", recoverConfirmPage);
        httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "authenticate")
    public String whenClickedOnEmailLink(
            @RequestParam("authenticationKey")
            String key,

            ForgotAuthenticateForm forgotAuthenticateForm
    ) {
        ForgotRecoverEntity forgotRecoverEntity = accountService.findAccountAuthenticationForKey(key);
        if(forgotRecoverEntity != null) {
            forgotAuthenticateForm.setAuthenticationKey(key);
            forgotAuthenticateForm.setReceiptUserId(forgotRecoverEntity.getReceiptUserId());
        }
        return authenticatePage;
    }

    @RequestMapping(method = RequestMethod.POST, value = "authenticate", params = {"update_password"})
    public ModelAndView updatePassword(
            @ModelAttribute("forgotAuthenticateForm")
            ForgotAuthenticateForm forgotAuthenticateForm,

            BindingResult result
    ) {
        DateTime time = DateUtil.now();
        forgotAuthenticateValidator.validate(forgotAuthenticateForm, result);
        if (result.hasErrors()) {
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), " failure");
            return new ModelAndView(authenticatePage);
        } else {
            ForgotRecoverEntity forgotRecoverEntity = accountService.findAccountAuthenticationForKey(forgotAuthenticateForm.getAuthenticationKey());
            ModelAndView modelAndView = new ModelAndView(authenticateConfirm);
            if(forgotRecoverEntity == null) {
                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), " failure");
                modelAndView.addObject(SUCCESS, false);
            } else {
                UserProfileEntity userProfileEntity = userProfilePreferenceService.findByReceiptUserId(forgotRecoverEntity.getReceiptUserId());
                Assert.notNull(userProfileEntity);

                UserAuthenticationEntity userAuthenticationEntity = UserAuthenticationEntity.newInstance(
                        HashText.computeBCrypt(forgotAuthenticateForm.getPassword()),
                        HashText.computeBCrypt(RandomString.newInstance().nextString())
                );

                UserAuthenticationEntity userAuthenticationLoaded = loginService.findByReceiptUserId(userProfileEntity.getReceiptUserId()).getUserAuthentication();

                userAuthenticationEntity.setId(userAuthenticationLoaded.getId());
                userAuthenticationEntity.setVersion(userAuthenticationLoaded.getVersion());
                userAuthenticationEntity.setCreated(userAuthenticationLoaded.getCreated());
                userAuthenticationEntity.setUpdated();
                try {
                    accountService.updateAuthentication(userAuthenticationEntity);
                    accountService.invalidateAllEntries(forgotRecoverEntity.getReceiptUserId());
                    modelAndView.addObject(SUCCESS, true);
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), " success");
                } catch (Exception e) {
                    log.error("Error during updating of the old authentication keys: " + e.getLocalizedMessage());
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), " failure");
                    modelAndView.addObject(SUCCESS, false);
                }
            }
            return modelAndView;
        }
    }
}
