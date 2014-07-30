package com.receiptofi.web.controller.ajax;

import com.receiptofi.social.domain.site.ReceiptUser;
import com.receiptofi.service.DocumentUpdateService;
import com.receiptofi.service.FetcherService;
import com.receiptofi.service.LandingService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.Formatter;
import com.receiptofi.utils.HashText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: hitender
 * Date: 4/19/13
 * Time: 11:44 PM
 */
@Controller
@RequestMapping(value = "/ws/r")
public final class ReceiptWebService {
    private static final Logger log = LoggerFactory.getLogger(ReceiptWebService.class);

    @Autowired private FetcherService fetcherService;
    @Autowired private LandingService landingService;
    @Autowired private DocumentUpdateService documentUpdateService;

    /**
     * @param businessName
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TECHNICIAN', 'ROLE_SUPERVISOR')")
    @RequestMapping(
            value = "/find_company",
            method = RequestMethod.GET,
            headers = "Accept=application/json",
            produces = "application/json")
    public
    @ResponseBody
    Set<String> searchBusinessWithBusinessName(@RequestParam("term") String businessName) {
        try {
            return fetcherService.findDistinctBizName(StringUtils.stripToEmpty(businessName));
        } catch (Exception fetchBusinessName) {
            log.error("Error fetching business number, error={}", fetchBusinessName);
            return new HashSet<>();
        }
    }

    /**
     * @param bizAddress
     * @param businessName
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TECHNICIAN', 'ROLE_SUPERVISOR')")
    @RequestMapping(
            value = "/find_address",
            method = RequestMethod.GET,
            headers = "Accept=application/json",
            produces = "application/json")
    public
    @ResponseBody
    Set<String> searchBiz(@RequestParam("term") String bizAddress, @RequestParam("nameParam") String businessName) {
        try {
            return fetcherService.findDistinctBizAddress(StringUtils.stripToEmpty(bizAddress), StringUtils.stripToEmpty(businessName));
        } catch (Exception fetchBusinessAddress) {
            log.error("Error fetching business address, error={}", fetchBusinessAddress);
            return new HashSet<>();
        }
    }

    /**
     * @param bizPhone
     * @param businessName
     * @param bizAddress
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TECHNICIAN', 'ROLE_SUPERVISOR')")
    @RequestMapping(
            value = "/find_phone",
            method = RequestMethod.GET,
            headers = "Accept=application/json",
            produces = "application/json")
    public
    @ResponseBody
    Set<String> searchPhone(@RequestParam("term") String bizPhone, @RequestParam("nameParam") String businessName, @RequestParam("addressParam") String bizAddress) {
        try {
            return fetcherService.findDistinctBizPhone(
                    StringUtils.stripToEmpty(bizPhone),
                    StringUtils.stripToEmpty(bizAddress),
                    StringUtils.stripToEmpty(businessName)
            );
        } catch (Exception fetchingPhone) {
            log.error("Error fetching phone number, error={}", fetchingPhone);
            return new HashSet<>();
        }
    }

    /**
     * @param itemName
     * @param businessName
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TECHNICIAN', 'ROLE_SUPERVISOR')")
    @RequestMapping(
            value = "/find_item",
            method = RequestMethod.GET,
            headers = "Accept=application/json",
            produces = "application/json")
    public
    @ResponseBody
    Set<String> searchItem(@RequestParam("term") String itemName, @RequestParam("nameParam") String businessName) {
        try {
            return fetcherService.findDistinctItems(StringUtils.stripToEmpty(itemName), StringUtils.stripToEmpty(businessName));
        } catch (Exception fetchingItems) {
            log.error("Error fetching items, error={}", fetchingItems);
            return new HashSet<>();
        }
    }

    /**
     * Gets all the pending receipt after a receipt is successfully uploaded
     *
     * @return
     * @throws IOException
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(
            value = "/pending",
            method = RequestMethod.POST,
            headers = "Accept=application/json",
            produces = "application/json")
    public
    @ResponseBody
    long pendingReceipts() {
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            return landingService.pendingReceipt(receiptUser.getRid());
        } catch (Exception pendingReceipt) {
            log.error("Error fetching items, error={}", pendingReceipt);
            return 0;
        }
    }

    /**
     * Check if a duplicate receipt exists for the user
     *
     * @param date
     * @param total
     * @param userProfileId
     * @return
     * @throws IOException
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TECHNICIAN', 'ROLE_SUPERVISOR')")
    @RequestMapping(
            value = "/check_for_duplicate",
            method = RequestMethod.GET,
            headers = "Accept=application/json",
            produces = "application/json")
    //@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Duplicate Account")  // 409 //TODO something to think about
    public
    @ResponseBody
    boolean checkForDuplicate(
            @RequestParam("date") String date,
            @RequestParam("total") String total,
            @RequestParam("userProfileId") String userProfileId
    ) throws IOException, ParseException, NumberFormatException {
        try {
            Date receiptDate = DateUtil.getDateFromString(StringUtils.stripToEmpty(date));
            Double receiptTotal = Formatter.getCurrencyFormatted(StringUtils.stripToEmpty(total)).doubleValue();

            String checkSum = HashText.calculateChecksumForNotDeleted(
                    StringUtils.stripToEmpty(userProfileId),
                    receiptDate,
                    receiptTotal
            );

            return documentUpdateService.hasReceiptWithSimilarChecksum(checkSum);
        } catch (ParseException parseException) {
            log.error("Ajax checkForDuplicate failed to parse total, error={}", parseException);
            throw parseException;
        }
    }

    /**
     * Update the orientation of the image
     *
     * @param fileSystemId
     * @param imageOrientation
     * @param blobId
     * @param userProfileId
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TECHNICIAN', 'ROLE_SUPERVISOR')")
    @RequestMapping(
            value = "/change_fs_image_orientation",
            method = RequestMethod.POST,
            headers = "Accept=application/json",
            produces = "application/json")
    public
    @ResponseBody
    boolean changeFSImageOrientation(
            @RequestParam("fileSystemId") String fileSystemId,
            @RequestParam("orientation") String imageOrientation,
            @RequestParam("blobId") String blobId,
            @RequestParam("userProfileId") String userProfileId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(request.isUserInRole("ROLE_ADMIN") ||
                request.isUserInRole("ROLE_TECHNICIAN") ||
                request.isUserInRole("ROLE_SUPERVISOR") ||
                userProfileId.equalsIgnoreCase(receiptUser.getRid()))
        {
            try {
                fetcherService.changeFSImageOrientation(
                        StringUtils.stripToEmpty(fileSystemId),
                        Integer.parseInt(StringUtils.stripToEmpty(imageOrientation)),
                        blobId
                );
                return true;
            } catch (Exception failedToChangeImageOrientation) {
                //Do nothing with the error message
                log.error("Failed to change orientation of the image, error={}", failedToChangeImageOrientation);
                return false;
            }
        } else {
            response.sendError(SC_FORBIDDEN, "Cannot access directly");
            return false;
        }
    }
}
