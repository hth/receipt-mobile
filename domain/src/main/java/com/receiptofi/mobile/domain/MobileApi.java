package com.receiptofi.mobile.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Response shown when mobile application is working.
 * User: hitender
 * Date: 6/9/14 12:37 PM
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
public class MobileApi {

    @SuppressWarnings ({"unused"})
    @JsonProperty ("working")
    private boolean working;

    private MobileApi(boolean working) {
        this.working = working;
    }

    public static MobileApi newInstance(boolean working) {
        return new MobileApi(working);
    }
}
