package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.google.gson.annotations.SerializedName;

/**
 * User: hitender
 * Date: 7/1/14 2:18 AM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public final class ProviderAndAccessToken {

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @SerializedName ("pid")
    private String providerId;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @SerializedName ("at")
    private String accessToken;

    private ProviderAndAccessToken(String providerId, String accessToken) {
        this.providerId = providerId;
        this.accessToken = accessToken;
    }

    public static ProviderAndAccessToken newInstance(String provider, String accessToken) {
        return new ProviderAndAccessToken(provider, accessToken);
    }
}
