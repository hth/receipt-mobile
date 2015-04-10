package com.receiptofi.mobile.service;

import com.receiptofi.domain.ExpenseTagEntity;
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

    @Autowired
    public ExpenseTagMobileService(ExpenseTagManager expenseTagManager) {
        this.expenseTagManager = expenseTagManager;
    }

    public List<ExpenseTagEntity> getExpenseTags(String rid) {
        return expenseTagManager.getExpenseTags(rid);
    }

    public ExpenseTagEntity getExpenseTag(String rid, String expenseTagId) {
        return expenseTagManager.getExpenseTag(rid, expenseTagId);
    }
}
