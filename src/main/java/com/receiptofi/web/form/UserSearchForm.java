/**
 *
 */
package com.receiptofi.web.form;

import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.types.UserLevelEnum;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;

/**
 * @author hitender
 * @since Mar 26, 2013 3:52:26 PM
 *
 */
public final class UserSearchForm {

	private String id;
    private String receiptUserId;
	private String userName = StringUtils.EMPTY;
	private String firstName = StringUtils.EMPTY;
	private String lastName = StringUtils.EMPTY;
	private UserLevelEnum level;
    private String emailId;

	/** To make bean happy */
	private UserSearchForm() {}

    public static UserSearchForm newInstance() {
        return new UserSearchForm();
    }

	public static UserSearchForm newInstance(UserProfileEntity userProfile) {
		UserSearchForm userSearchForm = new UserSearchForm();

        userSearchForm.setId(userProfile.getId());
        userSearchForm.setReceiptUserId(userProfile.getReceiptUserId());
        userSearchForm.setFirstName(userProfile.getFirstName());
        userSearchForm.setLastName(userProfile.getLastName());
        userSearchForm.setUserName(userProfile.getFirstName() + ", " + userProfile.getLastName());
        userSearchForm.setLevel(userProfile.getLevel());
        userSearchForm.setEmailId(userProfile.getEmail());
        return userSearchForm;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    public String getReceiptUserId() {
        return receiptUserId;
    }

    public void setReceiptUserId(String receiptUserId) {
        this.receiptUserId = receiptUserId;
    }

    public void setUserName(String userName) {
		this.userName = userName;
	}

    /**
     * Not sure why this logic but it forces user toe enter more than two characters to find a specific user
     *
     * @return
     */
	public String getUserName() {
		if(!userName.equalsIgnoreCase(", ") && userName.length() > 2) {
			return userName;
		}
		return StringUtils.EMPTY;
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

	public UserLevelEnum getLevel() {
		return level;
	}

	public void setLevel(UserLevelEnum level) {
		this.level = level;
	}

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .add("level", level)
                .add("emailId", emailId)
                .toString();
    }
}
