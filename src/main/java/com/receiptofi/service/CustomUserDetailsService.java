package com.receiptofi.service;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.domain.types.ProviderEnum;
import com.receiptofi.domain.types.RoleEnum;
import com.receiptofi.repository.GenerateUserIdManager;
import com.receiptofi.social.annotation.Social;
import com.receiptofi.social.config.SocialConfig;
import com.receiptofi.social.connect.ConnectionService;
import com.receiptofi.utils.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.google.api.plus.Person;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * User: hitender
 * Date: 3/29/14 12:33 AM
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired private LoginService loginService;
    @Autowired private UserProfilePreferenceService userProfilePreferenceService;
    @Autowired private SocialConfig socialConfig;
    @Autowired private AccountService accountService;
    @Autowired private ConnectionService connectionService;
    @Autowired private GenerateUserIdManager generateUserIdManager;

    /**
     * @param email - lower case string
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("login through site, user={}", email);

        //Always check user login with lower letter email case
        UserProfileEntity userProfile = userProfilePreferenceService.findByEmail(email);
        if(userProfile == null) {
            log.warn("not found user={}", email);
            throw new UsernameNotFoundException("Error in retrieving user");
        } else {
            UserAccountEntity userAccountEntity = loginService.findByReceiptUserId(userProfile.getReceiptUserId());
            UserAuthenticationEntity userAuthenticate = userAccountEntity.getUserAuthentication();

            return new ReceiptUser(
                    userProfile.getEmail(),
                    userAuthenticate.getPassword(),
                    getAuthorities(userAccountEntity.getRoles()),
                    userProfile.getReceiptUserId(),
                    userProfile.getProviderId(),
                    userProfile.getLevel(),
                    userAccountEntity.isActive() && userAccountEntity.isAccountValidated()
            );
        }
    }

    @Social
    public UserDetails loadUserByUserId(String uid) throws UsernameNotFoundException {
        log.info("login through Provider user={}", uid);

        UserProfileEntity userProfile = userProfilePreferenceService.getUsingUserId(uid);
        if(userProfile == null) {
            log.warn("not found user={}", uid);
            throw new UsernameNotFoundException("Error in retrieving user");
        } else {
            UserAccountEntity userAccountEntity = loginService.findByReceiptUserId(userProfile.getReceiptUserId());
            UserAuthenticationEntity userAuthenticate = userAccountEntity.getUserAuthentication();

            //XXX Remove Todo some static password to be remove
            return new ReceiptUser(
                    StringUtils.isBlank(userAccountEntity.getUserId()) ? userProfile.getUserId() : userAccountEntity.getUserId(),
                    userAuthenticate == null ?
                            "$2a$12$Ce0mzNSijSvhAjGqfMKvx.SCQUqLHRQnTeOsKH9sphjC0XF3TA4Ge" :
                            userAuthenticate.getPassword(),
                    getAuthorities(userAccountEntity.getRoles()),
                    userProfile.getReceiptUserId(),
                    userProfile.getProviderId(),
                    userProfile.getLevel(),
                    userAccountEntity.isActive() && userAccountEntity.isAccountValidated()
            );
        }
    }

    @Social
    public String signInOrSignup(ProviderEnum provider, String accessToken) {
        UserAccountEntity userAccount = null;
        UsersConnectionRepository userConnectionRepository;
        ConnectionRepository connectionRepository;
        List<Connection<?>> connections;

        switch (provider) {
            case FACEBOOK:
                Facebook facebook = new FacebookTemplate(accessToken);
                String facebookProfileId = facebook.userOperations().getUserProfile().getId();

                userAccount = accountService.findByProviderUserId(facebookProfileId);
                userConnectionRepository = socialConfig.usersConnectionRepository();
                connectionRepository = userConnectionRepository.createConnectionRepository(facebookProfileId);
                if(userAccount == null) {
                    userAccount = saveNewFacebookUserAccountEntity(accessToken, provider, facebook.userOperations().getUserProfile());
                }

                connections = connectionRepository.findConnections(provider.name());
                Assert.isTrue(isConnectionPopulated(connections, provider.name()), "connection repository size is zero");
                socialConfig.mongoConnectionService().update(facebookProfileId, connections.get(0));
                break;
            case GOOGLE:
                Google google = new GoogleTemplate(accessToken);
                String googleProfileId = google.plusOperations().getGoogleProfile().getId();

                userAccount = accountService.findByProviderUserId(googleProfileId);
                userConnectionRepository = socialConfig.usersConnectionRepository();
                connectionRepository = userConnectionRepository.createConnectionRepository(googleProfileId);
                if(userAccount == null) {
                    userAccount = saveNewGoogleUserAccountEntity(accessToken, provider, google.plusOperations().getGoogleProfile());
                }

                connections = connectionRepository.findConnections(provider.name());
                Assert.isTrue(isConnectionPopulated(connections, provider.name()), "connection repository size is zero");
                socialConfig.mongoConnectionService().update(googleProfileId, connections.get(0));
                break;
        }
        if(null != userAccount) {
            JsonObject result = new JsonObject();
            result.addProperty("X-R-MAIL", userAccount.getUserId());
            result.addProperty("X-R-AUTH", userAccount.getUserAuthentication().getAuthenticationKeyEncoded());
            return new Gson().toJson(result);
        }
        return "{}";
    }

    /**
     * Save UserAccountEntity when user signs up from mobile using Facebook provider
     *
     * @param accessToken
     * @param provider
     * @param facebookProfile
     * @return
     */
    private UserAccountEntity saveNewFacebookUserAccountEntity(String accessToken, ProviderEnum provider, FacebookProfile facebookProfile) {
        UserAccountEntity userAccount;UserAuthenticationEntity userAuthentication = accountService.getUserAuthenticationEntity(
                RandomString.newInstance().nextString()
        );

        userAccount = UserAccountEntity.newInstance(
                generateUserIdManager.getNextAutoGeneratedUserId(),
                facebookProfile.getId(),
                facebookProfile.getFirstName(),
                facebookProfile.getLastName(),
                userAuthentication
        );
        userAccount.setProviderId(provider);
        userAccount.setProviderUserId(facebookProfile.getId());
        userAccount.setAccessToken(accessToken);
        userAccount.setProfileUrl(facebookProfile.getLink());
        accountService.saveUserAccount(userAccount);

        //save profile
        connectionService.copyAndSaveFacebookToUserProfile(facebookProfile, userAccount);

        return userAccount;
    }

    /**
     * Save UserAccountEntity when user signs up from mobile using Google provider
     *
     * @param accessToken
     * @param provider
     * @param person
     * @return
     */
    private UserAccountEntity saveNewGoogleUserAccountEntity(String accessToken, ProviderEnum provider, Person person) {
        UserAuthenticationEntity userAuthentication = accountService.getUserAuthenticationEntity(
                RandomString.newInstance().nextString()
        );

        UserAccountEntity userAccount = UserAccountEntity.newInstance(
                generateUserIdManager.getNextAutoGeneratedUserId(),
                person.getId(),
                person.getGivenName(),
                person.getFamilyName(),
                userAuthentication
        );
        userAccount.setProviderId(provider);
        userAccount.setProviderUserId(person.getId());
        userAccount.setDisplayName(person.getDisplayName());
        userAccount.setProfileUrl(person.getUrl());
        userAccount.setImageUrl(person.getImageUrl());
        userAccount.setAccessToken(accessToken);
        accountService.saveUserAccount(userAccount);

        //save profile
        connectionService.copyAndSaveGoogleToUserProfile(person, userAccount);

        return userAccount;
    }

    /**
     * Retrieves the correct ROLE type depending on the access level, where access level is an Integer.
     * Basically, this interprets the access value whether it's for a regular user or admin.
     *
     * @param roles
     * @return collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Set<RoleEnum> roles) {
        List<GrantedAuthority> authList = new ArrayList<>(4);

        for(RoleEnum roleEnum : roles) {
            authList.add(new SimpleGrantedAuthority(roleEnum.name()));
        }

        return authList;
    }

    private boolean isConnectionPopulated(List<Connection<?>> connections, String pid) {
        if(connections.size() == 0) {
            log.warn("connection repository size is zero for pid={}", pid);
            return false;
        }
        return true;
    }
}