package com.receiptofi.domain.types;

/**
 * User: hitender
 * Date: 12/13/13 6:44 PM
 */
public enum DocumentOfTypeEnum {
    RECEIPT("RECEIPT", "Receipt"),
    INVOICE("INVOICE", "Invoice"),
    MILEAGE("MILEAGE", "Mileage");

    private final String description;
    private final String name;

    private DocumentOfTypeEnum(String name, String description) {
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
