package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_EXISTING;
import static com.receiptofi.mobile.web.controller.SocialAuthenticationController.AUTH;
import static com.receiptofi.mobile.web.controller.SocialAuthenticationController.MAIL;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.MobileAccountService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import com.receiptofi.mobile.web.validator.UserInfoValidator;
import com.receiptofi.service.AccountService;
import com.receiptofi.utils.HashText;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.RandomString;
import com.receiptofi.utils.ScrubbedInput;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 12/25/14 7:21 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api")
public class ProfileController {
    private static final Logger LOG = LoggerFactory.getLogger(ProfileController.class);

    private AuthenticateService authenticateService;
    private AccountService accountService;
    private MobileAccountService mobileAccountService;
    private UserInfoValidator userInfoValidator;

    @Autowired
    public ProfileController(
            AuthenticateService authenticateService,
            AccountService accountService,
            MobileAccountService mobileAccountService,
            UserInfoValidator userInfoValidator
    ) {
        this.authenticateService = authenticateService;
        this.accountService = accountService;
        this.mobileAccountService = mobileAccountService;
        this.userInfoValidator = userInfoValidator;
    }

    /**
     * On account UID change, set account to re-validated.
     *
     * @param mail
     * @param auth
     * @param updatedMailJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/updateMail.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String updateMail(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestBody
            String updatedMailJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(updatedMailJson);
            LOG.info("new mail={}", map.get("UID"));

            if (StringUtils.isBlank(map.get("UID").getText())) {
                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "failed data validation");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);
            }

            String newUserId = map.get("UID").getText();
            UserAccountEntity userAccountExists = accountService.findByUserId(newUserId);
            if (null == userAccountExists) {
                UserAccountEntity userAccount = mobileAccountService.changeUID(mail, newUserId);

                response.addHeader(MAIL, userAccount.getUserId());
                response.addHeader(AUTH, userAccount.getUserAuthentication().getAuthenticationKeyEncoded());
                return null;
            } else {
                LOG.info("failed user id update as another user exists with same mail={}", mail);
                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "user already exists with this mail");
                errors.put(MobileAccountService.REGISTRATION.EM.name(), newUserId);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_EXISTING.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_EXISTING.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }
        }
    }

    @RequestMapping (
            value = "/updatePassword.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String updatePassword(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestBody
            String updatedPasswordJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        UserAccountEntity userAccount = authenticateService.findUserAccount(mail, auth);
        if (null == userAccount) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(updatedPasswordJson);
            String password = map.get(MobileAccountService.REGISTRATION.PW.name()).getText();
            if (StringUtils.isBlank(password) || userInfoValidator.getPasswordLength() > password.length()) {
                Map <String, String> errors = new HashMap<>();
                userInfoValidator.passwordValidation(password, errors);
                return ErrorEncounteredJson.toJson(errors);
            }

            if (!userAccount.isAccountValidated()) {
                /** Since account is not validated, send validation email instead. */
                return null;
            } else {
                LOG.info("new password={}", UtilityController.AUTH_KEY_HIDDEN);
//                if (StringUtils.isBlank(map.get("PA").getText())) {
//
//                    Map<String, String> errors = new HashMap<>();
//                    errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");
//                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
//                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());
//
//                    return ErrorEncounteredJson.toJson(errors);
//                }

                UserAuthenticationEntity userAuthentication = UserAuthenticationEntity.newInstance(
                        HashText.computeBCrypt(map.get(MobileAccountService.REGISTRATION.PW.name()).getText()),
                        HashText.computeBCrypt(RandomString.newInstance().nextString())
                );

                userAuthentication.setId(userAccount.getUserAuthentication().getId());
                userAuthentication.setVersion(userAccount.getUserAuthentication().getVersion());
                userAuthentication.setCreated(userAccount.getUserAuthentication().getCreated());
                accountService.updateAuthentication(userAuthentication);

                response.addHeader(MAIL, mail);
                response.addHeader(AUTH, userAuthentication.getAuthenticationKeyEncoded());
                return null;
            }
        }
    }
}
