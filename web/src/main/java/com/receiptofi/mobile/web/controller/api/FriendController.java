package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;

import com.receiptofi.mobile.service.AccountMobileService;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.web.validator.UserInfoValidator;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
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
 * Date: 10/21/15 10:30 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api")
public class FriendController {
    private static final Logger LOG = LoggerFactory.getLogger(FriendController.class);

    @Autowired private AuthenticateService authenticateService;
    @Autowired private UserInfoValidator userInfoValidator;
    @Autowired private AccountMobileService accountMobileService;

    /**
     * On account UID change, set account to re-validated.
     *
     * @param mail
     * @param auth
     * @param inviteMailJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/invite.json",
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
            String inviteMailJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String inviteEmail;

        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(inviteMailJson);
            LOG.info("InviteEmail={} from rid={}", map.get(AccountMobileService.REGISTRATION.EM.name()), rid);

            inviteEmail = StringUtils.lowerCase(map.get(AccountMobileService.REGISTRATION.EM.name()).getText());
            if (StringUtils.isBlank(inviteEmail) || userInfoValidator.getMailLength() > inviteEmail.length()) {
                Map<String, String> errors = new HashMap<>();
                userInfoValidator.mailValidation(inviteEmail, errors);
                return ErrorEncounteredJson.toJson(errors);
            }

            try {
                if (!accountMobileService.inviteUser(inviteEmail, rid)) {
                    LOG.warn("Failed sending invite email={} by rid={}", inviteEmail, rid);

                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Failed sending invite. Please try again soon.");
                    errors.put(AccountMobileService.REGISTRATION.EM.name(), inviteEmail);
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, SEVERE.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, SEVERE.getCode());
                    return ErrorEncounteredJson.toJson(errors);
                }
            } catch (Exception e) {
                LOG.error("Failed sending invite email for inviteEmail={} reason={}", inviteEmail, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Something went wrong. Engineers are looking into this.");
                errors.put(AccountMobileService.REGISTRATION.EM.name(), inviteEmail);
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, SEVERE.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }
        }

        LOG.info("Sent Invite mail={} from rid={}", inviteEmail, rid);
        return null;
    }
}
