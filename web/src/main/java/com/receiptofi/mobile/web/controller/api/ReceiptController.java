package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.json.JsonReceipt;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.LandingService;
import com.receiptofi.utils.DateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Autowired
    public ReceiptController(LandingService landingService, AuthenticateService authenticateService) {
        this.landingService = landingService;
        this.authenticateService = authenticateService;
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

                for (ReceiptEntity receiptEntity : receiptEntities) {
                    jsonReceipts.add(new JsonReceipt(receiptEntity));
                }
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
                for (ReceiptEntity receiptEntity : receiptEntities) {
                    jsonReceipts.add(new JsonReceipt(receiptEntity));
                }
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
            for (ReceiptEntity receiptEntity : receiptEntities) {
                receipts.add(new JsonReceipt(receiptEntity));
            }
        } catch (Exception e) {
            LOG.error("reason={}", e.getLocalizedMessage(), e);
        }
        return receipts;
    }
}
