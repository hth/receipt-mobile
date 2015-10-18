package com.receiptofi.mobile.service;

import com.receiptofi.domain.CommentEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.types.CommentTypeEnum;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.repository.ReceiptManagerMobile;
import com.receiptofi.service.CommentService;
import com.receiptofi.service.ItemService;
import com.receiptofi.service.ReceiptService;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 4/5/15 2:02 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Component
public class ReceiptMobileService {
    @Autowired private ReceiptService receiptService;
    @Autowired private CommentService commentService;
    @Autowired private DocumentMobileService documentMobileService;
    @Autowired private ReceiptManagerMobile receiptManagerMobile;
    @Autowired private ItemService itemService;

    public ReceiptEntity findReceipt(String receiptId, String rid) {
        return receiptService.findReceipt(receiptId, rid);
    }

    public ReceiptEntity findReceiptForMobile(String receiptId, String rid) {
        return receiptService.findReceiptForMobile(receiptId, rid);
    }

    public void updateReceiptExpenseTag(ReceiptEntity receipt, String expenseTagId) {
        receiptService.updateReceiptExpenseTag(receipt, expenseTagId);
    }

    /**
     * Saves the comment and updates receipts with comment.
     *
     * @param notes
     * @param receipt
     */
    public void saveComment(String notes, ReceiptEntity receipt) {
        CommentEntity comment = receipt.getNotes();
        if (null == comment) {
            comment = CommentEntity.newInstance(CommentTypeEnum.NOTES);
            comment.setText(notes);
        } else {
            comment.setText(notes);
        }
        commentService.save(comment);
        receipt.setNotes(comment);
        receiptService.save(receipt);
    }

    public void reopen(String receiptId, String rid) throws Exception {
        receiptService.reopen(receiptId, rid);
    }

    public AvailableAccountUpdates getUpdateForChangedReceipt(ReceiptEntity receipt) {
        Assert.notNull(receipt, "ReceiptEntity should not be null");
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        getReceiptAndItemUpdates(availableAccountUpdates, Collections.singletonList(receipt));
        documentMobileService.getUnprocessedDocuments(receipt.getReceiptUserId(), availableAccountUpdates);
        return availableAccountUpdates;
    }

    /**
     * Do not use this open end query.
     *
     * @param profileId
     * @return
     */
    public List<ReceiptEntity> getAllReceipts(String profileId) {
        return receiptManagerMobile.getAllReceipts(profileId);
    }

    public List<ReceiptEntity> getAllUpdatedReceiptSince(String profileId, Date since) {
        return receiptManagerMobile.getAllUpdatedReceiptSince(profileId, since);
    }

    /**
     * Gets item updates for the set of receipts.
     *
     * @param availableAccountUpdates
     * @param receipts
     */
    public void getReceiptAndItemUpdates(AvailableAccountUpdates availableAccountUpdates, List<ReceiptEntity> receipts) {
        if (!receipts.isEmpty()) {
            availableAccountUpdates.addJsonReceipts(receipts);
            for (ReceiptEntity receipt : receipts) {
                if (StringUtils.isBlank(receipt.getReferToReceiptId())) {
                    availableAccountUpdates.addJsonReceiptItems(itemService.getAllItemsOfReceipt(receipt.getId()));
                } else {
                    availableAccountUpdates.addJsonReceiptItems(itemService.getAllItemsOfReceipt(receipt.getReferToReceiptId()));
                }
            }
        }
    }
}
