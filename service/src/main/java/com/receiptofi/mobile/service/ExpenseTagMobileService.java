package com.receiptofi.mobile.service;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.repository.ExpenseTagManagerMobile;
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

    private DocumentMobileService documentMobileService;
    private ExpensesService expensesService;
    private ExpenseTagManagerMobile expenseTagManagerMobile;

    @Autowired
    public ExpenseTagMobileService(
            ExpensesService expensesService,
            DocumentMobileService documentMobileService,
            ExpenseTagManagerMobile expenseTagManagerMobile
    ) {
        this.expensesService = expensesService;
        this.documentMobileService = documentMobileService;
        this.expenseTagManagerMobile = expenseTagManagerMobile;
    }

    public List<ExpenseTagEntity> getExpenseTags(String rid) {
        return expensesService.getExpenseTags(rid);
    }

    public List<ExpenseTagEntity> getAllExpenseTags(String rid) {
        return expensesService.getAllExpenseTypes(rid);
    }

    public ExpenseTagEntity getExpenseTag(String rid, String tagId) {
        if (StringUtils.isNotBlank(tagId)) {
            return expensesService.getExpenseTag(rid, tagId);
        }
        return null;
    }

    public ExpenseTagEntity getExpenseTag(String rid, String tagId, String tagName) {
        if (StringUtils.isNotBlank(tagId) && StringUtils.isNotBlank(tagName)) {
            return expenseTagManagerMobile.getExpenseTag(rid, tagId, tagName);
        }
        return null;
    }

    public boolean doesExists(String rid, String tagName) {
        return expenseTagManagerMobile.doesExits(rid, tagName);
    }

    public ExpenseTagEntity save(String tagName, String rid, String tagColor) {
        ExpenseTagEntity expenseTag = ExpenseTagEntity.newInstance(tagName, rid, tagColor);
        expensesService.saveExpenseTag(expenseTag);
        return expenseTag;
    }

    public void update(String tagId, String tagName, String rid, String tagColor) {
        ExpenseTagEntity expenseTag = expensesService.getExpenseTag(rid, tagId);
        Assert.notNull(expenseTag, "Expense Tag does not exists.");
        expensesService.updateExpenseTag(tagId, tagName, tagColor, rid);
    }

    public AvailableAccountUpdates getUpdates(String rid) {
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        getAllExpenseTags(rid, availableAccountUpdates);
        documentMobileService.getUnprocessedDocuments(rid, availableAccountUpdates);
        return availableAccountUpdates;
    }

    public void getAllExpenseTags(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.addJsonExpenseTag(getAllExpenseTags(rid));
    }

    public boolean softDeleteExpenseTag(String expenseTypeId, String expenseTagName, String rid) {
        return expensesService.softDeleteExpenseTag(expenseTypeId, expenseTagName, rid);
    }
}
