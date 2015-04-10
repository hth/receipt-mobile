package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.ExpenseTagMobileService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

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

            if (tagName == null || !expenseTagMobileService.doesExists(rid, tagName)) {
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
}
