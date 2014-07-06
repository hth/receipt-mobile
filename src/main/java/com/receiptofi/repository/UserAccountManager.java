package com.receiptofi.repository;

import com.receiptofi.domain.UserAccountEntity;

/**
 * User: hitender
 * Date: 4/23/14 6:43 AM
 */
public interface UserAccountManager extends RepositoryManager<UserAccountEntity> {
    UserAccountEntity findByReceiptUserId(String rid);
    UserAccountEntity findByUserId(String mail);
    UserAccountEntity findByProviderUserId(String providerUserId);
}
