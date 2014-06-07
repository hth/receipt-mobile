/**
 *
 */
package com.receiptofi.domain;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author hitender
 * @since Dec 15, 2012 8:11:45 PM
 */
@Document(collection = "USER_AUTHENTICATION")
public final class UserAuthenticationEntity extends BaseEntity {

	@NotNull
    @Field("PASSWORD")
	private String password;

	@NotNull
    @Field("AUTH")
	private String authenticationKey;

    //TODO drop this column
    @NotNull
    @Field("G_PASSWORD")
    private String grandPassword;

	/**
	 * Required for Bean Instantiation
	 */
    @SuppressWarnings("unused")
	private UserAuthenticationEntity() {}

	/**
	 *
	 * @param password
	 */
	private UserAuthenticationEntity(String password, String authenticationKey) {
		this.password = password;
		this.authenticationKey = authenticationKey;
	}

	/**
	 * This method is used when the Entity is created for the first time.
	 *
	 * @param password
	 * @param authenticationKey - (password + time stamp) to HashCode this needs to go to OAuth
	 * @return
	 */
	public static UserAuthenticationEntity newInstance(String password, String authenticationKey) {
		return new UserAuthenticationEntity(password, authenticationKey);
	}

	public String getPassword() {
		return password;
	}

	public String getAuthenticationKey() {
		return authenticationKey;
	}

    public String getGrandPassword() {
        return grandPassword;
    }

    public void setGrandPassword(String grandPassword) {
        this.grandPassword = grandPassword;
    }
}
