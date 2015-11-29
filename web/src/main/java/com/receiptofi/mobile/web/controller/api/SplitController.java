package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.json.JsonFriend;
import com.receiptofi.domain.types.SplitActionEnum;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.DeviceService;
import com.receiptofi.service.FriendService;
import com.receiptofi.service.ReceiptService;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 9/25/15 4:48 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Controller
@RequestMapping (value = "/api/split")
public class SplitController {
    private static final Logger LOG = LoggerFactory.getLogger(SplitController.class);

    private FriendService friendService;
    private AuthenticateService authenticateService;
    private ReceiptService receiptService;
    private DeviceService deviceService;

    @Autowired
    public SplitController(
            FriendService friendService,
            AuthenticateService authenticateService,
            ReceiptService receiptService,
            DeviceService deviceService
    ) {
        this.friendService = friendService;
        this.authenticateService = authenticateService;
        this.receiptService = receiptService;
        this.deviceService = deviceService;
    }

    @Timed
    @ExceptionMetered
    @PreAuthorize ("hasRole('ROLE_USER')")
    @RequestMapping (
            value = "/friends",
            method = RequestMethod.GET,
            headers = "Accept=application/json",
            produces = "application/json"
    )
    @ResponseBody
    public Collection<JsonFriend> getFriends(
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
            return friendService.getFriends(rid).values();
        }
    }

    /**
     * Original owner of the receipt can add or remove friends from split.
     *
     * @param mail
     * @param auth
     * @param deviceId
     * @param requestBodyJson
     * @param httpServletResponse
     * @return
     * @throws IOException
     */
    @Timed
    @ExceptionMetered
    @PreAuthorize ("hasRole('ROLE_USER')")
    @RequestMapping (
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ResponseBody
    public String split(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestHeader ("X-R-DID")
            String deviceId,

            @RequestBody
            String requestBodyJson,

            HttpServletResponse httpServletResponse
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(requestBodyJson);
            String fidRemove = map.containsKey("fidRemove") ? map.get("fidRemove").getText() : null;
            String fidAdd = map.containsKey("fidAdd") ? map.get("fidAdd").getText() : null;
            String receiptId = map.containsKey("receiptId") ? map.get("receiptId").getText() : null;
            LOG.debug("Receipt id={} fidRemove={} fidAdd={}", receiptId, fidRemove, fidAdd);

            List<String> removeFids = populateFids(fidRemove);
            List<String> addFids = populateFids(fidAdd);

            ReceiptEntity receipt = receiptService.findReceipt(receiptId, rid);
            if (null == receipt) {
                LOG.warn("No Receipt found to Split with receiptId={}", receiptId);
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "NotFound");
                return null;
            } else {
                for (String friendId : removeFids) {
                    receiptService.splitAction(friendId, SplitActionEnum.R, receipt);
                }

                for (String friendId : addFids) {
                    receiptService.splitAction(friendId, SplitActionEnum.A, receipt);
                }
            }

            return deviceService.getUpdates(rid, deviceId).asJson();
        }
    }

    private List<String> populateFids(String fids) {
        List<String> fidList = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(fids, ",");
        while (stringTokenizer.hasMoreTokens()) {
            fidList.add(stringTokenizer.nextToken());
        }
        return fidList;
    }
}
