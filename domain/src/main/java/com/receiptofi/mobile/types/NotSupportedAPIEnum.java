package com.receiptofi.mobile.types;

/**
 * User: hitender
 * Date: 1/19/17 12:00 PM
 */
public enum NotSupportedAPIEnum {

    V150("V150", true),
    V160("V160", false);

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
