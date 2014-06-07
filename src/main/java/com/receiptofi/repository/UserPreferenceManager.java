/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.UserPreferenceEntity;
import com.receiptofi.domain.UserProfileEntity;

/**
 * @author hitender
 * @since Dec 24, 2012 3:19:07 PM
 *
 */
public interface UserPreferenceManager extends RepositoryManager<UserPreferenceEntity> {

	UserPreferenceEntity getObjectUsingUserProfile(UserProfileEntity userProfile);
}
