package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.receiptofi.domain.types.BillingPlanEnum;
import com.receiptofi.domain.types.PaymentGatewayEnum;

import java.math.BigDecimal;

/**
 * User: hitender
 * Date: 5/10/15 6:27 PM
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
public class ReceiptofiPlan {

    @JsonProperty ("id")
    private String id;

    @JsonProperty ("name")
    private String name;

    @JsonProperty ("description")
    private String description;

    @JsonProperty ("price")
    private BigDecimal price;

    @JsonProperty ("billingFrequency")
    private int billingFrequency;

    @JsonProperty ("billingDayOfMonth")
    private int billingDayOfMonth;

    @JsonProperty ("paymentGateway")
    private PaymentGatewayEnum paymentGateway;

    @JsonProperty ("billingPlan")
    private BillingPlanEnum billingPlan;

    /**
     * Get plan id.
     *
     * @return
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.billingPlan = BillingPlanEnum.valueOf(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getBillingFrequency() {
        return billingFrequency;
    }

    public void setBillingFrequency(int billingFrequency) {
        this.billingFrequency = billingFrequency;
    }

    public int getBillingDayOfMonth() {
        return billingDayOfMonth;
    }

    public void setBillingDayOfMonth(int billingDayOfMonth) {
        this.billingDayOfMonth = billingDayOfMonth;
    }

    public PaymentGatewayEnum getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(PaymentGatewayEnum paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public BillingPlanEnum getBillingPlan() {
        return billingPlan;
    }
}
