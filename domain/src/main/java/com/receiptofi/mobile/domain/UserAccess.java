package com.receiptofi.mobile.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Shows user has access.
 * User: hitender
 * Date: 6/9/14 12:06 PM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public final class UserAccess {

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("access")
    private String access;

    private UserAccess(String access) {
        this.access = access;
    }

    public static UserAccess newInstance(String access) {
        return new UserAccess(access);
    }
}
