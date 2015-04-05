package com.receiptofi.mobile.service;

import com.receiptofi.domain.CommentEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.types.CommentTypeEnum;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.service.CommentService;
import com.receiptofi.service.ReceiptService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;

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
public class MobileReceiptService {
    @Autowired private ReceiptService receiptService;
    @Autowired private CommentService commentService;
    @Autowired private DeviceService deviceService;

    public ReceiptEntity findReceipt(String receiptId, String rid) {
        return receiptService.findReceipt(receiptId, rid);
    }

    public void updateReceiptExpenseTag(ReceiptEntity receipt, String expenseTagId) {
        receiptService.updateReceiptExpenseTag(receipt, expenseTagId);
    }

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
    }

    public void reopen(String receiptId, String rid) throws Exception {
        receiptService.reopen(receiptId, rid);
    }

    public AvailableAccountUpdates getUpdateForChangedReceipt(ReceiptEntity receipt) {
        Assert.notNull(receipt, "ReceiptEntity should not be null");
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        deviceService.getReceiptAndItemUpdates(availableAccountUpdates, Collections.singletonList(receipt));
        return availableAccountUpdates;
    }
}
