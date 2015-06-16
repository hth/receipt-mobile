package com.receiptofi.mobile.repository;

import com.receiptofi.domain.BillingAccountEntity;

/**
 * User: hitender
 * Date: 4/19/15 3:58 PM
 */
public interface BillingAccountManagerMobile extends RepositoryManager<BillingAccountEntity> {

    BillingAccountEntity getLatestBillingAccount(String rid);

    void save(BillingAccountEntity billingAccount);
}
