package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.json.JsonReceipt;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.ExpenseTagMobileService;
import com.receiptofi.mobile.service.ReceiptMobileService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import com.receiptofi.service.LandingService;
import com.receiptofi.utils.DateUtil;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 6/11/14 11:34 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api")
public class ReceiptController {
    private static final Logger LOG = LoggerFactory.getLogger(ReceiptController.class);

    private LandingService landingService;
    private AuthenticateService authenticateService;
    private ReceiptMobileService receiptMobileService;
    private ExpenseTagMobileService expenseTagMobileService;

    @Autowired
    public ReceiptController(
            LandingService landingService,
            AuthenticateService authenticateService,
            ReceiptMobileService receiptMobileService,
            ExpenseTagMobileService expenseTagMobileService
    ) {
        this.landingService = landingService;
        this.authenticateService = authenticateService;
        this.receiptMobileService = receiptMobileService;
        this.expenseTagMobileService = expenseTagMobileService;
    }

    @RequestMapping (
            value = "/ytdReceipts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<JsonReceipt> ytdReceipts(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return Collections.emptyList();
        } else {
            List<JsonReceipt> jsonReceipts = new ArrayList<>();
            try {
                List<ReceiptEntity> receipts = landingService.getAllReceiptsForTheYear(rid, DateUtil.startOfYear());
                jsonReceipts.addAll(receipts.stream().map(JsonReceipt::new).collect(Collectors.toList()));
            } catch (Exception e) {
                LOG.error("Found error reason={}", e.getLocalizedMessage(), e);
            }
            return jsonReceipts;
        }
    }

    @RequestMapping (
            value = "/allReceipts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<JsonReceipt> allReceipts(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return Collections.emptyList();
        } else {
            List<JsonReceipt> jsonReceipts = new ArrayList<>();
            try {
                List<ReceiptEntity> receiptEntities = landingService.getAllReceipts(rid);
                jsonReceipts.addAll(receiptEntities.stream().map(JsonReceipt::new).collect(Collectors.toList()));
            } catch (Exception e) {
                LOG.error("reason={}", e.getLocalizedMessage(), e);
            }
            return jsonReceipts;
        }
    }

    @RequestMapping (
            value = "/thisMonthReceipts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<JsonReceipt> thisMonthReceipts(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return Collections.emptyList();
        }

        List<JsonReceipt> receipts = new ArrayList<>();
        try {
            List<ReceiptEntity> receiptEntities = landingService.getAllReceiptsForThisMonth(rid, DateUtil.now());
            receipts.addAll(receiptEntities.stream().map(JsonReceipt::new).collect(Collectors.toList()));
        } catch (Exception e) {
            LOG.error("reason={}", e.getLocalizedMessage(), e);
        }
        return receipts;
    }

    /**
     * Set of actions to be performed on receipt.
     *
     * @param mail
     * @param auth
     * @param requestBodyJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            method = RequestMethod.POST,
            value = "/receiptAction",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String upload(
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
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
            String expenseTagId = map.containsKey("expenseTagId") ? map.get("expenseTagId").getText() : null;
            String notes = map.containsKey("notes") ? map.get("notes").getText() : null;
            String recheck = map.containsKey("recheck") ? map.get("recheck").getText() : null;
            String receiptId = map.containsKey("receiptId") ? map.get("receiptId").getText() : null;

            ReceiptEntity receipt = receiptMobileService.findReceipt(receiptId, rid);
            if (receipt == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "NotFound");
                return null;
            } else {
                if (StringUtils.isNotBlank(notes)) {
                    receiptMobileService.saveComment(notes, receipt);
                }

                if (StringUtils.isNotBlank(expenseTagId)) {
                    ExpenseTagEntity expenseTag = expenseTagMobileService.getExpenseTag(rid, expenseTagId);
                    if (null != expenseTag) {
                        receiptMobileService.updateReceiptExpenseTag(receipt, expenseTagId);
                    } else {
                        LOG.error("Could not find expenseTagId={} under user rid={}. Should never happen.",
                                expenseTagId, rid);

                        Map<String, String> errors = new HashMap<>();
                        errors.put(ErrorEncounteredJson.REASON, "Could not bind Expense Tag with Receipt.");
                        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.SEVERE.name());
                        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.SEVERE.getCode());

                        return ErrorEncounteredJson.toJson(errors);
                    }
                }

                try {
                    if (StringUtils.isNotBlank(recheck) && ("RECHECK").equals(recheck)) {
                        receiptMobileService.recheck(receiptId, rid);
                    }
                    return receiptMobileService.getUpdateForChangedReceipt(
                            receiptMobileService.findReceiptForMobile(receiptId, rid)
                    ).asJson();
                } catch (Exception e) {
                    LOG.error("Failure during receiptAction rid={} reason={}", rid, e.getLocalizedMessage(), e);

                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Something went wrong. Engineers are looking into this.");
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                    return ErrorEncounteredJson.toJson(errors);
                }
            }
        }
    }

    /**
     * Set of actions to be performed on receipt.
     *
     * @param mail
     * @param auth
     * @param requestBodyJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            method = RequestMethod.POST,
            value = "/receipt/delete",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String delete(
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
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
            String receiptId = map.get("receiptId").getText();
            LOG.info("Delete receiptId={}", receiptId);

            try {
                boolean result = receiptMobileService.deleteReceipt(receiptId, rid);
                if (result) {
                    ReceiptEntity receipt = receiptMobileService.findReceiptForMobile(receiptId, rid);
                    if (null != receipt) {
                        return receiptMobileService.getUpdateForChangedReceipt(receipt).asJson();
                    } else {
                        LOG.warn("Could not find receipt with id={} rid={} returning all receipts", receiptId, rid);
                        /** Since cannot find this receipt, instead return all the receipts. */
                        return receiptMobileService.getUpdateForAllReceipt(rid).asJson();
                    }
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Failed to delete receipt.");
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.SEVERE.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.SEVERE.getCode());

                    return ErrorEncounteredJson.toJson(errors);

                }
            } catch (Exception e) {
                LOG.error("Failure during receipt delete rid={} reason={}", rid, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put(ErrorEncounteredJson.REASON, "Something went wrong. Engineers are looking into this.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);
            }
        }
    }
}
