package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.types.ExpenseTagIconEnum;
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
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping (value = "/api/expenseTag")
public class ExpenseTagController {
    private static final Logger LOG = LoggerFactory.getLogger(ExpenseTagController.class);

    private AuthenticateService authenticateService;
    private ExpenseTagMobileService expenseTagMobileService;

    @Value ("${UserProfilePreferenceController.ExpenseTagCountMax}")
    private int expenseTagCountMax;

    @Value ("${UserProfilePreferenceController.ExpenseTagSize}")
    private int expenseTagSize;

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
            value = "/add.json",
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
            String tagName = map.containsKey("tagName") ? map.get("tagName").getText().toUpperCase() : null;
            String tagColor = map.containsKey("tagColor") ? map.get("tagColor").getText() : null;
            String tagIcon = map.containsKey("tagIcon") ? map.get("tagIcon").getText().toUpperCase() : null;

            if (StringUtils.isBlank(tagName) || StringUtils.isBlank(tagColor) ||  StringUtils.isBlank(tagIcon)) {
                LOG.warn("Null tagName={} or tagColor={} or tagIcon={} ", tagName, tagColor, tagIcon);
                Map<String, String> errors = getErrorUserInput("Either Expense Tag or Color received as empty.");
                return ErrorEncounteredJson.toJson(errors);
            }

            if (expenseTagMobileService.doesExists(rid, tagName)) {
                LOG.warn("Expense Tag with expenseTagName={} for rid={} already exists", tagName, rid);
                Map<String, String> errors = getErrorUserInput("Expense Tag already exists.");
                return ErrorEncounteredJson.toJson(errors);
            }

            if (tagName.length() > expenseTagSize) {
                LOG.warn("Expense Tag expenseTagName={} for rid={} length size={} greater", tagName, rid, expenseTagSize);
                Map<String, String> errors = getErrorUserInput("Expense Tag " + tagName + " length should not be greater than " + expenseTagSize + " characters.");
                return ErrorEncounteredJson.toJson(errors);
            }

            if (StringUtils.isBlank(tagIcon)) {
                LOG.warn("Expense Tag tagIcon={} for rid={} is empty", tagName, rid);
                Map<String, String> errors = getErrorUserInput("Expense Tag " + tagName + " is missing icon.");
                return ErrorEncounteredJson.toJson(errors);
            }

            try {
                if (expenseTagMobileService.getExpenseTags(rid).size() < expenseTagCountMax) {
                    expenseTagMobileService.save(tagName, rid, tagColor, ExpenseTagIconEnum.valueOf(tagIcon));
                    return expenseTagMobileService.getUpdates(rid).asJson();
                } else {
                    Map<String, String> errors = getErrorUserInput("Maximum number of TAG(s) allowed " + expenseTagCountMax + ". Could not add " + tagName + ".");
                    return ErrorEncounteredJson.toJson(errors);
                }
            } catch (Exception e) {
                LOG.error("Failure during addition of expense tag rid={} reason={}", rid, e.getLocalizedMessage(), e);
                Map<String, String> errors = getErrorUserInput("Something went wrong. Engineers are looking into this.");
                return ErrorEncounteredJson.toJson(errors);
            }
        }
    }

    /**
     * Update existing expense tag.
     *
     * @param mail
     * @param auth
     * @param requestBodyJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/update.json",
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
            String tagName = map.containsKey("tagName") ? map.get("tagName").getText().toUpperCase() : null;
            String tagColor = map.containsKey("tagColor") ? map.get("tagColor").getText().toUpperCase() : null;
            String tagIcon = map.containsKey("tagIcon") ? map.get("tagIcon").getText().toUpperCase() : null;
            String tagId = map.containsKey("tagId") ? map.get("tagId").getText() : null;

            if (StringUtils.isBlank(tagName) || StringUtils.isBlank(tagColor) ||  StringUtils.isBlank(tagIcon) || StringUtils.isBlank(tagId)) {
                LOG.warn("Null tagName={} or tagColor={} or tagIcon={} or tagId={}", tagName, tagColor, tagIcon, tagId);
                Map<String, String> errors = getErrorSevere("Either Expense Tag or Color or Id received as empty.");
                return ErrorEncounteredJson.toJson(errors);
            } else if (tagName.length() > expenseTagSize) {
                LOG.warn("Expense Tag expenseTagName={} for rid={} length size={} greater", tagName, rid, expenseTagSize);
                Map<String, String> errors = getErrorUserInput("Expense Tag " + tagName + " length should not be greater than " + expenseTagSize + " characters.");
                return ErrorEncounteredJson.toJson(errors);
            } else if (StringUtils.isBlank(tagIcon)) {
                LOG.warn("Expense Tag tagIcon={} for rid={} is empty", tagName, rid);
                Map<String, String> errors = getErrorUserInput("Expense Tag " + tagName + " is missing icon.");
                return ErrorEncounteredJson.toJson(errors);
            } else if (null == expenseTagMobileService.getExpenseTag(rid, tagId)) {
                LOG.warn("Expense Tag with expenseTagName={} for rid={} could not be found", tagName, rid);
                Map<String, String> errors = getErrorSevere("Expense Tag does not exists.");
                return ErrorEncounteredJson.toJson(errors);
            } else if (expenseTagMobileService.doesExists(rid, tagName)) {
                ExpenseTagEntity existingET = expenseTagMobileService.getExpenseTag(rid, tagId);
                if (existingET.getTagColor().equalsIgnoreCase(tagColor) && existingET.getIcon().getName().equalsIgnoreCase(tagIcon)) {
                    LOG.warn("No change detected for expenseTagName={} for rid={}", tagName, rid);
                    Map<String, String> errors = getErrorUserInput("Expense Tag already exists.");
                    return ErrorEncounteredJson.toJson(errors);
                } else {
                    expenseTagMobileService.update(tagId, tagName, rid, tagColor, ExpenseTagIconEnum.valueOf(tagIcon));
                    return expenseTagMobileService.getUpdates(rid).asJson();
                }
            } else {
                try {
                    expenseTagMobileService.update(tagId, tagName, rid, tagColor, ExpenseTagIconEnum.valueOf(tagIcon));
                    return expenseTagMobileService.getUpdates(rid).asJson();
                } catch (Exception e) {
                    LOG.error("Failure during expense tag update rid={} reason={}", rid, e.getLocalizedMessage(), e);
                    Map<String, String> errors = getErrorUserInput("Something went wrong. Engineers are looking into this.");
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
            value = "/delete.json",
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
                Map<String, String> errors = getErrorSevere("Either Expense Tag or Color or Id received as empty.");
                return ErrorEncounteredJson.toJson(errors);
            } else if (null == expenseTagMobileService.getExpenseTag(rid, tagId, tagName)) {
                LOG.warn("Expense Tag with expenseTagName={} for rid={} does not exists", tagName, rid);
                Map<String, String> errors = getErrorSevere("Expense Tag does not exists.");
                return ErrorEncounteredJson.toJson(errors);
            } else {
                boolean result = expenseTagMobileService.softDeleteExpenseTag(tagId, tagName, rid);
                if (result) {
                    return expenseTagMobileService.getUpdates(rid).asJson();
                } else {
                    LOG.error("Failure to delete expense tag rid={} tagId={}", rid, tagId);
                    Map<String, String> errors = getErrorUserInput("Something went wrong. Engineers are looking into this.");
                    return ErrorEncounteredJson.toJson(errors);
                }
            }
        }
    }

    static Map<String, String> getErrorUserInput(String reason) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ErrorEncounteredJson.REASON, reason);
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());
        return errors;
    }

    static Map<String, String> getErrorSevere(String reason) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ErrorEncounteredJson.REASON, reason);
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.SEVERE.name());
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.SEVERE.getCode());
        return errors;
    }
}
