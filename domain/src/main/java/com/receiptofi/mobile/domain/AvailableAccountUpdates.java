package com.receiptofi.mobile.domain;

import java.util.LinkedList;
import java.util.List;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.domain.mapping.Profile;
import com.receiptofi.mobile.domain.mapping.Receipt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 8/10/14 3:57 PM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public final class AvailableAccountUpdates extends AbstractDomain {

    @SuppressWarnings("unused")
    @JsonProperty ("receipts")
    private List<Receipt> receipts = new LinkedList<>();

    @SuppressWarnings("unused")
    @JsonProperty ("profile")
    private Profile profile;

    public static AvailableAccountUpdates newInstance() {
        return new AvailableAccountUpdates();
    }

    public void setReceipts(List<ReceiptEntity> receipts) {
        for(ReceiptEntity receiptEntity : receipts) {
            this.receipts.add(Receipt.newInstance(receiptEntity));
        }
    }

    public void setProfile(UserProfileEntity userProfile) {
        this.profile = Profile.newInstance(userProfile);
    }
}
