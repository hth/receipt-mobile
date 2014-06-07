/**
 *
 */
package com.receiptofi.web.controller.open;

import com.receiptofi.domain.EmailValidateEntity;
import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.service.AccountService;
import com.receiptofi.service.EmailValidateService;
import com.receiptofi.service.MailService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.form.UserRegistrationForm;
import com.receiptofi.web.helper.AvailabilityStatus;
import com.receiptofi.web.validator.UserRegistrationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.joda.time.DateTime;

/**
 * @author hitender
 * @since Dec 24, 2012 3:13:26 PM
 */
@Controller
@RequestMapping(value = "/open/registration")
public final class AccountRegistrationController {
    private static final Logger log = LoggerFactory.getLogger(AccountRegistrationController.class);

    @Value("${registrationPage:registration}")
    private String registrationPage;

    @Value("${registrationSuccess:redirect:/open/registration/success.htm}")
    private String registrationSuccess;

    @Value("${registrationSuccessPage:registrationsuccess}")
    private String registrationSuccessPage;

    @Value("${recover:redirect:/open/forgot/recover.htm}")
    private String recover;

    private final UserRegistrationValidator userRegistrationValidator;
    private final AccountService accountService;
    private final MailService mailService;
    private final EmailValidateService emailValidateService;

    @Autowired
    public AccountRegistrationController(
            UserRegistrationValidator userRegistrationValidator,
            AccountService accountService,
            MailService mailService,
            EmailValidateService emailValidateService) {
        this.userRegistrationValidator = userRegistrationValidator;
        this.accountService = accountService;
        this.mailService = mailService;
        this.emailValidateService = emailValidateService;
    }

    @ModelAttribute("userRegistrationForm")
    public UserRegistrationForm getUserRegistrationForm() {
        return UserRegistrationForm.newInstance();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String loadForm() {
        log.debug("New Account Registration invoked");
        return registrationPage;
    }

    @RequestMapping(method = RequestMethod.POST, params = {"signup"})
    public String post(@ModelAttribute("userRegistrationForm") UserRegistrationForm userRegistrationForm, RedirectAttributes redirectAttrs, BindingResult result) {
        DateTime time = DateUtil.now();
        userRegistrationValidator.validate(userRegistrationForm, result);
        if(result.hasErrors()) {
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "validation error");
            return registrationPage;
        }

        UserProfileEntity userProfile = accountService.findIfUserExists(userRegistrationForm.getEmailId());
        if(userProfile != null) {
            userRegistrationValidator.accountExists(userRegistrationForm, result);
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "account exists");
            return registrationPage;
        }

        UserAccountEntity userAccount;
        try {
            userAccount = accountService.executeCreationOfNewAccount(
                    userRegistrationForm.getEmailId(),
                    userRegistrationForm.getFirstName(),
                    userRegistrationForm.getLastName(),
                    userRegistrationForm.getPassword()
            );
        } catch (RuntimeException exce) {
            log.error(exce.getLocalizedMessage());
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "failure in registering user");
            return registrationPage;
        }

        log.info("Registered new user Id={}", userAccount.getReceiptUserId());
        redirectAttrs.addFlashAttribute("email", userAccount.getUserId());

        EmailValidateEntity accountValidate = emailValidateService.saveAccountValidate(userAccount.getReceiptUserId(), userAccount.getUserId());
        mailService.accountValidationEmail(userAccount, accountValidate);

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "success");
        return registrationSuccess;
    }

    /**
     * Starts the account recovery process
     * @param email
     * @param httpServletResponse
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/success")
    public String success(@ModelAttribute("email") String email, HttpServletResponse httpServletResponse) throws IOException {
        if(StringUtils.isNotBlank(email)) {
            return registrationSuccessPage;
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    /**
     * Starts the account recovery process
     * @param userRegistrationForm
     * @param redirectAttrs
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, params = {"recover"})
    public String recover(@ModelAttribute("userRegistrationForm") UserRegistrationForm userRegistrationForm, RedirectAttributes redirectAttrs) {
        redirectAttrs.addFlashAttribute("userRegistrationForm", userRegistrationForm);
        return recover;
    }

    /**
     * Ajax call to check if the account is available to register.
     * @param body
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = "/availability",
            method = RequestMethod.POST,
            headers = "Accept=application/json",
            produces = "application/json"
    )
    public
    @ResponseBody
    String getAvailability(@RequestBody String body) throws IOException {
        DateTime time = DateUtil.now();
        String email =  StringUtils.lowerCase(ParseJsonStringToMap.jsonStringToMap(body).get("email"));
        AvailabilityStatus availabilityStatus;

        UserProfileEntity userProfileEntity = accountService.findIfUserExists(email);
        if(userProfileEntity != null && userProfileEntity.getEmail().equals(email)) {
            log.info("Email={} provided during registration exists", email);
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "success");
            availabilityStatus = AvailabilityStatus.notAvailable(email);
            return String.format("{ \"valid\" : \"%s\", \"message\" : \"<b>%s</b> is already registered. %s\" }", availabilityStatus.isAvailable(), email, StringUtils.join(availabilityStatus.getSuggestions()));
        }
        log.info("Email available={} for registration", email);
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "success");
        availabilityStatus = AvailabilityStatus.available();
        return String.format("{ \"valid\" : \"%s\" }", availabilityStatus.isAvailable());
    }
}
