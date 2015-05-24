package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.springframework.util.Assert;

/**
 * User: hitender
 * Date: 5/23/15 9:29 PM
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
public class BraintreeToken implements Token {

    @SuppressWarnings ({"unused"})
    @JsonProperty ("token")
    private String token;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("hasCustomerInfo")
    private boolean hasCustomerInfo;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("firstName")
    private String firstName;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("lastName")
    private String lastName;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("postalCode")
    private String postalCode;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("planId")
    private String planId;

    public BraintreeToken(String token) {
        Assert.hasText(token, "Client token is empty");
        this.token = token;
    }

    public void setHasCustomerInfo(boolean hasCustomerInfo) {
        this.hasCustomerInfo = hasCustomerInfo;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getToken() {
        return token;
    }
}
