package com.receiptofi.mobile.web.controller;

import static com.receiptofi.mobile.service.AccountSignupService.REGISTRATION;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.EXISTING_USER;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_JSON;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;

import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.service.AccountSignupService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.service.AccountService;
import com.receiptofi.utils.ParseJsonStringToMap;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 11/2/14 1:08 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal"
})
@RestController
public class AccountRegistrationController {
    private static final Logger LOG = LoggerFactory.getLogger(AccountRegistrationController.class);
    private static final int MINIMUM_SIZE = 4;
    private static final String EMPTY = "Empty";

    private AccountService accountService;
    private AccountSignupService accountSignupService;

    @Autowired
    public AccountRegistrationController(AccountService accountService, AccountSignupService accountSignupService) {
        this.accountService = accountService;
        this.accountSignupService = accountSignupService;
    }

    @RequestMapping (
            value = "/registration.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    @ResponseBody
    public String registerUser(@RequestBody String registrationJson, HttpServletResponse response) throws IOException {
        String credential = "{}";
        Map<String, String> map;
        try {
            map = ParseJsonStringToMap.jsonStringToMap(registrationJson);
        } catch (IOException e) {
            LOG.error("could not parse json={} reason={}", registrationJson, e.getLocalizedMessage(), e);
            return ErrorEncounteredJson.toJson("could not parse JSON", MOBILE_JSON);
        }

        if (map != null) {
            String mail = StringUtils.lowerCase(map.get(REGISTRATION.EM.name()));
            String firstName = map.get(REGISTRATION.FN.name());
            String lastName = map.get(REGISTRATION.LN.name());
            String password = map.get(REGISTRATION.PW.name());
            String birthday = map.get(REGISTRATION.BD.name());

            if (null == mail || mail.length() < MINIMUM_SIZE ||
                    null == firstName || firstName.length() < MINIMUM_SIZE ||
                    null == lastName || lastName.length() < MINIMUM_SIZE ||
                    null == password || password.length() < MINIMUM_SIZE) {

                return ErrorEncounteredJson.toJson(validate(mail, firstName, lastName, password));
            }

            UserProfileEntity userProfile = accountService.doesUserExists(mail);
            if (userProfile != null) {
                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "user already exists");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, EXISTING_USER.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, EXISTING_USER.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            try {
                String auth = accountSignupService.signup(mail, firstName, lastName, password, birthday);
                response.addHeader("X-R-MAIL", mail);
                response.addHeader("X-R-AUTH", auth);
            } catch(Exception e) {
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

    private Map<String, String> validate(String mail, String firstName, String lastName, String password) {
        Map<String, String> errors = new HashMap<>();
        errors.put("reason", "failed data validation");
        if (null == firstName || firstName.length() < MINIMUM_SIZE) {
            errors.put(REGISTRATION.FN.name(), firstName == null ? EMPTY : firstName);
        }
        if (null == lastName || lastName.length() < MINIMUM_SIZE) {
            errors.put(REGISTRATION.LN.name(), lastName == null ? EMPTY : lastName);
        }
        if (null == mail || mail.length() < MINIMUM_SIZE) {
            errors.put(REGISTRATION.EM.name(), mail == null ? EMPTY : mail);
        }
        if (null == password || password.length() < MINIMUM_SIZE) {
            errors.put(REGISTRATION.PW.name(), password == null ? EMPTY : password);
        }
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        return errors;
    }
}
