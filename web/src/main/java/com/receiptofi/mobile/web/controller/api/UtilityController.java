package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.mobile.domain.UnprocessedDocuments;
import com.receiptofi.mobile.domain.UserAccess;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.LandingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 6/8/14 2:31 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal"
})
@Controller
@RequestMapping (value = "/api")
public final class UtilityController {
    private static final Logger LOG = LoggerFactory.getLogger(UtilityController.class);

    private AuthenticateService authenticateService;
    private LandingService landingService;

    @Autowired
    public UtilityController(AuthenticateService authenticateService, LandingService landingService) {
        this.authenticateService = authenticateService;
        this.landingService = landingService;
    }

    @RequestMapping (
            value = "/hasAccess",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    UserAccess hasAccess(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, "*********");
        if (authenticateService.hasAccess(mail, auth)) {
            return UserAccess.newInstance("granted");
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return null;
        }
    }

    @RequestMapping (
            value = "/unprocessed",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    UnprocessedDocuments unprocessedDocuments(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return null;
        } else {
            return UnprocessedDocuments.newInstance(landingService.pendingReceipt(rid));
        }
    }
}
