package com.receiptofi.mobile.domain;

/**
 * User: hitender
 * Date: 6/9/14 12:37 PM
 */
public final class MobileApi {
    boolean working;

    private MobileApi(boolean working) {
        this.working = working;
    }

    public static MobileApi newInstance(boolean working) {
        return new MobileApi(working);
    }

    public boolean isWorking() {
        return working;
    }
}
