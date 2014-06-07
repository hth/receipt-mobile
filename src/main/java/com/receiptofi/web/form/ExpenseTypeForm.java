package com.receiptofi.web.form;

import org.apache.commons.lang3.StringUtils;

/**
 * Used in adding new expense type
 *
 * User: hitender
 * Date: 7/26/13
 * Time: 7:17 PM
 */
public final class ExpenseTypeForm {
    private String tagName;

    public static ExpenseTypeForm newInstance() {
        return new ExpenseTypeForm();
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = StringUtils.trim(tagName);
    }
}
