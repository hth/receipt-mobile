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
public class TransactionDetailSubscription implements TransactionDetail {

    @JsonProperty ("type")
    private TYPE type;

    @JsonProperty ("success")
    private boolean success;

    @JsonProperty ("status")
    private String status;

    @JsonProperty ("planId")
    private String planId;

    @JsonProperty ("firstName")
    private String firstName;

    @JsonProperty ("lastName")
    private String lastName;

    @JsonProperty ("postalCode")
    private String postalCode;

    @JsonProperty ("accountPlanId")
    private String accountPlanId;

    @JsonProperty ("transactionId")
    private String transactionId;

    /**
     *
     * @param success
     * @param status
     * @param planId
     * @param firstName
     * @param lastName
     * @param postalCode
     * @param accountPlanId
     * @param transactionId - Cancelled subscription Id
     */
    public TransactionDetailSubscription(
            boolean success,
            String status,
            String planId,
            String firstName,
            String lastName,
            String postalCode,
            String accountPlanId,
            String transactionId
    ) {
        this.type = TYPE.SUB;
        this.success = success;
        this.status = status;
        this.planId = planId;
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
