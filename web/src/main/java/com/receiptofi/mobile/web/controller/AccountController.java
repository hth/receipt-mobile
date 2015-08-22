package com.receiptofi.mobile.web.controller;

import static com.receiptofi.mobile.service.AccountMobileService.REGISTRATION;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_JSON;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.REGISTRATION_TURNED_OFF;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_EXISTING;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_NOT_FOUND;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_SOCIAL;

import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.service.AccountMobileService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.web.validator.UserInfoValidator;
import com.receiptofi.service.AccountService;
import com.receiptofi.utils.Constants;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 11/2/14 1:08 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
public class AccountController {
    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

    private AccountService accountService;
    private AccountMobileService accountMobileService;
    private UserInfoValidator userInfoValidator;

    @Autowired
    public AccountController(
            AccountService accountService,
            AccountMobileService accountMobileService,
            UserInfoValidator userInfoValidator
    ) {
        this.accountService = accountService;
        this.accountMobileService = accountMobileService;
        this.userInfoValidator = userInfoValidator;
    }

    @RequestMapping (
            value = "/registration.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String registerUser(
            @RequestBody
            String registrationJson,

            HttpServletResponse response
    ) throws IOException {
        String credential = "{}";
        Map<String, ScrubbedInput> map;
        try {
            map = ParseJsonStringToMap.jsonStringToMap(registrationJson);
        } catch (IOException e) {
            LOG.error("Could not parse json={} reason={}", registrationJson, e.getLocalizedMessage(), e);
            return ErrorEncounteredJson.toJson("Could not parse JSON", MOBILE_JSON);
        }

        if (map.isEmpty()) {
            /** Validation failure as there is not data in the map. */
            return ErrorEncounteredJson.toJson(userInfoValidator.validate(null, null, null, null));
        } else {
            Set<String> unknownKeys = invalidElementsInMapDuringRegistration(map);
            if (!unknownKeys.isEmpty()) {
                /** Validation failure as there are unknown keys. */
                return ErrorEncounteredJson.toJson("Could not parse " + unknownKeys, MOBILE_JSON);
            }

            String mail = StringUtils.lowerCase(map.get(REGISTRATION.EM.name()).getText());
            String firstName = WordUtils.capitalize(map.get(REGISTRATION.FN.name()).getText());
            String lastName = null;
            if (StringUtils.isNotBlank(firstName)) {
                /** NPE is already checked in above condition. */
                @SuppressWarnings ("all")
                String[] name = firstName.split(" ");
                if (name.length > 1) {
                    lastName = name[name.length - 1];
                    firstName = StringUtils.trim(firstName.substring(0, firstName.indexOf(lastName)));
                }
            }
            String password = map.get(REGISTRATION.PW.name()).getText();
            String birthday = map.get(REGISTRATION.BD.name()).getText();

            if (StringUtils.isBlank(mail) || userInfoValidator.getMailLength() > mail.length() ||
                    StringUtils.isBlank(firstName) || userInfoValidator.getNameLength() > firstName.length() ||
                    StringUtils.isBlank(password) || userInfoValidator.getPasswordLength() > password.length() ||
                    StringUtils.isNotBlank(birthday) && !Constants.AGE_RANGE.matcher(birthday).matches()) {

                return ErrorEncounteredJson.toJson(userInfoValidator.validate(mail, firstName, password, birthday));
            }

            birthday = DateUtil.parseAgeForBirthday(birthday);

            UserProfileEntity userProfile = accountService.doesUserExists(mail);
            if (userProfile != null) {
                LOG.info("Failed user registration as already exists mail={}", mail);
                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "User already exists. Did you forget password?");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_EXISTING.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_EXISTING.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            try {
                String auth = accountMobileService.signup(mail, firstName, lastName, password, birthday);
                response.addHeader("X-R-MAIL", mail);
                if (accountMobileService.acceptingSignup()) {
                    /** X-R-AUTH is sent when server is accepting registration. */
                    response.addHeader("X-R-AUTH", auth);
                } else {
                    /** when server is NOT accepting registration. */
                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Account created successfully. Site is not accepting new " +
                            "users. When site starts accepting new users, you will be notified through email and your " +
                            "account would be turned active.");
                    errors.put(AccountMobileService.REGISTRATION_TURNED_ON.RTO.name(), Boolean.FALSE.toString());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, REGISTRATION_TURNED_OFF.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, REGISTRATION_TURNED_OFF.getCode());
                    return ErrorEncounteredJson.toJson(errors);

                }
            } catch (Exception e) {
                LOG.error("Failed signup for user={} reason={}", mail, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Something went wrong. Engineers are looking into this.");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, SEVERE.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }
        }

        return credential;
    }

    @RequestMapping (
            value = "/recover.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String recover(
            @RequestBody
            String recoverJson,

            HttpServletResponse response
    ) throws IOException {
        String credential = "{}";
        Map<String, ScrubbedInput> map;
        try {
            map = ParseJsonStringToMap.jsonStringToMap(recoverJson);
        } catch (IOException e) {
            LOG.error("Could not parse json={} reason={}", recoverJson, e.getLocalizedMessage(), e);
            return ErrorEncounteredJson.toJson("Could not parse JSON.", MOBILE_JSON);
        }

        if (map.isEmpty()) {
            /** Validation failure as there is not data in the map. */
            return ErrorEncounteredJson.toJson(userInfoValidator.validateFailureWhenEmpty());
        } else {
            Set<String> unknownKeys = invalidElementsInMapDuringRecovery(map);
            if (!unknownKeys.isEmpty()) {
                /** Validation failure as there are unknown keys. */
                return ErrorEncounteredJson.toJson("Could not parse " + unknownKeys, MOBILE_JSON);
            }

            String mail = StringUtils.lowerCase(map.get(REGISTRATION.EM.name()).getText());
            if (StringUtils.isBlank(mail) || userInfoValidator.getMailLength() > mail.length()) {
                LOG.info("Failed data validation={}", mail);
                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");
                errors.put(REGISTRATION.EM.name(), StringUtils.isBlank(mail) ? UserInfoValidator.EMPTY : mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            UserProfileEntity userProfile = accountService.doesUserExists(mail);
            if (null == userProfile) {
                LOG.info("User does not exists mail={}", mail);
                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "User with this email address is not registered. Would you like to sign up?");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_NOT_FOUND.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_NOT_FOUND.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            /** Remove this code to allow user from social login to change password. */
            if (null != userProfile.getProviderId()) {
                LOG.info("Social account user trying to recover password mail={} pid={}", mail, userProfile.getProviderId());
                Map<String, String> errors = new HashMap<>();
                errors.put(
                        ErrorEncounteredJson.REASON,
                        "Cannot change password for your account. As you signed up using social login from Facebook or Google+.");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_SOCIAL.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_SOCIAL.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            try {
                if (accountMobileService.recoverAccount(mail)) {
                    LOG.info("Sent recovery mail={}", mail);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    LOG.warn("Failed sending recovery email={}", mail);

                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Failed sending recovery email. Please try again soon.");
                    errors.put(REGISTRATION.EM.name(), mail);
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, SEVERE.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, SEVERE.getCode());
                    return ErrorEncounteredJson.toJson(errors);
                }
            } catch (Exception e) {
                LOG.error("Failed sending recovery email for user={} reason={}", mail, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Something went wrong. Engineers are looking into this.");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, SEVERE.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }
        }

        return credential;
    }

    private Set<String> invalidElementsInMapDuringRegistration(Map<String, ScrubbedInput> map) {
        Set<String> keys = new HashSet<>(map.keySet());
        List<REGISTRATION> enums = new ArrayList<>(Arrays.asList(REGISTRATION.values()));
        for (REGISTRATION registration : enums) {
            keys.remove(registration.name());
        }

        return keys;
    }

    private Set<String> invalidElementsInMapDuringRecovery(Map<String, ScrubbedInput> map) {
        Set<String> keys = new HashSet<>(map.keySet());
        List<REGISTRATION> enums = new ArrayList<>(Collections.singletonList(REGISTRATION.EM));
        for (REGISTRATION registration : enums) {
            keys.remove(registration.name());
        }

        return keys;
    }
}
