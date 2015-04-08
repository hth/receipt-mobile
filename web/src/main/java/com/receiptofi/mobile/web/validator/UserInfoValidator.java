package com.receiptofi.mobile.web.validator;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;

import com.receiptofi.mobile.service.AccountMobileService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.web.controller.api.UtilityController;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * User: hitender
 * Date: 3/5/15 12:06 AM
 */
@Component
public class UserInfoValidator {
    private static final Logger LOG = LoggerFactory.getLogger(UserInfoValidator.class);
    public static final String EMPTY = "Empty";

    private int mailLength;
    private int nameLength;
    private int passwordLength;

    @Autowired
    public UserInfoValidator(
            @Value ("${UserInfoValidator.mailLength:5}")
            int mailLength,

            @Value ("${UserInfoValidator.nameLength:2}")
            int nameLength,

            @Value ("${UserInfoValidator.passwordLength:6}")
            int passwordLength
    ) {
        this.mailLength = mailLength;
        this.nameLength = nameLength;
        this.passwordLength = passwordLength;
    }

    public Map<String, String> validate(String mail, String firstName, String password) {
        LOG.info("failed validation mail={} firstName={} password={}", mail, firstName, UtilityController.AUTH_KEY_HIDDEN);

        Map<String, String> errors = new HashMap<>();
        errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");

        firstNameValidation(firstName, errors);
        mailValidation(mail, errors);
        passwordValidation(password, errors);

        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        return errors;
    }

    public Map<String, String> validateFailureWhenEmpty() {
        Map<String, String> errors = new HashMap<>();
        mailValidation(null, errors);
        return errors;
    }

    public void passwordValidation(String password, Map<String, String> errors) {
        if (StringUtils.isBlank(password) || password.length() < passwordLength) {
            LOG.info("failed validation password={}", UtilityController.AUTH_KEY_HIDDEN);
            errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");
            errors.put(AccountMobileService.REGISTRATION.PW.name(), StringUtils.isBlank(password) ? EMPTY : password);
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        }
    }

    public void mailValidation(String mail, Map<String, String> errors) {
        if (StringUtils.isBlank(mail) || mail.length() < mailLength) {
            LOG.info("failed validation mail={}", mail);
            errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");
            errors.put(AccountMobileService.REGISTRATION.EM.name(), StringUtils.isBlank(mail) ? EMPTY : mail);
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        }
    }

    public void firstNameValidation(String firstName, Map<String, String> errors) {
        if (StringUtils.isBlank(firstName) || firstName.length() < nameLength) {
            LOG.info("failed validation firstName={}", firstName);
            errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");
            errors.put(AccountMobileService.REGISTRATION.FN.name(), StringUtils.isBlank(firstName) ? EMPTY : firstName);
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        }
    }

    public int getMailLength() {
        return mailLength;
    }

    public int getNameLength() {
        return nameLength;
    }

    public int getPasswordLength() {
        return passwordLength;
    }
}
