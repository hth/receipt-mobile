package com.receiptofi.mobile.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Shows user has access.
 * User: hitender
 * Date: 6/9/14 12:06 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable",
        "PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal"
})
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class UserAccess {

    @SuppressWarnings ({"unused"})
    @JsonProperty ("access")
    private String access;

    private UserAccess(String access) {
        this.access = access;
    }

    public static UserAccess newInstance(String access) {
        return new UserAccess(access);
    }

    public String getAccess() {
        return access;
    }
}
