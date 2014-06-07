package com.receiptofi.web.form;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.UserPreferenceEntity;
import com.receiptofi.domain.UserProfileEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * User: hitender
 * Date: 8/11/13
 * Time: 7:31 PM
 */
public final class UserProfilePreferenceForm {

    private UserProfileEntity userProfile;
    private UserPreferenceEntity userPreference;
    private UserAuthenticationEntity userAuthentication;
    private List<ExpenseTagEntity> expenseTags;
    private Map<String, Long> expenseTagCount = new HashMap<>();
    private int visibleExpenseTags = 0;
    private boolean isActive = false;

    private String errorMessage;
    private String successMessage;

    private UserProfilePreferenceForm() {}

    public static UserProfilePreferenceForm newInstance() {
        return new UserProfilePreferenceForm();
    }

    public UserProfileEntity getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfileEntity userProfile) {
        this.userProfile = userProfile;
        this.isActive = userProfile.isActive();
    }

    public UserPreferenceEntity getUserPreference() {
        return userPreference;
    }

    public void setUserPreference(UserPreferenceEntity userPreference) {
        this.userPreference = userPreference;
    }

    public List<ExpenseTagEntity> getExpenseTags() {
        return expenseTags;
    }

    public void setExpenseTags(List<ExpenseTagEntity> expenseTags) {
        this.expenseTags = expenseTags;
    }

    public Map<String, Long> getExpenseTagCount() {
        return expenseTagCount;
    }

    public void setExpenseTagCount(Map<String, Long> expenseTagCount) {
        this.expenseTagCount = expenseTagCount;
    }

    public int getVisibleExpenseTags() {
        return visibleExpenseTags;
    }

    public void setVisibleExpenseTags(int visibleExpenseTags) {
        this.visibleExpenseTags = visibleExpenseTags;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        if(StringUtils.isEmpty(this.errorMessage))  {
            this.errorMessage = errorMessage;
        } else {
            this.errorMessage = this.errorMessage + ", " + errorMessage;
        }
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        if(StringUtils.isEmpty(this.successMessage)) {
            this.successMessage = successMessage;
        } else {
            this.successMessage = this.successMessage + ", " + successMessage;
        }
    }

    public UserAuthenticationEntity getUserAuthentication() {
        return userAuthentication;
    }

    public void setUserAuthentication(UserAuthenticationEntity userAuthentication) {
        this.userAuthentication = userAuthentication;
    }
}
