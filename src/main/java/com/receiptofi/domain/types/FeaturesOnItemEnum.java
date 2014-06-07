/**
 *
 */
package com.receiptofi.domain.types;

/**
 * @author hitender
 * @since Dec 26, 2012 1:58:20 PM
 */
public enum FeaturesOnItemEnum {

    RETURN_120_DAY("RETURN_120_DAY",            "120 Days Return Notification"),
    RETURN_90_DAY("RETURN_90_DAY",              "90 Days Return Notification"),
    RETURN_60_DAY("RETURN_60_DAY",              "60 Days Return Notification"),
    RETURN_30_DAY("RETURN_30_DAY",              "30 Days Return Notification"),
    RETURN_15_DAY("RETURN_15_DAY",              "15 Days Return Notification"),
    PRICE_CHECK_30_DAY("PRICE_CHECK_30_DAY",    "30 Days Price Check");

    private final String description;
    private final String name;

    private FeaturesOnItemEnum(String name, String description) {
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
