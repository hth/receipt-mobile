package com.receiptofi.service;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.domain.types.RoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 3/29/14 12:33 AM
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired private LoginService loginService;
    @Autowired private UserProfilePreferenceService userProfilePreferenceService;

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
                    userProfile.getLevel(),
                    userAccountEntity.isActive() && userAccountEntity.isAccountValidated()
            );
        }
    }

    public UserDetails loadUserByUserId(String uid) throws UsernameNotFoundException {
        log.info("login through facebook user={}", uid);

        UserProfileEntity userProfile = userProfilePreferenceService.getUsingUserId(uid);
        if(userProfile == null) {
            log.warn("not found user={}", uid);
            throw new UsernameNotFoundException("Error in retrieving user");
        } else {
            UserAccountEntity userAccountEntity = loginService.findByReceiptUserId(userProfile.getReceiptUserId());
            UserAuthenticationEntity userAuthenticate = userAccountEntity.getUserAuthentication();

            return new ReceiptUser(
                    userProfile.getUserId(),
                    userAuthenticate == null ?
                            "$2a$12$Ce0mzNSijSvhAjGqfMKvx.SCQUqLHRQnTeOsKH9sphjC0XF3TA4Ge" :
                            userAuthenticate.getPassword(),
                    getAuthorities(userAccountEntity.getRoles()),
                    userProfile.getReceiptUserId(),
                    userProfile.getLevel(),
                    userAccountEntity.isActive() && userAccountEntity.isAccountValidated()
            );
        }
    }

    /**
     * Retrieves the correct ROLE type depending on the access level, where access level is an Integer.
     * Basically, this interprets the access value whether it's for a regular user or admin.
     *
     * @param roles
     * @return collection of granted authorities
     */
    private Collection<? extends  GrantedAuthority> getAuthorities(Set<RoleEnum> roles) {
        List<GrantedAuthority> authList = new ArrayList<>(4);

        for(RoleEnum roleEnum : roles) {
            authList.add(new SimpleGrantedAuthority(roleEnum.name()));
        }

        return authList;
    }
}