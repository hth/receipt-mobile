package com.receiptofi.mobile.domain;

import java.util.ArrayList;
import java.util.List;

import com.receiptofi.domain.ReceiptEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 8/10/14 3:57 PM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
public final class AvailableAccountUpdates extends AbstractDomain {

    @SuppressWarnings("unused")
    @JsonProperty ("receipts")
    private List<ReceiptEntity> receipts = new ArrayList<>();

    public static AvailableAccountUpdates newInstance() {
        return new AvailableAccountUpdates();
    }

    public void addReceipts(List<ReceiptEntity> receipts) {
        this.receipts.addAll(receipts);
    }
}
