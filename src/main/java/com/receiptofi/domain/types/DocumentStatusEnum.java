/**
 *
 */
package com.receiptofi.domain.types;

/**
 * @author hitender
 * @since Jan 5, 2013 7:37:02 PM
 */
public enum DocumentStatusEnum {

    /** Note: Do not change the order. New ENUM should be appended at the bottom */
    OCR_PROCESSED("PROCESSED",                      "OCR Processed"),
    TURK_PROCESSED("TURK_PROCESSED",                "Turk Processed"),
    TURK_REQUEST("TURK_REQUEST",                    "Turk Request"),

    /** Reject condition when its hard to process a receipts because of the image quality or invalid image */
    TURK_RECEIPT_REJECT("TURK_RECEIPT_REJECT",      "Turk Receipt Reject"),
    TURK_RECEIPT_DUPLICATE("TURK_RECEIPT_DUPLICATE","Turk Receipt Duplicate");

    private final String description;
    private final String name;

    private DocumentStatusEnum(String name, String description) {
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
