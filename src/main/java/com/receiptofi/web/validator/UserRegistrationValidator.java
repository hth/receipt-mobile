/**
 *
 */
package com.receiptofi.web.validator;

import com.receiptofi.web.form.UserRegistrationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author hitender
 * @since Dec 25, 2012 12:17:57 PM
 *
 */
@Component
public final class UserRegistrationValidator implements Validator {
	private static final Logger log = LoggerFactory.getLogger(UserRegistrationValidator.class);

	public static final String EMAIL_REGEX = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";

	@Override
	public boolean supports(Class<?> clazz) {
		return UserRegistrationForm.class.equals(clazz);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		log.debug("Executing validation");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required", new Object[] { "First Name" });
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "field.required", new Object[] { "Last Name" });
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailId", "field.required", new Object[] { "Email ID" });
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required", new Object[] { "Password" });

		UserRegistrationForm userRegistration = (UserRegistrationForm) obj;
		if (userRegistration.getFirstName() != null && userRegistration.getFirstName().length() < 4) {
			errors.rejectValue("firstName",
                    "field.length",
                    new Object[] { Integer.valueOf("4") },
                    "Minimum length of four characters");
		}

		if (userRegistration.getLastName() != null && userRegistration.getLastName().length() < 4) {
			errors.rejectValue("lastName",
                    "field.length",
                    new Object[] { Integer.valueOf("4") },
                    "Minimum length of four characters");
		}

		if (userRegistration.getEmailId() != null && !userRegistration.getEmailId().matches(EMAIL_REGEX)) {
			errors.rejectValue("emailId",
                    "field.email.address.not.valid",
                    new Object[] { userRegistration.getEmailId() },
                    "Email Address provided is not valid");
		}

		if (userRegistration.getPassword() != null && userRegistration.getPassword().length() < 4) {
			errors.rejectValue("password",
                    "field.length",
                    new Object[] { Integer.valueOf("4") },
                    "Minimum length of four characters");
		}
	}

    public void accountExists(Object obj, Errors errors) {
        UserRegistrationForm userRegistration = (UserRegistrationForm) obj;
        errors.rejectValue("emailId",
                "emailId.already.registered",
                new Object[] { userRegistration.getEmailId() },
                "Account already registered with this Email");
    }
}
