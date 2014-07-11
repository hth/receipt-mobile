package com.receiptofi.mobile.util;

/**
 * User: hitender
 * Date: 7/10/14 11:28 PM
 */
public enum SystemErrorCodeEnum {

    SEVERE(500);

    private int code;

    private SystemErrorCodeEnum(int code) {
        this.code = code;
    }
}
