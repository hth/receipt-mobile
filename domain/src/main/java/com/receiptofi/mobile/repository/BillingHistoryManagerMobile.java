package com.receiptofi.mobile.repository;

import com.receiptofi.domain.BillingHistoryEntity;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 4/19/15 4:04 PM
 */
public interface BillingHistoryManagerMobile extends RepositoryManager<BillingHistoryEntity> {
    /**
     * Get history since last mobile device update.
     *
     * @param rid
     * @param since
     * @return
     */
    List<BillingHistoryEntity> getHistory(String rid, Date since);

    List<BillingHistoryEntity> getHistory(String rid);

    BillingHistoryEntity getHistory(String rid, String yyyyMM);

    void save(BillingHistoryEntity billingHistory);

    /**
     * Delete billingHistory that have blank transactionId.
     *
     * @param billingHistory
     * @return
     */
    void deleteHard(BillingHistoryEntity billingHistory);
}
