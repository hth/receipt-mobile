package com.receiptofi.mobile.web.controller.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: hitender
 * Date: 9/9/14 11:37 PM
 */
@Controller
@RequestMapping (value = "/api")
public final class DetailedReceipt {
    private static final Logger LOG = LoggerFactory.getLogger(DetailedReceipt.class);

    @SuppressWarnings ({"PMD.BeanMembersShouldSerialize"})
    private AuthenticateService authenticateService;

    @Autowired
    public DetailedReceipt(AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    /**
     * Gets detailed receipt.
     *
     * @param mail
     * @param auth
     * @param receiptId
     * @param response
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping (
            method = RequestMethod.GET,
            value = "/update",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public
    @ResponseBody
    String getDetailedReceipt(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestParam (value = "receiptId", required = true)
            String receiptId,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid != null) {
            try {
                return "";
            } catch (Exception e) {
                LOG.error("fetching update for receipt={} failed reason={}", receiptId, e.getLocalizedMessage(), e);

                Map<String, String> errors = new HashMap<>();
                errors.put("reason", "something went wrong");
                errors.put("receiptId", receiptId);
                errors.put("systemError", MobileSystemErrorCodeEnum.USER_INPUT.name());
                errors.put("systemErrorCode", MobileSystemErrorCodeEnum.USER_INPUT.getCode());

                return ErrorEncounteredJson.toJson(errors);
            }
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }
}
