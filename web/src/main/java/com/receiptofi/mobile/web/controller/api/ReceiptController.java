package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.json.JsonReceipt;
import com.receiptofi.mobile.service.AuthenticateService;
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

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    public ReceiptController(
            LandingService landingService,
            AuthenticateService authenticateService,
            ReceiptMobileService receiptMobileService
    ) {
        this.landingService = landingService;
        this.authenticateService = authenticateService;
        this.receiptMobileService = receiptMobileService;
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
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return Collections.emptyList();
        } else {
            List<JsonReceipt> jsonReceipts = new ArrayList<>();
            try {
                List<ReceiptEntity> receiptEntities =
                        landingService.getAllReceiptsForTheYear(rid, DateUtil.startOfYear());

                jsonReceipts.addAll(receiptEntities.stream().map(JsonReceipt::new).collect(Collectors.toList()));
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
        if (rid == null) {
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
        if (rid == null) {
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
     * @param request
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

            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
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
                    receiptMobileService.updateReceiptExpenseTag(receipt, expenseTagId);
                }

                try {
                    if (StringUtils.isNotBlank(recheck) && ("RECHECK").equals(recheck)) {
                        receiptMobileService.reopen(receiptId, rid);
                    }
                    return receiptMobileService.getUpdateForChangedReceipt(
                            receiptMobileService.findReceiptForMobile(receiptId, rid)
                    ).asJson();
                } catch (Exception e) {
                    LOG.error("Failure during recheck rid={} reason={}", rid, e.getLocalizedMessage(), e);

                    Map<String, String> errors = new HashMap<>();
                    errors.put(ErrorEncounteredJson.REASON, "Something went wrong");
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MobileSystemErrorCodeEnum.USER_INPUT.name());
                    errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                    return ErrorEncounteredJson.toJson(errors);
                }
            }
        }
    }
}
