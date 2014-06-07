package com.receiptofi.web.controller.ajax;

import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.service.MileageService;
import com.receiptofi.service.ReceiptService;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.TextInputScrubber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Update for all Ajax Calls
 *
 * User: hitender
 * Date: 7/22/13
 * Time: 8:57 PM
 */
@Controller
@RequestMapping(value = "/ws/nc")
public final class NotesAndCommentsWebService {
     private static final Logger log = LoggerFactory.getLogger(NotesAndCommentsWebService.class);

    @Autowired private ReceiptService receiptService;
    @Autowired private MileageService mileageService;

    @RequestMapping(value = "/rn", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody
    boolean saveReceiptNotes(@RequestBody String body) throws IOException {
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Receipt notes updated by userProfileId={}", receiptUser.getRid());
        Map<String, String> map = ParseJsonStringToMap.jsonStringToMap(body);
        return receiptService.updateReceiptNotes(TextInputScrubber.scrub(map.get("notes")), map.get("receiptId"), receiptUser.getRid());
    }

    @RequestMapping(value ="/mn", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody
    boolean saveMileageNotes(@RequestBody String body) throws IOException {
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Note updated by userProfileId={}", receiptUser.getRid());
        Map<String, String> map = ParseJsonStringToMap.jsonStringToMap(body);
        return mileageService.updateMileageNotes(TextInputScrubber.scrub(map.get("notes")), map.get("mileageId"), receiptUser.getRid());
    }

    @RequestMapping(value = "/rc", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody
    boolean saveReceiptRecheckComment(@RequestBody String body) throws IOException {
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Receipt recheck comment updated by userProfileId={}", receiptUser.getRid());
        Map<String, String> map = ParseJsonStringToMap.jsonStringToMap(body);
        return receiptService.updateReceiptComment(TextInputScrubber.scrub(map.get("notes")), map.get("receiptId"), receiptUser.getRid());
    }

    @RequestMapping(value = "/dc", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody
    boolean saveDocumentRecheckComment(@RequestBody String body) throws IOException {
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Document recheck comment updated by userProfileId={}", receiptUser.getRid());
        Map<String, String> map = ParseJsonStringToMap.jsonStringToMap(body);
        return receiptService.updateDocumentComment(TextInputScrubber.scrub(map.get("notes")), map.get("documentId"));
    }
}
