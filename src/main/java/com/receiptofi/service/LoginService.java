package com.receiptofi.service;

import com.receiptofi.domain.BrowserEntity;
import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.repository.BrowserManager;
import com.receiptofi.repository.UserAccountManager;
import com.receiptofi.repository.UserAuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 4/27/13
 * Time: 9:33 PM
 */
@Service
public final class LoginService {
    private static Logger log = LoggerFactory.getLogger(LoginService.class);

    @Autowired private UserAuthenticationManager userAuthenticationManager;
    @Autowired private UserAccountManager userAccountManager;
    @Autowired private BrowserManager browserManager;

    public UserAccountEntity findByReceiptUserId(String rid) {
        return userAccountManager.findByReceiptUserId(rid);
    }

    private UserAuthenticationEntity loadAuthenticationEntity(UserAccountEntity userAccount) {
        return userAuthenticationManager.findOne(userAccount.getUserAuthentication().getId());
    }

    public void saveUpdateBrowserInfo(String cookieId, String ip, String userAgent) {
        try {
            BrowserEntity browserEntity = browserManager.findOne(cookieId);
            if(browserEntity == null) {
                browserEntity = BrowserEntity.newInstance(cookieId, ip, userAgent);
                browserManager.save(browserEntity);
            } else {
                browserEntity.setUpdated();
                browserManager.save(browserEntity);
            }
        } catch(Exception e) {
            log.error("Moving on. Omitting this error={}", e.getLocalizedMessage(), e);
        }
    }
}
