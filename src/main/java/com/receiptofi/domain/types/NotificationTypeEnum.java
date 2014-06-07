package com.receiptofi.domain.types;

/**
 * User: hitender
 * Date: 7/3/13
 * Time: 8:20 PM
 */
public enum NotificationTypeEnum {

    MESSAGE("MESSAGE",                  "Message"),
    RECEIPT("RECEIPT",                  "Receipt"),
    INVOICE("INVOICE",                  "Invoice"),
    MILEAGE("MILEAGE",                  "Mileage"),
    DOCUMENT("DOCUMENT",                "Document"),
    EXPENSE_REPORT("EXPENSE_REPORT",    "Expense Report");

    private final String description;
    private final String name;

    private NotificationTypeEnum(String name, String description) {
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
