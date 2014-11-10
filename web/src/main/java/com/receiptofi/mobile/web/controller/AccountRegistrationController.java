package com.receiptofi.mobile.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import static com.receiptofi.mobile.service.AccountSignupService.REGISTRATION;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_JSON;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.EXISTING_USER;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: hitender
 * Date: 11/2/14 1:08 AM
 */
@Controller
public class AccountRegistrationController {
    private static final Logger LOG = LoggerFactory.getLogger(AccountRegistrationController.class);

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

            if (null == mail || mail.length() < 4 ||
                    null == firstName || firstName.length() < 4 ||
                    null == lastName || lastName.length() < 4 ||
                    null == password || password.length() < 4) {
                Map<String, String> errors = new HashMap<>();
                errors.put("reason", "failed data validation");
                if (null == firstName || firstName.length() < 4) {
                    errors.put(REGISTRATION.FN.name(), firstName == null ? "Empty" : firstName);
                }
                if (null == lastName || lastName.length() < 4) {
                    errors.put(REGISTRATION.LN.name(), lastName == null ? "Empty" : lastName);
                }
                if (null == mail || mail.length() < 4) {
                    errors.put(REGISTRATION.EM.name(), mail == null ? "Empty" : mail);
                }
                if (null == password || password.length() < 4) {
                    errors.put(REGISTRATION.PW.name(), password == null ? "Empty" : password);
                }
                errors.put("systemError", USER_INPUT.name());
                errors.put("systemErrorCode", USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);
            }

            UserProfileEntity userProfile = accountService.doesUserExists(mail);
            if (userProfile != null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("reason", "user already exists");
                errors.put(REGISTRATION.EM.name(), mail);
                errors.put("systemError", EXISTING_USER.name());
                errors.put("systemErrorCode", EXISTING_USER.getCode());
                return ErrorEncounteredJson.toJson(errors);
            } else {
                try {
                    String auth = accountSignupService.signup(mail, firstName, lastName, password, birthday);
                    response.addHeader("X-R-MAIL", mail);
                    response.addHeader("X-R-AUTH", auth);
                } catch(Exception e) {
                    LOG.error("failed signup for user={} reason={}", mail, e.getLocalizedMessage(), e);

                    Map<String, String> errors = new HashMap<>();
                    errors.put("reason", "failed creating account");
                    errors.put(REGISTRATION.EM.name(), mail);
                    errors.put("systemError", SEVERE.name());
                    errors.put("systemErrorCode", SEVERE.getCode());
                    return ErrorEncounteredJson.toJson(errors);
                }
            }
        }

        return credential;
    }
}
