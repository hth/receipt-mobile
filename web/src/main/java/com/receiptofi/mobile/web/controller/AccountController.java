package com.receiptofi.mobile.web.controller;

import static com.receiptofi.mobile.service.MobileAccountService.REGISTRATION;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_JSON;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_EXISTING;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_NOT_FOUND;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.REGISTRATION_TURNED_OFF;

import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.service.MobileAccountService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.web.controller.api.UtilityController;
import com.receiptofi.service.AccountService;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String EMPTY = "Empty";

    private int mailLength;
    private int nameLength;
    private int passwordLength;
    private AccountService accountService;
    private MobileAccountService mobileAccountService;

    @Autowired
    public AccountController(
            @Value ("${AccountRegistrationController.mailLength:5}")
            int mailLength,

            @Value ("${AccountRegistrationController.nameLength:2}")
            int nameLength,

            @Value ("${AccountRegistrationController.passwordLength:6}")
            int passwordLength,

            AccountService accountService,
            MobileAccountService mobileAccountService
    ) {
        this.mailLength = mailLength;
        this.nameLength = nameLength;
        this.passwordLength = passwordLength;
        this.accountService = accountService;
        this.mobileAccountService = mobileAccountService;
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
            LOG.error("could not parse json={} reason={}", registrationJson, e.getLocalizedMessage(), e);
            return ErrorEncounteredJson.toJson("could not parse JSON", MOBILE_JSON);
        }

        if (map.isEmpty()) {
            /** Validation failure as there is not data in the map. */
            return ErrorEncounteredJson.toJson(validate(null, null, null));
        } else {
            Set<String> unknownKeys = invalidElementsInMapDuringRegistration(map);
            if (!unknownKeys.isEmpty()) {
                /** Validation failure as there are unknown keys. */
                return ErrorEncounteredJson.toJson("could not parse " + unknownKeys, MOBILE_JSON);
            }

            String mail = StringUtils.lowerCase(map.get(REGISTRATION.EM.name()).getText());
            String firstName = WordUtils.capitalize(map.get(REGISTRATION.FN.name()).getText());
            String lastName = null;
            if (StringUtils.isNotBlank(firstName)) {
                String[] name = firstName.split(" ");
                if (name.length > 1) {
                    lastName = name[name.length - 1];
                    firstName = StringUtils.trim(firstName.substring(0, firstName.indexOf(lastName)));
                }
            }
            String password = map.get(REGISTRATION.PW.name()).getText();
            String birthday = map.get(REGISTRATION.BD.name()).getText();

            if (StringUtils.isBlank(mail) || mailLength > mail.length() ||
                    StringUtils.isBlank(firstName) || nameLength > firstName.length() ||
                    StringUtils.isBlank(password) || passwordLength > password.length()) {

                return ErrorEncounteredJson.toJson(validate(mail, firstName, password));
            }

            UserProfileEntity userProfile = accountService.doesUserExists(mail);
            if (userProfile != null) {
                LOG.info("failed user registration as already exists mail={}", mail);
                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "user already exists");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_EXISTING.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_EXISTING.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            try {
                String auth = mobileAccountService.signup(mail, firstName, lastName, password, birthday);
                response.addHeader("X-R-MAIL", mail);
                if (mobileAccountService.acceptingSignup()) {
                    /** X-R-AUTH is sent when server is accepting registration. */
                    response.addHeader("X-R-AUTH", auth);
                } else {
                    /** when server is NOT accepting registration. */
                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Account created successfully. Site is not accepting new " +
                            "users. When site starts accepting new users, you will be notified through email and your " +
                            "account would be turned active.");
                    errors.put(MobileAccountService.REGISTRATION_TURNED_ON.RTO.name(), Boolean.FALSE.toString());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, REGISTRATION_TURNED_OFF.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, REGISTRATION_TURNED_OFF.getCode());
                    return ErrorEncounteredJson.toJson(errors);

                }
            } catch (Exception e) {
                LOG.error("failed signup for user={} reason={}", mail, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "failed creating account");
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
            LOG.error("could not parse json={} reason={}", recoverJson, e.getLocalizedMessage(), e);
            return ErrorEncounteredJson.toJson("could not parse JSON", MOBILE_JSON);
        }

        if (map.isEmpty()) {
            /** Validation failure as there is not data in the map. */
            return ErrorEncounteredJson.toJson(validate(null));
        } else {
            Set<String> unknownKeys = invalidElementsInMapDuringRecovery(map);
            if (!unknownKeys.isEmpty()) {
                /** Validation failure as there are unknown keys. */
                return ErrorEncounteredJson.toJson("could not parse " + unknownKeys, MOBILE_JSON);
            }

            String mail = StringUtils.lowerCase(map.get(REGISTRATION.EM.name()).getText());
            if (StringUtils.isBlank(mail) || mailLength > mail.length()) {
                LOG.info("failed data validation={}", mail);
                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "failed data validation");
                errors.put(REGISTRATION.EM.name(), StringUtils.isBlank(mail) ? EMPTY : mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            UserProfileEntity userProfile = accountService.doesUserExists(mail);
            if (null == userProfile) {
                LOG.info("user does not exists mail={}", mail);
                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "user does not exists");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_NOT_FOUND.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_NOT_FOUND.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            try {
                if (mobileAccountService.recoverAccount(mail)) {
                    LOG.info("sent recovery mail={}", mail);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    LOG.warn("failed sending recovery email={}", mail);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (Exception e) {
                LOG.error("failed signup for user={} reason={}", mail, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "failed creating account");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, SEVERE.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }
        }

        return credential;
    }

    private Map<String, String> validate(String mail, String firstName, String password) {
        LOG.info("failed validation mail={} firstName={} password={}", mail, firstName, UtilityController.AUTH_KEY_HIDDEN);

        Map<String, String> errors = new HashMap<>();
        errors.put(ErrorEncounteredJson.REASON, "failed data validation");

        if (StringUtils.isBlank(firstName) || firstName.length() < nameLength) {
            errors.put(REGISTRATION.FN.name(), StringUtils.isBlank(firstName) ? EMPTY : firstName);
        }
        if (StringUtils.isBlank(mail) || mail.length() < mailLength) {
            errors.put(REGISTRATION.EM.name(), StringUtils.isBlank(mail) ? EMPTY : mail);
        }
        if (StringUtils.isBlank(password) || password.length() < passwordLength) {
            errors.put(REGISTRATION.PW.name(), StringUtils.isBlank(password) ? EMPTY : password);
        }

        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        return errors;
    }

    private Set<String> invalidElementsInMapDuringRegistration(Map<String, ScrubbedInput> map) {
        Set<String> keys = new HashSet<>(map.keySet());
        List<REGISTRATION> enums = new ArrayList<>(Arrays.asList(REGISTRATION.values()));
        for (REGISTRATION registration : enums) {
            keys.remove(registration.name());
        }

        return keys;
    }

    private Map<String, String> validate(String mail) {
        LOG.info("failed validation mail={}", mail);

        Map<String, String> errors = new HashMap<>();
        errors.put(ErrorEncounteredJson.REASON, "failed data validation");

        if (StringUtils.isBlank(mail) || mail.length() < mailLength) {
            errors.put(REGISTRATION.EM.name(), StringUtils.isBlank(mail) ? EMPTY : mail);
        }

        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        return errors;
    }

    private Set<String> invalidElementsInMapDuringRecovery(Map<String, ScrubbedInput> map) {
        Set<String> keys = new HashSet<>(map.keySet());
        List<REGISTRATION> enums = new ArrayList<>(Arrays.asList(REGISTRATION.EM));
        for (REGISTRATION registration : enums) {
            keys.remove(registration.name());
        }

        return keys;
    }
}
