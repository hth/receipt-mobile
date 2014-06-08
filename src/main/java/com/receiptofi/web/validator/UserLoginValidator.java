/**
 *
 */
package com.receiptofi.web.validator;

import com.receiptofi.web.form.UserLoginForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author hitender
 * @since Dec 16, 2012 6:52:46 PM
 */
@Component
public final class UserLoginValidator implements Validator {
    @SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(UserLoginValidator.class);

	@Override
	public boolean supports(Class<?> clazz) {
		return UserLoginForm.class.equals(clazz);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailId", "field.required", new Object[] { "Email ID" });
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required", new Object[] { "Password" });

		UserLoginForm userLoginForm = (UserLoginForm) obj;
		if (!userLoginForm.getEmailId().matches(UserRegistrationValidator.EMAIL_REGEX)) {
			errors.rejectValue("emailId", "field.email.address.not.valid", new Object[] { userLoginForm.getEmailId() }, "Email Address provided is not valid");
		}

		if (userLoginForm.getPassword().length() < 4) {
			errors.rejectValue("password", "field.length", new Object[] { Integer.valueOf("4") }, "Minimum length of four characters");
		}
	}
}
