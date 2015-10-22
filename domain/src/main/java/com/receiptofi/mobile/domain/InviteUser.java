package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.google.gson.annotations.SerializedName;

/**
 * User: hitender
 * Date: 10/22/15 12:16 AM
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
public class InviteUser extends AbstractDomain {
    @SuppressWarnings ({"unused"})
    @SerializedName ("rid")
    private String rid;

    @SuppressWarnings ({"unused"})
    @SerializedName ("inviteEmail")
    private String inviteEmail;

    private InviteUser(String inviteEmail, String rid) {
        super();
        this.inviteEmail = inviteEmail;
        this.rid = rid;
    }

    public static InviteUser newInstance(String inviteEmail, String rid) {
        return new InviteUser(inviteEmail, rid);
    }
}
