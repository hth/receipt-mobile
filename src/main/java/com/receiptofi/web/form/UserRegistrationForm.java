/**
 *
 */
package com.receiptofi.web.form;

import org.apache.commons.lang3.StringUtils;

/**
 * @author hitender
 * @since Dec 25, 2012 12:01:53 PM
 */
public final class UserRegistrationForm {

    private String firstName;
    private String lastName;
    private String emailId;
    private String password;

    private UserRegistrationForm() {
    }

    public static UserRegistrationForm newInstance() {
        return new UserRegistrationForm();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * During registration make sure all the email ids are lowered case.
     *
     * @return
     */
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

    @Override
    public String toString() {
        return "UserRegistrationForm [firstName=" + firstName + ", lastName=" + lastName + ", emailId=" + emailId + ", password=" + password + "]";
    }
}
