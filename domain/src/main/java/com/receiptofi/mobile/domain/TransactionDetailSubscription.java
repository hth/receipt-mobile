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
    private final TYPE type;

    @JsonProperty ("success")
    private final boolean success;

    @JsonProperty ("status")
    private final String status;

    @JsonProperty ("planId")
    private final String planId;

    @JsonProperty ("firstName")
    private final String firstName;

    @JsonProperty ("lastName")
    private final String lastName;

    @JsonProperty ("postalCode")
    private final String postalCode;

    @JsonProperty ("accountPlanId")
    private final String accountPlanId;

    @JsonProperty ("subscriptionId")
    private final String subscriptionId;

    /**
     *
     * @param success
     * @param status
     * @param planId
     * @param firstName
     * @param lastName
     * @param postalCode
     * @param accountPlanId
     * @param subscriptionId - Cancelled subscription Id
     */
    public TransactionDetailSubscription(
            boolean success,
            String status,
            String planId,
            String firstName,
            String lastName,
            String postalCode,
            String accountPlanId,
            String subscriptionId
    ) {
        this.type = TYPE.SUB;
        this.success = success;
        this.status = status;
        this.planId = planId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.postalCode = postalCode;
        this.accountPlanId = accountPlanId;
        this.subscriptionId = subscriptionId;
    }

    public boolean isSuccess() {
        return success;
    }
}
