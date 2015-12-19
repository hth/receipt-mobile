package com.receiptofi.mobile.service;

import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.service.SplitExpensesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 12/19/15 4:34 AM
 */
@Service
public class SplitExpensesMobileService {

    @Autowired
    private SplitExpensesService splitExpensesService;

    public void getJsonOwe(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.setOwes(splitExpensesService.getJsonOweExpenses(rid));
    }

    public void getJsonOwesOther(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.setOwesOther(splitExpensesService.getJsonOweOthersExpenses(rid));
    }
}
