package com.receiptofi.mobile.domain.mapping;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.receiptofi.domain.UserProfileEntity;

/**
 * User: hitender
 * Date: 8/24/14 9:35 PM
 */
@JsonAutoDetect (
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class Profile {

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("rid")
    private String receiptUserId;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("name")
    private String name;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("firstName")
    private String firstName;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("lastName")
    private String lastName;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("mail")
    private String mail;

    private Profile(String receiptUserId, String name, String firstName, String lastName, String mail) {
        this.receiptUserId = receiptUserId;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
    }

    public static Profile newInstance(UserProfileEntity userProfile) {
        return new Profile(
                userProfile.getReceiptUserId(),
                userProfile.getName(),
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getEmail()
        );
    }
}
