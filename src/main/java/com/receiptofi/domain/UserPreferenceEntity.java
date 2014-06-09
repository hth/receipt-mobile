/**
 *
 */
package com.receiptofi.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author hitender
 * @since Dec 23, 2012 1:48:36 AM
 */
@Document(collection = "USER_PREFERENCE")
public final class UserPreferenceEntity extends BaseEntity {

    @DBRef
    @Indexed(unique = true)
    @Field("USER_PROFILE")
    private UserProfileEntity userProfile;

    /**
     * To make bean happy
     */
    @SuppressWarnings("unused")
    private UserPreferenceEntity() {

    }

    // @PersistenceConstructor
    private UserPreferenceEntity(UserProfileEntity userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * This method is used when the Entity is created for the first time.
     *
     * @param userProfile
     * @return
     */
    public static UserPreferenceEntity newInstance(UserProfileEntity userProfile) {
        return new UserPreferenceEntity(userProfile);
    }

    public UserProfileEntity getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfileEntity userProfile) {
        this.userProfile = userProfile;
    }
}
