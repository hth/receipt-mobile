package com.receiptofi.mobile.domain.mapping;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.receiptofi.domain.ItemEntity;

/**
 * User: hitender
 * Date: 9/11/14 12:06 AM
 */
@JsonAutoDetect (
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
@SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
public final class ReceiptItem {

    @JsonProperty ("id")
    private String id;

    @JsonProperty ("seq")
    private String seq;

    @JsonProperty ("name")
    private String name;

    @JsonProperty ("quant")
    private String quantity;

    @JsonProperty ("price")
    private String price;

    @JsonProperty ("tax")
    private String tax;

    @JsonProperty ("receiptId")
    private String receiptId;

    private ReceiptItem(ItemEntity item) {
        this.id = item.getId();
        this.seq = String.valueOf(item.getSequence());
        this.name = item.getName();
        this.quantity = String.valueOf(item.getQuantity());
        this.price = String.valueOf(item.getPrice());
        this.tax = String.valueOf(item.getTax());
        this.receiptId = item.getReceipt().getId();
    }

    public static ReceiptItem newInstance(ItemEntity item) {
        return new ReceiptItem(item);
    }
}
