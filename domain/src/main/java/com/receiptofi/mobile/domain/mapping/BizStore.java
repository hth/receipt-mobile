package com.receiptofi.mobile.domain.mapping;

import com.receiptofi.domain.BizStoreEntity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 8/25/14 12:17 AM
 */
@JsonAutoDetect (
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
@JsonInclude (JsonInclude.Include.NON_NULL)
public final class BizStore {

    @SuppressWarnings("unused")
    @JsonProperty ("address")
    private String address;

    @SuppressWarnings("unused")
    @JsonProperty ("phone")
    private String phone;

    private BizStore(BizStoreEntity bizStoreEntity) {
        this.address = bizStoreEntity.getAddress();
        this.phone = bizStoreEntity.getPhoneFormatted();
    }

    public static BizStore newInstance(BizStoreEntity bizStoreEntity) {
        return new BizStore(bizStoreEntity);
    }
}
