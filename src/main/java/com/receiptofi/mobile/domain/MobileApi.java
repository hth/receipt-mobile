package com.receiptofi.mobile.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: hitender
 * Date: 6/9/14 12:37 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class MobileApi {

    @JsonProperty("working")
    private boolean working;

    private MobileApi(boolean working) {
        this.working = working;
    }

    public static MobileApi newInstance(boolean working) {
        return new MobileApi(working);
    }
}
