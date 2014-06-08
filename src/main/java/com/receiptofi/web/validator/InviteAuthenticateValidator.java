package com.receiptofi.web.validator;

import com.receiptofi.web.form.InviteAuthenticateForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * User: hitender
 * Date: 6/9/13
 * Time: 5:39 PM
 */
@Component
public final class InviteAuthenticateValidator implements Validator {
    private static final Logger log = LoggerFactory.getLogger(InviteAuthenticateValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return InviteAuthenticateForm.class.equals(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        log.debug("Executing validation");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required", new Object[] { "First Name" });
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "field.required", new Object[] { "Last Name" });
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "forgotAuthenticateForm.password", "field.required", new Object[]{"Password"});
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "forgotAuthenticateForm.passwordSecond", "field.required", new Object[]{"Retype Password"});

        InviteAuthenticateForm faa = (InviteAuthenticateForm) obj;
        if (faa.getFirstName() != null && faa.getFirstName().length() < 4) {
            errors.rejectValue("firstName",
                    "field.length",
                    new Object[] { Integer.valueOf("4") },
                    "Minimum length of four characters");
        }

        if (faa.getLastName() != null && faa.getLastName().length() < 4) {
            errors.rejectValue("lastName",
                    "field.length",
                    new Object[] { Integer.valueOf("4") },
                    "Minimum length of four characters");
        }

        if(!faa.getForgotAuthenticateForm().isEqual()) {
            errors.rejectValue("forgotAuthenticateForm.password", "field.unmatched", new Object[] { "" }, "Password entered value does not match");
            errors.rejectValue("forgotAuthenticateForm.passwordSecond", "field.unmatched", new Object[] { "" }, "Password entered value does not match");
        }

        if (faa.getForgotAuthenticateForm().getPassword().length() < 4) {
            errors.rejectValue("forgotAuthenticateForm.password",
                    "field.length",
                    new Object[] { Integer.valueOf("4") },
                    "Minimum length of four characters");
        }

        if (faa.getForgotAuthenticateForm().getPasswordSecond().length() < 4) {
            errors.rejectValue("forgotAuthenticateForm.passwordSecond",
                    "field.length",
                    new Object[] { Integer.valueOf("4") },
                    "Minimum length of four characters");
        }
    }
}
