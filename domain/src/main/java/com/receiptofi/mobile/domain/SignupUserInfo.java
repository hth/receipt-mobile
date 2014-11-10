package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.google.gson.annotations.SerializedName;

/**
 * User: hitender
 * Date: 11/8/14 2:28 AM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
public final class SignupUserInfo {

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @SerializedName ("userId")
    private String userId;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @SerializedName ("name")
    private String name;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @SerializedName ("auth")
    private String auth;

    private SignupUserInfo(String userId, String name, String auth) {
        this.userId = userId;
        this.name = name;
        this.auth = auth;
    }

    public static SignupUserInfo newInstance(String userId, String name, String auth) {
        return new SignupUserInfo(userId, name, auth);
    }
}
