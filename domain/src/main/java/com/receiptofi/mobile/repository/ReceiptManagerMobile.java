package com.receiptofi.mobile.repository;

import com.receiptofi.domain.ReceiptEntity;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 4/7/15 7:34 PM
 */
public interface ReceiptManagerMobile extends RepositoryManager<ReceiptEntity> {
    /**
     *
     * @param receiptUserId
     * @return
     */
    List<ReceiptEntity> getAllReceipts(String receiptUserId);

    /**
     * Gets all updated receipts since specified time
     *
     * @param receiptUserId
     * @param since
     * @return
     */
    List<ReceiptEntity> getAllUpdatedReceiptSince(String receiptUserId, Date since);

    /**
     * Gets recently processed receipts.
     *
     * @param limit
     * @return
     */
    List<ReceiptEntity> getRecentReceipts(int limit);

    /**
     * Used mostly for loading receipt by Id. Should be refrained from using as this query is not secure.
     *
     * @param id
     * @return
     */
    ReceiptEntity findReceipt(String id);
}
