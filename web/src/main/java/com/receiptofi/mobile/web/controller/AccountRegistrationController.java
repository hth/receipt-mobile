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
import com.receiptofi.utils.ScrubbedInput;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String EMPTY = "Empty";

    private int mailLength;
    private int nameLength;
    private int passwordLength;
    private AccountService accountService;
    private AccountSignupService accountSignupService;

    @Autowired
    public AccountRegistrationController(
            @Value ("${AccountRegistrationController.mailLength:5}")
            int mailLength,

            @Value ("${AccountRegistrationController.nameLength:2}")
            int nameLength,

            @Value ("${AccountRegistrationController.passwordLength:6}")
            int passwordLength,

            AccountService accountService,
            AccountSignupService accountSignupService
    ) {
        this.mailLength = mailLength;
        this.nameLength = nameLength;
        this.passwordLength = passwordLength;
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

        if(!map.isEmpty()) {
            String mail = StringUtils.lowerCase(map.get(REGISTRATION.EM.name()).getText());
            String firstName = map.get(REGISTRATION.FN.name()).getText();
            String lastName = null;
            if (StringUtils.isNotEmpty(firstName)) {
                String[] name = firstName.split(" ");
                if (name.length > 1) {
                    String fn = null;
                    for (int i = 0; i < name.length - 1; i++) {
                        if (fn == null) {
                            fn = name[i] + " ";
                        } else {
                            fn = fn + name[i] + " ";
                        }
                    }
                    firstName = StringUtils.trim(fn);
                    lastName = name[nameLength - (nameLength - 1)];
                }
            }
            String password = map.get(REGISTRATION.PW.name()).getText();
            String birthday = map.get(REGISTRATION.BD.name()).getText();

            if (null == mail || mail.length() < mailLength ||
                    null == firstName || firstName.length() < nameLength ||
                    null == password || password.length() < passwordLength) {

                return ErrorEncounteredJson.toJson(validate(mail, firstName, password));
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
            } catch (Exception e) {
                LOG.error("failed signup for user={} reason={}", mail, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "failed creating account");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, SEVERE.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }
        } else {
            /** Validation failure as there is not data in the map. */
            return ErrorEncounteredJson.toJson(validate(null, null, null));
        }

        return credential;
    }

    private Map<String, String> validate(String mail, String firstName, String password) {
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
}
