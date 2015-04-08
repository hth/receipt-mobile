package com.receiptofi.mobile.service;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.annotation.Mobile;
import com.receiptofi.mobile.repository.ReceiptManagerMobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 4/7/15 7:45 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Component
public class ReceiptService {

    @Autowired private ReceiptManagerMobile receiptManagerMobile;

    /**
     * Do not use this open end query.
     *
     * @param profileId
     * @return
     */
    @Mobile
    @SuppressWarnings ("unused")
    public List<ReceiptEntity> getAllReceipts(String profileId) {
        return receiptManagerMobile.getAllReceipts(profileId);
    }

    @Mobile
    @SuppressWarnings ("unused")
    public List<ReceiptEntity> getAllUpdatedReceiptSince(String profileId, Date since) {
        return receiptManagerMobile.getAllUpdatedReceiptSince(profileId, since);
    }
}
