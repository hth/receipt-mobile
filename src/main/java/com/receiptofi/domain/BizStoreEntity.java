package com.receiptofi.domain;

import com.receiptofi.utils.Formatter;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.NumberFormat;

/**
 * User: hitender
 * Date: 4/22/13
 * Time: 10:16 PM
 */
@Document(collection = "BIZ_STORE")
@CompoundIndexes(value = {
        @CompoundIndex(name = "biz_store_idx", def = "{'ADDRESS': 1, 'PHONE': 1}", unique=true),
} )
public final class BizStoreEntity extends BaseEntity {

    /** Better to add a BLANK PHONE then to add nothing when biz does not have a phone number */
    @Value("${phoneNumberBlank:000_000_0000}")
    private String phoneNumberBlank;

    @NotNull
    @Field("ADDRESS")
    private String address;

    @NotNull
    @Field("PHONE")
    private String phone;

    @NumberFormat(style = NumberFormat.Style.NUMBER)
    @Field("LAT")
    private double lat;

    @NumberFormat(style = NumberFormat.Style.NUMBER)
    @Field("LNG")
    private double lng;

    @DBRef
    @Field("BIZ_NAME")
    private BizNameEntity bizName;

    /* To make bean happy */
    public BizStoreEntity() {}

    public static BizStoreEntity newInstance() {
        return new BizStoreEntity();
    }

    /**
     * For web display of the address
     *
     * @return
     */
    @Transient
    public String getAddressWrapped() {
        return address.replaceFirst(",", "<br/>");
    }

    public String getAddressWrappedMore() {
        return getAddressWrapped().replaceFirst(",", "<br/>");
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = StringUtils.strip(address);
    }

    public String getPhone() {
        return phone;
    }

    public String getPhoneFormatted() {
        return Formatter.phone(phone);
    }

    /**
     * Remove everything other than numbers. Do the formatting on client side
     *
     * @param phone
     */
    public void setPhone(String phone) {
        if(StringUtils.isEmpty(phone)) {
            this.phone = phoneCleanup(phoneNumberBlank);
        } else {
            this.phone = phoneCleanup(phone);
        }
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public BizNameEntity getBizName() {
        return bizName;
    }

    public void setBizName(BizNameEntity bizName) {
        this.bizName = bizName;
    }

    /**
     * Strip all the characters other than number
     *
     * @param phone
     * @return
     */
    public static String phoneCleanup(String phone) {
        if(StringUtils.isNotEmpty(phone)) {
            return phone.replaceAll("[^0-9]", "");
        }
        return phone;
    }
}
