package com.receiptofi.mobile.web.controller.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.mobile.domain.mapping.Receipt;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.LandingService;
import com.receiptofi.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: hitender
 * Date: 6/11/14 11:34 PM
 */
@Controller
@RequestMapping (value = "/api")
public final class ReceiptController {
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
    public
    @ResponseBody
    List<Receipt> ytdReceipts(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid != null) {
            List<Receipt> receipts = new ArrayList<>();
            try {
                List<ReceiptEntity> receiptEntities = landingService.getAllReceiptsForTheYear(rid, DateUtil.startOfYear());
                for (ReceiptEntity receiptEntity : receiptEntities) {
                    receipts.add(Receipt.newInstance(receiptEntity));
                }
            } catch (Exception e) {
                LOG.error("found error message={}", e.getLocalizedMessage(), e);
            }
            return receipts;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }

    @RequestMapping (
            value = "/allReceipts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    List<Receipt> allReceipts(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid != null) {
            List<Receipt> receipts = new ArrayList<>();
            try {
                List<ReceiptEntity> receiptEntities = landingService.getAllReceipts(rid);
                for (ReceiptEntity receiptEntity : receiptEntities) {
                    receipts.add(Receipt.newInstance(receiptEntity));
                }
            } catch (Exception e) {
                LOG.error("found error message={}", e.getLocalizedMessage(), e);
            }
            return receipts;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }

    @RequestMapping (
            value = "/thisMonthReceipts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    List<Receipt> thisMonthReceipts(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid != null) {
            List<Receipt> receipts = new ArrayList<>();
            try {
                List<ReceiptEntity> receiptEntities = landingService.getAllReceiptsForThisMonth(rid, DateUtil.now());
                for (ReceiptEntity receiptEntity : receiptEntities) {
                    receipts.add(Receipt.newInstance(receiptEntity));
                }
            } catch (Exception e) {
                LOG.error("found error message={}", e.getLocalizedMessage(), e);
            }
            return receipts;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }
}
