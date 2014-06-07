/**
 *
 */
package com.receiptofi.web.controller.access;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.repository.UserProfileManager;
import com.receiptofi.service.LoginService;
import com.receiptofi.web.rest.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hitender
 * @since Mar 28, 2013 2:00:46 PM
 *
 */
public abstract class BaseController {
	private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    @Autowired private UserProfileManager userProfileManager;
    @Autowired private LoginService loginService;

    public String getAuth(String profileId) {
        log.debug("Find user with profileId: " + profileId);
        return getAuth(userProfileManager.findByReceiptUserId(profileId));
    }

    private String getAuth(UserProfileEntity userProfile) {
        UserAuthenticationEntity userAuthentication = loginService.findByReceiptUserId(userProfile.getReceiptUserId()).getUserAuthentication();
        return userAuthentication.getAuthenticationKey();
    }

    public UserProfileEntity authenticate(String profileId, String authKey) {
        if(isValid(profileId, authKey)) {
            UserProfileEntity userProfile = userProfileManager.findByReceiptUserId(profileId);
            UserAccountEntity userAccountEntity = loginService.findByReceiptUserId(userProfile.getReceiptUserId());
            if(checkAuthKey(authKey, userAccountEntity)) {
                return userProfile;
            }
            return null;
        }
        return null;
    }

    /**
     *
     * @param authKey
     * @param userAccount
     * @return
     */
    private boolean checkAuthKey(String authKey, UserAccountEntity userAccount) {
        return userAccount != null && authKey.equals(userAccount.getUserAuthentication().getAuthenticationKey());
    }

    /**
     * Validates if the Profile Id and Auth Key is not empty and valid as Object ID
     * @param profileId
     * @param authKey
     * @return
     */
    private boolean isValid(String profileId, String authKey) {
        return StringUtils.isNotEmpty(profileId) && StringUtils.isNotEmpty(authKey);
    }

    /**
     * Header for failure
     * @return
     */
    public Header getHeaderForProfileOrAuthFailure() {
        Header header = Header.newInstanceFailure();
        header.setStatus(Header.RESULT.AUTH_FAILURE);
        header.setMessage("Profile or Authorization key missing or invalid");
        return header;
    }
}
