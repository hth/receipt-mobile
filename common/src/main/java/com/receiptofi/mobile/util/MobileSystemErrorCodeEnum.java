package com.receiptofi.mobile.util;

/**
 * Error code to share between APP and Mobile API.
 * User: hitender
 * Date: 7/10/14 11:28 PM
 */
public enum MobileSystemErrorCodeEnum {

    USER_INPUT("100"),

    /** Mobile data submitted. */
    MOBILE("200"),

    MOBILE_JSON("210"),

    DOCUMENT_UPLOAD("300"),

    AUTHENTICATION("400"),
    USER_EXISTING("410"),
    USER_NOT_FOUND("412"),
    REGISTRATION_TURNED_OFF("430"),

    /** Mobile application related issue. */
    SEVERE("500"),

    /** Not mobile web application. */
    WEB_APPLICATION("600");

    private String code;

    private MobileSystemErrorCodeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
