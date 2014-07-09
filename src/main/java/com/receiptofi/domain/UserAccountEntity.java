package com.receiptofi.domain;

import com.receiptofi.domain.types.ProviderEnum;
import com.receiptofi.domain.types.RoleEnum;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "USER_ACCOUNT")
@CompoundIndexes({
        @CompoundIndex(name = "user_account_role_idx", def = "{'UID': 1, 'PID': 1, 'RE': 1}", unique = true),
        @CompoundIndex(name = "user_account_primary_idx", def = "{'UID': 1, 'PID': 1, 'PUID': 1}", unique = true),
        @CompoundIndex(name = "user_account_rid_idx", def = "{'RID': 1}", unique = true)
})
public final class UserAccountEntity extends BaseEntity {

    /**
     * Unique Id throughout the system. This will never change.
     */
    @NotNull
    @Field("RID")
    private String receiptUserId;

    /**
     * This is set by third party and cannot be relied on
     * It could be either matching provider's Id or email
     */
    @NotNull
    @Field("UID")
    private String userId;

    @Field("PID")
    private ProviderEnum providerId;

    /**
     * Provider User Id matches id's from social provider
     */
    @Field("PUID")
    private String providerUserId;

    @Field("DN")
    private String displayName;

    @Field("PURL")
    private String profileUrl;

    @Field("IURL")
    private String imageUrl;

    @Field("AT")
    private String accessToken;

    @Field("SE")
    private String secret;

    @Field("RT")
    private String refreshToken;

    @Field("ET")
    private Long expireTime;

    @Field("FN")
    private String firstName;

    @Field("LN")
    private String lastName;

    @Field("RE")
    private Set<RoleEnum> roles = new LinkedHashSet<RoleEnum>() {{
        add(RoleEnum.ROLE_USER);
    }};

    @DBRef
    @Field("USER_AUTHENTICATION")
    private UserAuthenticationEntity userAuthentication;

    //TODO on email change this should be reset to false and validation process has to be redone
    @Field("AV")
    private boolean isAccountValidated;

    private UserAccountEntity(
            String receiptUserId,
            String userId,
            String firstName,
            String lastName,
            UserAuthenticationEntity userAuthentication
    ) {
        this.receiptUserId = receiptUserId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userAuthentication = userAuthentication;
    }

    public static UserAccountEntity newInstance(
            String receiptUserId,
            String userId,
            String firstName,
            String lastName,
            UserAuthenticationEntity userAuthentication
    ) {
        return new UserAccountEntity(receiptUserId, userId, firstName, lastName, userAuthentication);
    }

    public String getReceiptUserId() {
        return receiptUserId;
    }

    public void setReceiptUserId(String receiptUserId) {
        this.receiptUserId = receiptUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ProviderEnum getProviderId() {
        return providerId;
    }

    public void setProviderId(ProviderEnum providerId) {
        this.providerId = providerId;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<RoleEnum> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEnum> roles) {
        this.roles = roles;
    }

    public void addRole(RoleEnum role) {
        this.roles.add(role);
    }

    public UserAuthenticationEntity getUserAuthentication() {
        return userAuthentication;
    }

    public void setUserAuthentication(UserAuthenticationEntity userAuthentication) {
        this.userAuthentication = userAuthentication;
    }

    public boolean isAccountValidated() {
        return isAccountValidated;
    }

    public void setAccountValidated(boolean accountValidated) {
        isAccountValidated = accountValidated;
    }

    public String getName() {
        if(StringUtils.isNoneBlank(firstName, lastName)) {
            return firstName + " " + lastName;
        }
        if(StringUtils.isNotBlank(displayName)) {
            return displayName;
        }
        return userId;
    }

    @Override
    public String toString() {
        return "UserAccountEntity{" +
                "receiptUserId='" + receiptUserId + '\'' +
                ", userId='" + userId + '\'' +
                ", providerId=" + providerId +
                ", providerUserId='" + providerUserId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", secret='" + secret + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expireTime=" + expireTime +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roles=" + roles +
                ", userAuthentication=" + userAuthentication +
                ", isAccountValidated=" + isAccountValidated +
                '}';
    }
}