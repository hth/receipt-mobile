package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.json.JsonAwaitingAcceptance;
import com.receiptofi.domain.json.JsonBilling;
import com.receiptofi.domain.json.JsonExpenseTag;
import com.receiptofi.domain.json.JsonFriend;
import com.receiptofi.domain.json.JsonNotification;
import com.receiptofi.domain.json.JsonReceipt;
import com.receiptofi.domain.json.JsonReceiptItem;
import com.receiptofi.domain.json.JsonReceiptSplit;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All account updates available.
 * User: hitender
 * Date: 8/10/14 3:57 PM
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
public class AvailableAccountUpdates extends AbstractDomain {

    @JsonProperty ("receipts")
    private List<JsonReceipt> jsonReceipts = new LinkedList<>();

    @JsonProperty ("receiptSplits")
    private List<JsonReceiptSplit> jsonReceiptSplits = new ArrayList<>();

    @JsonProperty ("items")
    private List<JsonReceiptItem> jsonReceiptItems = new LinkedList<>();

    @JsonProperty ("profile")
    private Profile profile;

    @JsonProperty ("expenseTags")
    private List<JsonExpenseTag> jsonExpenseTags = new LinkedList<>();

    @JsonProperty ("unprocessedDocuments")
    private UnprocessedDocuments unprocessedDocuments;

    @JsonProperty ("notifications")
    private List<JsonNotification> jsonNotifications = new ArrayList<>();

    @JsonProperty ("billing")
    private JsonBilling jsonBilling;

    @JsonProperty ("friends")
    private Collection<JsonFriend> activeFriends = new ArrayList<>();

    @JsonProperty ("pendingFriends")
    private List<JsonAwaitingAcceptance> pendingFriends = new ArrayList<>();

    @JsonProperty ("awaitingFriends")
    private List<JsonAwaitingAcceptance> awaitingFriends = new ArrayList<>();

    public static AvailableAccountUpdates newInstance() {
        return new AvailableAccountUpdates();
    }

    public List<JsonReceipt> getJsonReceipts() {
        return jsonReceipts;
    }

    public void addJsonReceipts(List<ReceiptEntity> receipts) {
        this.jsonReceipts.addAll(receipts.stream().map(JsonReceipt::new).collect(Collectors.toList()));
    }

    public List<JsonReceiptSplit> getJsonReceiptSplits() {
        return jsonReceiptSplits;
    }

    public void addJsonReceiptSplits(JsonReceiptSplit jsonReceiptSplit) {
        Assert.notEmpty(jsonReceiptSplit.getSplits(), "Split list is empty for rdid=" + jsonReceiptSplit.getReceiptId());
        this.jsonReceiptSplits.add(jsonReceiptSplit);
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

    public void addJsonReceiptItems(List<ItemEntity> items) {
        this.jsonReceiptItems.addAll(items.stream().map(JsonReceiptItem::newInstance).collect(Collectors.toList()));
    }

    public List<JsonExpenseTag> getJsonExpenseTags() {
        return jsonExpenseTags;
    }

    public void addJsonExpenseTag(List<ExpenseTagEntity> expenseTags) {
        this.jsonExpenseTags.addAll(expenseTags.stream().map(JsonExpenseTag::newInstance).collect(Collectors.toList()));
    }

    public UnprocessedDocuments getUnprocessedDocuments() {
        return unprocessedDocuments;
    }

    public void setUnprocessedDocuments(long count) {
        this.unprocessedDocuments = UnprocessedDocuments.newInstance(count);
    }

    public List<JsonNotification> getJsonNotifications() {
        return jsonNotifications;
    }

    public void setJsonNotifications(List<NotificationEntity> notifications) {
        this.jsonNotifications.addAll(notifications.stream().map(JsonNotification::newInstance).collect(Collectors.toList()));
    }

    public JsonBilling getJsonBilling() {
        return jsonBilling;
    }

    public void setJsonBilling(JsonBilling jsonBilling) {
        this.jsonBilling = jsonBilling;
    }

    public Collection<JsonFriend> getActiveFriends() {
        return activeFriends;
    }

    public void setActiveFriends(Collection<JsonFriend> activeFriends) {
        this.activeFriends = activeFriends;
    }

    public List<JsonAwaitingAcceptance> getPendingFriends() {
        return pendingFriends;
    }

    public void setPendingFriends(List<JsonAwaitingAcceptance> pendingFriends) {
        this.pendingFriends = pendingFriends;
    }

    public List<JsonAwaitingAcceptance> getAwaitingFriends() {
        return awaitingFriends;
    }

    public void setAwaitingFriends(List<JsonAwaitingAcceptance> awaitingFriends) {
        this.awaitingFriends = awaitingFriends;
    }
}
