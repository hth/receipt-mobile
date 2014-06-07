package com.receiptofi.service;

import com.receiptofi.domain.EmailValidateEntity;
import com.receiptofi.repository.EmailValidateManager;
import com.receiptofi.utils.HashText;
import com.receiptofi.utils.RandomString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 5/17/14 6:28 PM
 */
@Service
public class EmailValidateService {

    private EmailValidateManager emailValidateManager;

    @Autowired
    public EmailValidateService(EmailValidateManager emailValidateManager) {
        this.emailValidateManager = emailValidateManager;
    }

    public EmailValidateEntity saveAccountValidate(String receiptUserId, String userId) {
        String authenticationKey = HashText.computeBCrypt(RandomString.newInstance().nextString());
        EmailValidateEntity emailValidate = EmailValidateEntity.newInstance(receiptUserId, userId, authenticationKey);
        saveEmailValidateEntity(emailValidate);
        return emailValidate;
    }

    public void saveEmailValidateEntity(EmailValidateEntity emailValidate) {
        emailValidateManager.save(emailValidate);
    }

    public EmailValidateEntity findByAuthenticationKey(String key) {
        return emailValidateManager.findByAuthenticationKey(key);
    }
}
