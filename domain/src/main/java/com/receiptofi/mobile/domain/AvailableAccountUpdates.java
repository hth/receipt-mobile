package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.json.JsonProfile;
import com.receiptofi.domain.json.JsonReceipt;

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
    @JsonProperty ("profile")
    private JsonProfile jsonProfile;

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

    public JsonProfile getJsonProfile() {
        return jsonProfile;
    }

    public void setJsonProfile(UserProfileEntity userProfile) {
        this.jsonProfile = JsonProfile.newInstance(userProfile);
    }
}
