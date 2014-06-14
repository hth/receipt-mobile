package com.receiptofi.mobile.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: hitender
 * Date: 6/9/14 12:06 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class UserAccess {

    @JsonProperty("access")
    private String access;

    private UserAccess(String access) {
        this.access = access;
    }

    public static UserAccess newInstance(String access) {
        return new UserAccess(access);
    }

    public String getAccess() {
        return this.access;
    }
}
