package com.receiptofi.mobile.types;

/**
 * User: hitender
 * Date: 1/19/17 12:00 PM
 */
public enum NotSupportedAPIEnum {

    V150("1.5.0", false),
    V160("1.6.0", false);

    private String version;
    private boolean notSupported;

    NotSupportedAPIEnum(String version, boolean notSupported) {
        this.version = version;
        this.notSupported = notSupported;
    }

    public String getVersion() {
        return version;
    }

    public boolean isNotSupported() {
        return notSupported;
    }
}
