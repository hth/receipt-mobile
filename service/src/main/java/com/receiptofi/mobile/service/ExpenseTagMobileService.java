package com.receiptofi.mobile.service;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.repository.ExpenseTagManager;
import com.receiptofi.service.ExpensesService;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
    private DocumentMobileService documentMobileService;
    private ExpensesService expensesService;

    @Autowired
    public ExpenseTagMobileService(
            ExpenseTagManager expenseTagManager,
            ReceiptMobileService receiptMobileService,
            DocumentMobileService documentMobileService
    ) {
        this.expenseTagManager = expenseTagManager;
        this.expensesService = expensesService;
        this.documentMobileService = documentMobileService;
    }

    public List<ExpenseTagEntity> getExpenseTags(String rid) {
        return expenseTagManager.getExpenseTags(rid);
    }

    public ExpenseTagEntity getExpenseTag(String rid, String tagId) {
        if (StringUtils.isNotBlank(tagId)) {
            return expenseTagManager.getExpenseTag(rid, tagId);
        }
        return null;
    }

    public boolean doesExists(String rid, String tagName) {
        return expenseTagManager.doesExits(rid, tagName);
    }

    public ExpenseTagEntity save(String tagName, String rid, String tagColor) {
        ExpenseTagEntity expenseTag = ExpenseTagEntity.newInstance(tagName, rid, tagColor);
        expenseTagManager.save(expenseTag);
        return expenseTag;
    }

    public void update(String tagName, String rid, String tagColor, String tagId) {
        ExpenseTagEntity expenseTag = expenseTagManager.getExpenseTag(rid, tagId);
        Assert.notNull(expenseTag, "Expense Tag does not exists.");
        expenseTagManager.updateExpenseTag(tagId, tagName, tagColor, rid);
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

    public void deleteExpenseTag(String expenseTypeId, String expenseTagName, String rid) {
        expensesService.deleteExpenseTag(expenseTypeId, expenseTagName, rid);
    }
}
