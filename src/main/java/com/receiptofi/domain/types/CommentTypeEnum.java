package com.receiptofi.domain.types;

/**
 * User: hitender
 * Date: 7/23/13
 * Time: 6:14 PM
 */
public enum CommentTypeEnum {
    NOTES("NOTES", "Notes"),
    RECHECK("RECHECK", "Recheck");

    private final String description;
    private final String name;

    private CommentTypeEnum(String name, String description) {
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
