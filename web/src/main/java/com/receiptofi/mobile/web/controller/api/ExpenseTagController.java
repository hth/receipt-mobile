package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.ExpenseTagMobileService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
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
 * Date: 3/26/15 10:13 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api")
public class ExpenseTagController {
    private static final Logger LOG = LoggerFactory.getLogger(ExpenseTagController.class);

    private AuthenticateService authenticateService;
    private ExpenseTagMobileService expenseTagMobileService;

    @Autowired
    public ExpenseTagController(
            AuthenticateService authenticateService,
            ExpenseTagMobileService expenseTagMobileService
    ) {
        this.authenticateService = authenticateService;
        this.expenseTagMobileService = expenseTagMobileService;
    }

    /**
     * Create new expense tag.
     *
     * @param mail
     * @param auth
     * @param requestBodyJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/addExpenseTag.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String addExpenseTag(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestBody
            String requestBodyJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
            String tagName = map.containsKey("tagName") ? map.get("tagName").getText() : null;
            String tagColor = map.containsKey("tagColor") ? map.get("tagColor").getText() : null;

            if (StringUtils.isBlank(tagName) || StringUtils.isBlank(tagColor)) {
                LOG.warn("Null tagName={} or tagColor={}", tagName, tagColor);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Either Expense Tag or Color received as empty.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);
            } else if (expenseTagMobileService.doesExists(rid, tagName)) {
                LOG.warn("Expense Tag with expenseTagName={} for rid={} already exists", tagName, rid);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Expense Tag already exists.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);
            } else {
                try {
                    expenseTagMobileService.save(tagName, rid, tagColor);
                    return expenseTagMobileService.getUpdates(rid).asJson();
                } catch (Exception e) {
                    LOG.error("Failure during recheck rid={} reason={}", rid, e.getLocalizedMessage(), e);

                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Something went wrong.");
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                    return ErrorEncounteredJson.toJson(errors);
                }
            }
        }
    }

    /**
     * Create new expense tag.
     *
     * @param mail
     * @param auth
     * @param requestBodyJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/updateExpenseTag.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String updateExpenseTag(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestBody
            String requestBodyJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
            String tagName = map.containsKey("tagName") ? map.get("tagName").getText() : null;
            String tagColor = map.containsKey("tagColor") ? map.get("tagColor").getText() : null;
            String tagId = map.containsKey("tagId") ? map.get("tagId").getText() : null;


            if (StringUtils.isBlank(tagName) || StringUtils.isBlank(tagColor) || StringUtils.isBlank(tagId)) {
                LOG.warn("Null tagName={} or tagColor={} or tagId={}", tagName, tagColor, tagId);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Either Expense Tag or Color or Id received as empty.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.SEVERE.getCode());

                return ErrorEncounteredJson.toJson(errors);
            } else if (null == expenseTagMobileService.getExpenseTag(rid, tagId)) {
                LOG.warn("Expense Tag with expenseTagName={} for rid={} already exists", tagName, rid);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Expense Tag does not exists.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.SEVERE.getCode());

                return ErrorEncounteredJson.toJson(errors);
            } else {
                try {
                    expenseTagMobileService.update(tagId, tagName, rid, tagColor);
                    return expenseTagMobileService.getUpdates(rid).asJson();
                } catch (Exception e) {
                    LOG.error("Failure during recheck rid={} reason={}", rid, e.getLocalizedMessage(), e);

                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Something went wrong.");
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                    return ErrorEncounteredJson.toJson(errors);
                }
            }
        }
    }

    /**
     * Delete expense tag.
     *
     * @param mail
     * @param auth
     * @param requestBodyJson
     * @param response
     * @return
     */
    @RequestMapping (
            value = "/deleteExpenseTag.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String deleteExpenseTag(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestBody
            String requestBodyJson,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
            String tagName = map.containsKey("tagName") ? map.get("tagName").getText() : null;
            String tagId = map.containsKey("tagId") ? map.get("tagId").getText() : null;


            if (StringUtils.isBlank(tagName) || StringUtils.isBlank(tagId)) {
                LOG.warn("Null tagName={} or tagId={}", tagName, tagId);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Either Expense Tag or Color or Id received as empty.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.SEVERE.getCode());

                return ErrorEncounteredJson.toJson(errors);
            } else if (null == expenseTagMobileService.getExpenseTag(rid, tagId)) {
                LOG.warn("Expense Tag with expenseTagName={} for rid={} already exists", tagName, rid);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Expense Tag does not exists.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.SEVERE.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.SEVERE.getCode());

                return ErrorEncounteredJson.toJson(errors);
            } else {
                try {
                    expenseTagMobileService.deleteExpenseTag(tagId, tagName, rid);
                    return expenseTagMobileService.getUpdates(rid).asJson();
                } catch (Exception e) {
                    LOG.error("Failure during recheck rid={} reason={}", rid, e.getLocalizedMessage(), e);

                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Something went wrong.");
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                    return ErrorEncounteredJson.toJson(errors);
                }
            }
        }

    }
}
