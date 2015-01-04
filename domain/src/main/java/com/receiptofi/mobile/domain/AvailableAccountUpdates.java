package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.json.JsonReceipt;
import com.receiptofi.domain.json.JsonReceiptItem;

import java.util.LinkedList;
import java.util.List;

/**
 * All account updates available.
 * User: hitender
 * Date: 8/10/14 3:57 PM
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
public class AvailableAccountUpdates extends AbstractDomain {

    @SuppressWarnings ({"unused"})
    @JsonProperty ("receipts")
    private List<JsonReceipt> jsonReceipts = new LinkedList<>();

    @SuppressWarnings ({"unused"})
    @JsonProperty ("items")
    private List<JsonReceiptItem> jsonReceiptItems = new LinkedList<>();

    @SuppressWarnings ({"unused"})
    @JsonProperty ("profile")
    private Profile profile;

    //TODO add expense Tag

    public static AvailableAccountUpdates newInstance() {
        return new AvailableAccountUpdates();
    }

    public List<JsonReceipt> getJsonReceipts() {
        return jsonReceipts;
    }

    public void setJsonReceipts(List<ReceiptEntity> receipts) {
        for (ReceiptEntity receiptEntity : receipts) {
            this.jsonReceipts.add(new JsonReceipt(receiptEntity));
        }
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(UserProfileEntity userProfile) {
        this.profile = Profile.newInstance(userProfile);
    }

    public List<JsonReceiptItem> getJsonReceiptItems() {
        return jsonReceiptItems;
    }

    public void addJsonReceiptItems(ItemEntity item) {
        this.jsonReceiptItems.add(JsonReceiptItem.newInstance(item));
    }

    public void addJsonReceiptItems(List<ItemEntity> items) {
        for(ItemEntity item : items) {
            this.jsonReceiptItems.add(JsonReceiptItem.newInstance(item));
        }
    }
}
