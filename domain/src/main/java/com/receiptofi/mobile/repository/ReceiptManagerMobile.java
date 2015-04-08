package com.receiptofi.mobile.repository;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.repository.ReceiptManager;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 4/7/15 7:34 PM
 */
public interface ReceiptManagerMobile extends ReceiptManager {
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
}
