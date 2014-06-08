package com.receiptofi.mobile.security;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.service.AccountService;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * User: hitender
 * Date: 6/7/14 1:12 AM
 */
@Component
public class AuthenticatedToken {

    private final AccountService accountService;

    @Value("${cookieMaxAge:1814400}")
    private int cookieMaxAge;

    @Value("${cookiePath:/receipt-mobile}")
    private String cookiePath;

    //set cookieDomain to www.receiptofi.com for prod
    //set cookieDomain to IP for test
    @Value("${cookieDomain:localhost}")
    private String cookieDomain;

    //prod and test true for secure protocol
    @Value("${cookieHttpOnly:false}")
    private boolean cookieHttpOnly;

    @Autowired
    public AuthenticatedToken(AccountService accountService) {
        this.accountService = accountService;
    }

    protected Cookie createAuthenticatedCookie(String receiptUserId) {
        Cookie cookie = new Cookie("id", receiptUserId + "|" + getUserAuthenticationKey(receiptUserId));
        cookie.setPath(cookiePath);
        cookie.setDomain(cookieDomain);
        cookie.setMaxAge(cookieMaxAge);
        cookie.setHttpOnly(cookieHttpOnly);
        return cookie;
    }

    protected String getUserAuthenticationKey(String receiptUserId) {
        UserAccountEntity userAccountEntity = accountService.findByUserId(receiptUserId);
        Assert.notNull(userAccountEntity);
        return userAccountEntity.getUserAuthentication().getAuthenticationKey();
    }
}
