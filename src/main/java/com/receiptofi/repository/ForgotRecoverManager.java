package com.receiptofi.repository;

import com.receiptofi.domain.ForgotRecoverEntity;

/**
 * User: hitender
 * Date: 6/4/13
 * Time: 12:10 AM
 */
public interface ForgotRecoverManager extends RepositoryManager<ForgotRecoverEntity> {

    /**
     * Find ForgotRecoverEntity by authentication key
     *
     * @param key
     * @return
     */
    ForgotRecoverEntity findByAuthenticationKey(String key);

    /**
     * Make all the existing request invalid
     *
     * @param receiptUserId
     */
    void invalidateAllEntries(String receiptUserId);
}
