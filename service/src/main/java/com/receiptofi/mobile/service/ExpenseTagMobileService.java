package com.receiptofi.mobile.service;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.repository.ExpenseTagManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User: hitender
 * Date: 4/9/15 4:00 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Service
public class ExpenseTagMobileService {

    private ExpenseTagManager expenseTagManager;
    private ReceiptMobileService receiptMobileService;
    private DocumentMobileService documentMobileService;

    @Autowired
    public ExpenseTagMobileService(
            ExpenseTagManager expenseTagManager,
            ReceiptMobileService receiptMobileService,
            DocumentMobileService documentMobileService
    ) {
        this.expenseTagManager = expenseTagManager;
        this.receiptMobileService = receiptMobileService;
        this.documentMobileService = documentMobileService;
    }

    public List<ExpenseTagEntity> getExpenseTags(String rid) {
        return expenseTagManager.getExpenseTags(rid);
    }

    public ExpenseTagEntity getExpenseTag(String rid, String tagId) {
        return expenseTagManager.getExpenseTag(rid, tagId);
    }

    public boolean doesExists(String rid, String tagName) {
        return expenseTagManager.doesExits(rid, tagName);
    }

    public ExpenseTagEntity save(String tagName, String rid, String tagColor) {
        ExpenseTagEntity expenseTag = ExpenseTagEntity.newInstance(tagName, rid, tagColor);
        expenseTagManager.save(expenseTag);
        return expenseTag;
    }

    public AvailableAccountUpdates getUpdates(String rid) {
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        getExpenseTag(rid, availableAccountUpdates);
        documentMobileService.getUnprocessedDocuments(rid, availableAccountUpdates);
        return availableAccountUpdates;
    }

    public void getExpenseTag(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.addJsonExpenseTag(getExpenseTags(rid));
    }
}
