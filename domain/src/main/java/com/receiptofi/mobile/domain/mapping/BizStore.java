package com.receiptofi.mobile.domain.mapping;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.receiptofi.domain.BizStoreEntity;

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
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class BizStore {

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("address")
    private String address;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
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
