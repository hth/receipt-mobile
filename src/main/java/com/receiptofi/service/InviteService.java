package com.receiptofi.service;

import com.receiptofi.domain.InviteEntity;
import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.repository.InviteManager;
import com.receiptofi.repository.UserAccountManager;
import com.receiptofi.repository.UserProfileManager;
import com.receiptofi.utils.HashText;
import com.receiptofi.utils.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 6/9/13
 * Time: 2:06 PM
 */
@Service
public final class InviteService {
    private static Logger log = LoggerFactory.getLogger(InviteService.class);

    private final AccountService accountService;
    private final InviteManager inviteManager;
    private final UserProfileManager userProfileManager;
    private final UserAccountManager userAccountManager;

    @Autowired
    public InviteService(
            AccountService accountService,
            InviteManager inviteManager,
            UserProfileManager userProfileManager,
            UserAccountManager userAccountManager
    ) {
        this.accountService = accountService;
        this.inviteManager = inviteManager;
        this.userProfileManager = userProfileManager;
        this.userAccountManager = userAccountManager;
    }

    /**
     * @param emailId
     * @param userProfile
     * @return
     */
    public InviteEntity initiateInvite(String emailId, UserProfileEntity userProfile) {
        InviteEntity inviteEntity;
        UserAccountEntity userAccount;
        try {
            //First save is performed
            userAccount = accountService.executeCreationOfNewAccount(
                    emailId,
                    "",
                    "",
                    RandomString.newInstance(8).nextString()
            );
        } catch (RuntimeException exception) {
            log.error("Error occurred during creation of invited user: " + exception.getLocalizedMessage());
            throw exception;
        }

        //Updating the record as inactive until user completes registration
        userAccount.inActive();
        userAccountManager.save(userAccount);

        UserProfileEntity newInvitedUser = userProfileManager.findByReceiptUserId(userAccount.getReceiptUserId());
        String authenticationKey = HashText.computeBCrypt(RandomString.newInstance().nextString());
        inviteEntity = InviteEntity.newInstance(emailId, authenticationKey, newInvitedUser, userProfile);
        inviteManager.save(inviteEntity);
        return inviteEntity;
    }

    /**
     * Re-Invite only when the invite is active
     *
     * @param emailId
     * @return
     */
    public InviteEntity reInviteActiveInvite(String emailId, UserProfileEntity userProfile) {
        return inviteManager.reInviteActiveInvite(emailId, userProfile);
    }

    public InviteEntity find(String emailId) {
        return inviteManager.find(emailId);
    }

    public InviteEntity findInviteAuthenticationForKey(String key) {
        return inviteManager.findByAuthenticationKey(key);
    }

    public void invalidateAllEntries(InviteEntity inviteEntity) {
        inviteManager.invalidateAllEntries(inviteEntity);
    }
}
