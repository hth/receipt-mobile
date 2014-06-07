package com.receiptofi.domain;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * User: hitender
 * Date: 4/22/13
 * Time: 10:16 PM
 */
@Document(collection = "BIZ_NAME")
@CompoundIndexes(value = {
        @CompoundIndex(name = "biz_name_idx", def = "{'N': 1}",  unique = true),
} )
public final class BizNameEntity extends BaseEntity {

    @NotNull
    @Field("N")
    private String businessName;

    /* To make bean happy */
    public BizNameEntity() {}

    public static BizNameEntity newInstance() {
        return new BizNameEntity();
    }

    public String getBusinessName() {
        return businessName;
    }

    /**
     * Cannot: Added Capitalize Fully feature to business businessName as the businessName has to be matching with business style
     *
     * @param businessName
     */
    public void setBusinessName(String businessName) {
        //this.businessName = WordUtils.capitalize(WordUtils.capitalizeFully(StringUtils.strip(businessName)), '.', '(', ')');
        this.businessName = StringUtils.trim(businessName);
    }

    /**
     * Escape String for Java Script
     *
     * @return
     */
    public String getSafeJSBusinessName() {
        return StringEscapeUtils.escapeEcmaScript(businessName);
    }
}
