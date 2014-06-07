package com.receiptofi.web.form;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.ItemEntity;

import java.util.List;

/**
 * Used in displaying items with specific expense type
 *
 * User: hitender
 * Date: 5/24/13
 * Time: 1:20 AM
 */
public final class ExpenseForm {

    String name;
    List<ExpenseTagEntity> expenseTags;
    List<ItemEntity> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ExpenseTagEntity> getExpenseTags() {
        return expenseTags;
    }

    public void setExpenseTags(List<ExpenseTagEntity> expenseTags) {
        this.expenseTags = expenseTags;
    }

    public List<ItemEntity> getItems() {
        return items;
    }

    public void setItems(List<ItemEntity> items) {
        this.items = items;
    }
}
