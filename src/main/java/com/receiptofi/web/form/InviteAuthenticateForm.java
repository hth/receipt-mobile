package com.receiptofi.web.form;

/**
 * User: hitender
 * Date: 6/9/13
 * Time: 5:04 PM
 */
public final class InviteAuthenticateForm {

    private String emailId;
    private String firstName;
    private String lastName;
    private ForgotAuthenticateForm forgotAuthenticateForm;

    private InviteAuthenticateForm() {
        forgotAuthenticateForm = ForgotAuthenticateForm.newInstance();
    }

    public static InviteAuthenticateForm newInstance() {
        return new InviteAuthenticateForm();
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
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

    public ForgotAuthenticateForm getForgotAuthenticateForm() {
        return forgotAuthenticateForm;
    }

    public void setForgotAuthenticateForm(ForgotAuthenticateForm forgotAuthenticateForm) {
        this.forgotAuthenticateForm = forgotAuthenticateForm;
    }
}
