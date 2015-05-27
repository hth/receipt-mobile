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
        "PMD.LongVariable"
})
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class TransactionDetailPayment implements TransactionDetail {

    @SuppressWarnings ({"unused"})
    @JsonProperty ("type")
    private TYPE type;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("success")
    private boolean success;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("status")
    private String status;

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
    @JsonProperty ("accountPlanId")
    private String accountPlanId;

    @SuppressWarnings ({"unused"})
    @JsonProperty ("transactionId")
    private String transactionId;

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
