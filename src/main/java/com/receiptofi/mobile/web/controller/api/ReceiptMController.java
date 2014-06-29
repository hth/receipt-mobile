package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.LandingService;
import com.receiptofi.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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
@RequestMapping(value = "/api")
public class ReceiptMController {
    private static final Logger log = LoggerFactory.getLogger(ReceiptMController.class);

    @Autowired LandingService landingService;

    @Autowired AuthenticateService authenticateService;

    @RequestMapping(
            value = "/ytdReceipts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    List<ReceiptEntity> ytdReceipts(
            @RequestHeader("X-R-MAIL")
            String mail,

            @RequestHeader("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        log.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if(rid != null) {
            return landingService.getAllReceiptsForTheYear(rid, DateUtil.startOfYear());
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }

    @RequestMapping(
            value = "/allReceipts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    List<ReceiptEntity> allReceipts(
            @RequestHeader("X-R-MAIL")
            String mail,

            @RequestHeader("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        log.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if(rid != null) {
            return landingService.getAllReceipts(rid);
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }

    @RequestMapping(
            value = "/thisMonthReceipts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    List<ReceiptEntity> thisMonthReceipts(
            @RequestHeader("X-R-MAIL")
            String mail,

            @RequestHeader("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        log.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if(rid != null) {
            return landingService.getAllReceiptsForThisMonth(rid, DateUtil.now());
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }
}