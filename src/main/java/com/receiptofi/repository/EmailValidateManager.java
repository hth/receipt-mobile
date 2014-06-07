package com.receiptofi.repository;

import com.receiptofi.domain.EmailValidateEntity;
import com.receiptofi.domain.UserProfileEntity;

/**
 * User: hitender
 * Date: 5/17/14 6:29 PM
 */
public interface EmailValidateManager extends RepositoryManager<EmailValidateEntity> {
    EmailValidateEntity findByAuthenticationKey(String auth);
    void invalidateAllEntries(EmailValidateEntity object);
    EmailValidateEntity reInviteActiveInvite(String email, UserProfileEntity invitedBy);
    EmailValidateEntity find(String email);
}
