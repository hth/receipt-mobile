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
        @CompoundIndex(name = "invite_email_idx",   def = "{'EMAIL': 0}", unique = false),
        @CompoundIndex(name = "invite_key_idx",     def = "{'AUTH' : 0}", unique = true)
} )
public final class InviteEntity extends BaseEntity {

    @NotNull
    @Field("EM")
    @Email
    private String emailId;

    @NotNull
    @Field("AUTH")
    private String authenticationKey;

    @DBRef
    @Field("USER_PROFILE_INVITED")
    private UserProfileEntity invited;

    @DBRef
    @Field("USER_PROFILE_INVITED_BY")
    private UserProfileEntity invitedBy;

    @NotNull
    @Field("COUNT")
    private int invitationCount;

    public static InviteEntity newInstance(String emailId, String authenticationKey, UserProfileEntity invited, UserProfileEntity invitedBy) {
        InviteEntity inviteEntity = new InviteEntity();

        inviteEntity.setEmailId(emailId);
        inviteEntity.setAuthenticationKey(authenticationKey);
        inviteEntity.setInvited(invited);
        inviteEntity.setInvitedBy(invitedBy);

        return inviteEntity;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
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

    public UserProfileEntity getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(UserProfileEntity invitedBy) {
        this.invitedBy = invitedBy;
    }

    public int getInvitationCount() {
        return invitationCount;
    }

    public void increaseInvitationCount() {
        this.invitationCount ++;
    }
}
