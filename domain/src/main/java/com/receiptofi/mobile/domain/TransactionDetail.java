package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 5/23/15 9:10 PM
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
public class TransactionDetail {

    @SuppressWarnings ({"unused"})
    @JsonProperty ("success")
    private boolean success;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("planId")
    private String planId;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("transactionId")
    private String transactionId;

    public TransactionDetail(boolean success, String planId, String transactionId) {
        this.success = success;
        this.planId = planId;
        this.transactionId = transactionId;
    }

    public boolean isSuccess() {
        return success;
    }
}
