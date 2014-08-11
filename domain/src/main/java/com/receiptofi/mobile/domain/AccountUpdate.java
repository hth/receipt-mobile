package com.receiptofi.mobile.domain;

import com.receiptofi.domain.RecentActivityEntity;
import com.receiptofi.domain.types.RecentActivityEnum;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 8/10/14 1:51 PM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
public final class AccountUpdate extends AbstractDomain {
    private static DateTimeFormatter DATE_FMT = ISODateTimeFormat.dateTime();

    @SuppressWarnings ("unused")
    @JsonProperty ("update")
    private RecentActivityEnum recentActivity;

    @SuppressWarnings ("unused")
    @JsonProperty ("earliest")
    private String earliestUpdate;

    private AccountUpdate(RecentActivityEntity recentActivityEntity) {
        this.recentActivity = recentActivityEntity.getRecentActivity();
        this.earliestUpdate = DATE_FMT.print(recentActivityEntity.getEarliestUpdate().getTime());
    }

    public static AccountUpdate newInstance(RecentActivityEntity recentActivityEntity) {
        return new AccountUpdate(recentActivityEntity);
    }
}
