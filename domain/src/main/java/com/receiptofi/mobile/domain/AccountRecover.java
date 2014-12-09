package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.google.gson.annotations.SerializedName;

/**
 * User: hitender
 * Date: 12/8/14 7:36 PM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class AccountRecover extends AbstractDomain {

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @SerializedName ("userId")
    private String userId;

    private AccountRecover(String userId) {
        this.userId = userId;
    }

    public static AccountRecover newInstance(String userId) {
        return new AccountRecover(userId);
    }
}
