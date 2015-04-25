package com.receiptofi.mobile.repository;

import com.receiptofi.domain.ExpenseTagEntity;

/**
 * User: hitender
 * Date: 4/25/15 3:30 AM
 */
public interface ExpenseTagManagerMobile extends RepositoryManager<ExpenseTagEntity> {
    ExpenseTagEntity getExpenseTag(String rid, String tagId, String tagName);
}
