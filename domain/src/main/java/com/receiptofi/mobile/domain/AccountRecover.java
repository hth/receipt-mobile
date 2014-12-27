package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.google.gson.annotations.SerializedName;

/**
 * User: hitender
 * Date: 12/8/14 7:36 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class AccountRecover extends AbstractDomain {

    @SuppressWarnings ({"unused"})
    @SerializedName ("userId")
    private String userId;

    private AccountRecover(String userId) {
        super();
        this.userId = userId;
    }

    public static AccountRecover newInstance(String userId) {
        return new AccountRecover(userId);
    }
}
