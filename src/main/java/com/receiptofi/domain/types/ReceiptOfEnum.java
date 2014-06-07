package com.receiptofi.domain.types;

/**
 * User: hitender
 * Date: 7/12/13
 * Time: 9:06 PM
 */
//TODO should be deleted
@Deprecated
public enum ReceiptOfEnum {
    INCOME("INCOME", "Income"),
    EXPENSE("EXPENSE", "Expense");

    private final String description;
    private final String name;

    private ReceiptOfEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
