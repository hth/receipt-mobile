package com.receiptofi.domain;

import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * User: hitender
 * Date: 6/9/13
 * Time: 2:06 PM
 */
@Document(collection = "INVITE")
@CompoundIndexes(value = {
        @CompoundIndex(name = "invite_email_idx",   def = "{'EM': 0}", unique = false),
        @CompoundIndex(name = "invite_key_idx",     def = "{'AU' : 0}", unique = true)
} )
public final class InviteEntity extends BaseEntity {

    @NotNull
    @Field("EM")
    @Email
    private String email;

    @NotNull
    @Field("AU")
    private String authenticationKey;

    @DBRef
    @Field("IN")
    private UserProfileEntity invited;

    @DBRef
    @Field("IN_BY")
    private UserAccountEntity invitedBy;

    public static InviteEntity newInstance(String email, String authenticationKey, UserProfileEntity invited, UserAccountEntity invitedBy) {
        InviteEntity inviteEntity = new InviteEntity();

        inviteEntity.setEmail(email);
        inviteEntity.setAuthenticationKey(authenticationKey);
        inviteEntity.setInvited(invited);
        inviteEntity.setInvitedBy(invitedBy);

        return inviteEntity;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAuthenticationKey() {
        return authenticationKey;
    }

    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    public UserProfileEntity getInvited() {
        return invited;
    }

    public void setInvited(UserProfileEntity invited) {
        this.invited = invited;
    }

    public UserAccountEntity getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(UserAccountEntity invitedBy) {
        this.invitedBy = invitedBy;
    }
}
