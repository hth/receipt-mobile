/**
 *
 */
package com.receiptofi.web.form;

import org.apache.commons.lang3.StringUtils;

/**
 * @author hitender
 * @since Jan 4, 2013 4:41:01 PM
 *
 */
public final class UserLoginForm {

	private String emailId;
	private String password;

    private UserLoginForm() {}

	public static UserLoginForm newInstance() {
		return new UserLoginForm();
	}

	public String getEmailId() {
		return StringUtils.lowerCase(emailId);
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
