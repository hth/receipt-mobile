package com.receiptofi.mobile.domain;

import com.google.gson.annotations.SerializedName;

/**
 * User: hitender
 * Date: 7/1/14 2:18 AM
 */
public final class ProviderAndAccessToken {

    @SerializedName("pid")
    private String providerId;

    @SerializedName("at")
    private String accessToken;

    private ProviderAndAccessToken(String providerId, String accessToken) {
        this.providerId = providerId;
        this.accessToken = accessToken;
    }

    public static ProviderAndAccessToken newInstance(String provider, String accessToken) {
        return new ProviderAndAccessToken(provider, accessToken);
    }
}
