package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;

import com.google.gson.JsonObject;

import com.receiptofi.domain.types.FriendConnectionTypeEnum;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.service.AccountMobileService;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.FriendMobileService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import com.receiptofi.mobile.web.validator.UserInfoValidator;
import com.receiptofi.utils.ParseJsonStringToMap;
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
    @Autowired private FriendMobileService friendMobileService;

    /**
     * Invite new user or send invites to existing user.
     *
     * @param mail
     * @param auth
     * @param inviteMailJson
     * @param response
     * @return pendingFriends
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
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        friendMobileService.getPendingFriends(rid, availableAccountUpdates);
        return availableAccountUpdates.asJson();
    }

    /**
     * Invite new user or send invites to existing user.
     *
     * @param mail
     * @param auth
     * @param connectionChangeJson
     * @param response
     * @return pendingFriends, friends, awaitingFriends
     * @throws IOException
     */
    @RequestMapping (
            value = "/friend.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String friend(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestBody
            String connectionChangeJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(connectionChangeJson);
            LOG.info("Friend mail={} id={} au={} ct={}", mail, map.get("id"), map.get("au"), map.get("ct"));

            if (!map.containsKey("id") || StringUtils.isBlank(map.get("id").getText()) ||
                    !map.containsKey("au") || StringUtils.isBlank(map.get("au").getText()) ||
                    !map.containsKey("ct") || StringUtils.isBlank(map.get("ct").getText())) {
                LOG.warn("Friend API missing data. Failed to process request, mail={}", mail);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Missing required data.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            FriendConnectionTypeEnum friendConnectionType;
            try {
                friendConnectionType = FriendConnectionTypeEnum.valueOf(map.get("ct").getText());
                if (friendMobileService.updateFriendConnection(
                        map.get("id").getText(),
                        map.get("au").getText(),
                        friendConnectionType,
                        rid)) {

                    AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
                    friendMobileService.getActiveFriends(rid, availableAccountUpdates);
                    friendMobileService.getPendingFriends(rid, availableAccountUpdates);
                    friendMobileService.getAwaitingFriends(rid, availableAccountUpdates);

                    return availableAccountUpdates.asJson();
                } else {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("success", false);
                    return jsonObject.toString();
                }
            } catch (IllegalArgumentException e) {
                LOG.error("FriendConnectionType={} reason=", map.get("ct").getText(), e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Unsupported action requested on Friend.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);

            } catch (Exception e) {
                LOG.error("reason=", e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Something went wrong. Engineers are looking into this.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.SEVERE.getCode());

                return ErrorEncounteredJson.toJson(errors);
            }
        }
    }

    /**
     * Unfriend users.
     *
     * @param mail
     * @param auth
     * @param unfriendJson
     * @param response
     * @return friends
     * @throws IOException
     */
    @RequestMapping (
            value = "/unfriend.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String unfriend(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestBody
            String unfriendJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String fid;

        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(unfriendJson);
            LOG.info("Unfriend fid={} from rid={}", map.get("fid"), rid);

            fid = map.get("fid").getText();
            if (!map.containsKey("fid") || fid.length() == 0) {
                LOG.warn("Un-Friend API missing data. Failed to process request, mail={}", mail);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Missing required data.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
                return ErrorEncounteredJson.toJson(errors);
            }

            try {
                if (friendMobileService.unfriend(rid, fid)) {
                    AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
                    friendMobileService.getActiveFriends(rid, availableAccountUpdates);
                    return availableAccountUpdates.asJson();
                } else {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("success", false);
                    return jsonObject.toString();
                }
            } catch(Exception e) {
                LOG.error("Unfriend reason=", e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Something went wrong. Engineers are looking into this.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.SEVERE.getCode());

                return ErrorEncounteredJson.toJson(errors);
            }
        }
    }
}
