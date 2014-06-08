package com.receiptofi.web.validator;

import com.receiptofi.web.form.ForgotRecoverForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * User: hitender
 * Date: 5/31/13
 * Time: 8:29 PM
 */
@Component
public final class ForgotRecoverValidator implements Validator {
    private static final Logger log = LoggerFactory.getLogger(ForgotRecoverValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return ForgotRecoverForm.class.equals(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        log.debug("Executing validation");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailId", "field.required", new Object[]{"Email Id"});

        ForgotRecoverForm frf = (ForgotRecoverForm) obj;
        if(StringUtils.isNotEmpty(frf.getCaptcha())) {
            errors.rejectValue("captcha", "field.unmatched", new Object[] { "" }, "Entered value does not match");
        }

        EmailValidator emailValidator = EmailValidator.getInstance();
        if(!emailValidator.isValid(frf.getEmailId())) {
            errors.rejectValue("emailId", "field.email.address.not.valid", new Object[] { frf.getEmailId() }, "Email Address provided is not valid");
        }
    }
}
