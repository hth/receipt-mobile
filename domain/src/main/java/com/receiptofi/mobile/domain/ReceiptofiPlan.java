package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.receiptofi.domain.types.BillingProviderEnum;

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

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int billingFrequency;
    private int billingDayOfMonth;
    private BillingProviderEnum billingProvider;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public BillingProviderEnum getBillingProvider() {
        return billingProvider;
    }

    public void setBillingProvider(BillingProviderEnum billingProvider) {
        this.billingProvider = billingProvider;
    }
}
