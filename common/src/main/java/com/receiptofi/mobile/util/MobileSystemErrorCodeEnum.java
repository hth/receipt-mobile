package com.receiptofi.mobile.util;

/**
 * Error code to share between APP and Mobile API
 * User: hitender
 * Date: 7/10/14 11:28 PM
 */
public enum MobileSystemErrorCodeEnum {

    USER_INPUT("100"),
    API_WARNING("200"),
    DOCUMENT_UPLOAD("300"),
    AUTHENTICATION("400"),
    SEVERE("500");

    private String code;

    private MobileSystemErrorCodeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
