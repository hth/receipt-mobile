/**
 *
 */
package com.receiptofi.domain.types;

/**
 * @author hitender
 * @since Dec 23, 2012 11:06:13 AM
 */
public enum AccountTypeEnum {

    PERSONAL("PERSONAL",                                "Personal"),
    PERSONAL_HOME_BUSINESS("PERSONAL_HOME_BUSINESS",    "Personal & Home Business"),
    SMALL_BUSINESS("SMALL_BUSINESS",                    "Small Business"),
    SMALL_BUSINESS_CLIENT("SMALL_BUSINESS_CLIENT",      "Business with multiple clients"),
    ENTERPRISE("ENTERPRISE",                            "Enterprise");

    private final String description;
    private final String name;

    private AccountTypeEnum(String name, String description) {
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
