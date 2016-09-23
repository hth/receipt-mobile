package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.receiptofi.domain.UserProfileEntity;

/**
 * User: hitender
 * Date: 8/24/14 9:35 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable",
        "unused"
})
@JsonAutoDetect (
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class Profile {

    @JsonProperty ("rid")
    private String receiptUserId;

    @JsonProperty ("name")
    private String name;

    @JsonProperty ("firstName")
    private String firstName;

    @JsonProperty ("lastName")
    private String lastName;

    @JsonProperty ("mail")
    private String mail;

    @JsonProperty ("cs")
    private String countryShortName;

    private Profile(String receiptUserId, String name, String firstName, String lastName, String mail, String countryShortName) {
        this.receiptUserId = receiptUserId;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.countryShortName = countryShortName;
    }

    public static Profile newInstance(UserProfileEntity userProfile) {
        return new Profile(
                userProfile.getReceiptUserId(),
                userProfile.getName(),
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getEmail(),
                userProfile.getCountryShortName()
        );
    }

    public String getReceiptUserId() {
        return receiptUserId;
    }
}
