/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.UserProfileEntity;

import java.util.List;

/**
 * @author hitender
 * @since Dec 23, 2012 3:45:26 AM
 *
 */
public interface UserProfileManager extends RepositoryManager<UserProfileEntity> {

	UserProfileEntity getObjectUsingUserAuthentication(UserAuthenticationEntity object);

	UserProfileEntity findByEmail(String email);

    UserProfileEntity findByReceiptUserId(String receiptUserId);

    UserProfileEntity findByUserId(String email);

	/**
	 * Used for searching user based on name. Search could be based on First Name or Last Name.
	 * The list is sorted based on First Name. Displayed with format First Name, Last Name.
	 * @param name
	 * @return
	 */
	List<UserProfileEntity> searchAllByName(String name);

    UserProfileEntity findOneByMail(String emailId);
}
