package com.receiptofi.mobile.web.controller.api;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.RecentActivityService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * User: hitender
 * Date: 8/9/14 2:22 PM
 */
@Controller
@RequestMapping (value = "/api")
public class RecentUpdateController {
    private static final Logger log = LoggerFactory.getLogger(RecentUpdateController.class);
    private static DateTimeFormatter DATE_FMT = ISODateTimeFormat.dateTime();
    private RecentActivityService recentActivityService;
    private AuthenticateService authenticateService;

    @Autowired
    public RecentUpdateController(RecentActivityService recentActivityService, AuthenticateService authenticateService) {
        this.recentActivityService = recentActivityService;
        this.authenticateService = authenticateService;
    }

    /**
     * Finds all available updates on server
     *
     * @param mail
     * @param auth
     * @param since    - Accepts IOS 8601 date format
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            method = RequestMethod.GET,
            value = "/hasRecentUpdate/{since}",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    String hasNewUpdate(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @PathVariable String since,

            HttpServletResponse response
    ) throws IOException {
        log.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid != null) {
            try {
                return recentActivityService.hasRecentActivities(
                        rid,
                        //Minus a minute to capture most recent update since last check
                        DATE_FMT.parseDateTime(since).minusMinutes(1).toDate()
                ).asJson();
            } catch (IllegalArgumentException e) {
                log.error("Invalid date format={}, should be ISO 8601", since);

                Map<String, String> errors = new HashMap<>();
                errors.put("reason", "invalid date format");
                errors.put("since", since);
                errors.put("systemError", MobileSystemErrorCodeEnum.USER_INPUT.name());
                errors.put("systemErrorCode", MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);
            }
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }
}
