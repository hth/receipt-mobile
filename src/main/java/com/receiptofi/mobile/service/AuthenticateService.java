package com.receiptofi.mobile.service;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.repository.UserAccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * User: hitender
 * Date: 6/9/14 11:16 PM
 */
@Service
public class AuthenticateService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticateService.class);

    private final UserAccountManager userAccountManager;

    @Autowired
    public AuthenticateService(UserAccountManager userAccountManager) {
        this.userAccountManager = userAccountManager;
    }

    public boolean hasAccess(String mail, String auth) {
        UserAccountEntity userAccountEntity = userAccountManager.findByUserId(mail);
        Assert.notNull(userAccountEntity);

        try {
            return userAccountEntity.getUserAuthentication().getAuthenticationKey().equals(URLDecoder.decode(auth, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Auth decoding issue for user={}, reason={}", mail, e.getLocalizedMessage(), e);
            return false;
        }
    }
}
