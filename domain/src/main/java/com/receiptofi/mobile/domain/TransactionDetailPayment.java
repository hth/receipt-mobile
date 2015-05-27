package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 5/26/15 5:07 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable",
        "unused"
})
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class TransactionDetailPayment implements TransactionDetail {

    @JsonProperty ("type")
    private final TYPE type;

    @JsonProperty ("success")
    private final boolean success;

    @JsonProperty ("status")
    private final String status;

    @JsonProperty ("firstName")
    private final String firstName;

    @JsonProperty ("lastName")
    private final String lastName;

    @JsonProperty ("postalCode")
    private final String postalCode;

    @JsonProperty ("accountPlanId")
    private final String accountPlanId;

    @JsonProperty ("transactionId")
    private final String transactionId;

    public TransactionDetailPayment(
            boolean success,
            String status,
            String firstName,
            String lastName,
            String postalCode,
            String accountPlanId,
            String transactionId
    ) {
        this.type = TYPE.PAY;
        this.success = success;
        this.status = status;
        this.firstName = firstName;
        this.lastName = lastName;
        this.postalCode = postalCode;
        this.accountPlanId = accountPlanId;
        this.transactionId = transactionId;
    }

    public boolean isSuccess() {
        return success;
    }
}
