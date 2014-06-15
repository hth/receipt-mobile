/**
 *
 */
package com.receiptofi.web.controller.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.receiptofi.domain.MileageEntity;
import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.domain.types.FileTypeEnum;
import com.receiptofi.domain.types.NotificationTypeEnum;
import com.receiptofi.domain.types.UserLevelEnum;
import com.receiptofi.domain.value.ReceiptGrouped;
import com.receiptofi.domain.value.ReceiptGroupedByBizLocation;
import com.receiptofi.service.AccountService;
import com.receiptofi.service.FileDBService;
import com.receiptofi.service.LandingService;
import com.receiptofi.service.MailService;
import com.receiptofi.service.MileageService;
import com.receiptofi.service.NotificationService;
import com.receiptofi.service.ReportService;
import com.receiptofi.service.mobile.LandingViewService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.Maths;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.form.LandingDonutChart;
import com.receiptofi.web.form.LandingForm;
import com.receiptofi.web.form.UploadReceiptImage;
import com.receiptofi.web.helper.ReceiptForMonth;
import com.receiptofi.web.helper.json.Mileages;
import com.receiptofi.web.rest.Base;
import com.receiptofi.web.rest.Header;
import com.receiptofi.web.rest.LandingView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author hitender
 * @since Dec 17, 2012 3:19:01 PM
 */
@Controller
@RequestMapping(value = "/access")
public final class LandingController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(LandingController.class);

    @Autowired LandingService landingService;
    @Autowired FileDBService fileDBService;
    @Autowired MailService mailService;
    @Autowired AccountService accountService;
    @Autowired NotificationService notificationService;
    @Autowired ReportService reportService;
    @Autowired LandingViewService landingViewService;
    @Autowired MileageService mileageService;

	/**
	 * Refers to landing.jsp
	 */
	private static final String NEXT_PAGE_IS_CALLED_LANDING = "/landing";

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/landing", method = RequestMethod.GET)
	public ModelAndView loadForm(
            @ModelAttribute("uploadReceiptImage")
            UploadReceiptImage uploadReceiptImage,

            @ModelAttribute("landingForm")
            LandingForm landingForm
    ) {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("LandingController loadForm user={}, rid={}", receiptUser.getUsername(), receiptUser.getRid());

		ModelAndView modelAndView = new ModelAndView(NEXT_PAGE_IS_CALLED_LANDING);

		List<ReceiptEntity> allReceiptsForThisMonth = landingService.getAllReceiptsForThisMonth(receiptUser.getRid(), time);
        ReceiptForMonth receiptForMonth = landingService.getReceiptForMonth(allReceiptsForThisMonth, time);
        modelAndView.addObject("receiptForMonth", receiptForMonth);
        landingForm.setReceiptForMonth(receiptForMonth);

        long pendingCount = landingService.pendingReceipt(receiptUser.getRid());
        modelAndView.addObject("pendingCount", pendingCount);

        /** Receipt grouped by date */
        log.info("Calculating calendar grouped expense");
        Iterator<ReceiptGrouped> receiptGrouped = landingService.getReceiptGroupedByDate(receiptUser.getRid());
        landingForm.setReceiptGrouped(receiptGrouped);

        /** Lists all the receipt grouped by months */
        List<ReceiptGrouped> receiptGroupedByMonth = landingService.getAllObjectsGroupedByMonth(receiptUser.getRid());
        modelAndView.addObject("months", landingService.addMonthsIfLessThanThree(receiptGroupedByMonth));
        landingForm.setReceiptGroupedByMonths(receiptGroupedByMonth);

        if(receiptUser.getUserLevel().value >= UserLevelEnum.USER_COMMUNITY.value) {
            List<ReceiptGroupedByBizLocation> receiptGroupedByBizLocations = landingService.getAllObjectsGroupedByBizLocation(receiptUser.getRid());
            landingForm.setReceiptGroupedByBizLocations(receiptGroupedByBizLocations);
        }

        /** Used for charting in Expense Analysis tab */
        log.info("Calculating Pie chart - item expense");
        Map<String, BigDecimal> itemExpenses = landingService.getAllItemExpenseForTheYear(receiptUser.getRid());
        modelAndView.addObject("itemExpenses", itemExpenses);

        /** Used for donut chart of each receipts with respect to expense types in TAB 1 */
        log.info("Calculating Donut chart - receipt expense");
        /** bizNames and bizByExpenseTypes added below to landingForm*/
        populateReceiptExpenseDonutChartDetails(landingForm, allReceiptsForThisMonth);

        landingService.computeYearToDateExpense(receiptUser.getRid(), modelAndView);

        /** Notification */
        List<NotificationEntity> notifications = landingService.notifications(receiptUser.getRid());
        landingForm.setNotifications(notifications);

        /** Mileage */
        List<MileageEntity> mileageEntityList = mileageService.getMileageForThisMonth(receiptUser.getRid(), time);
        landingForm.setMileageEntities(mileageEntityList);
        landingForm.setMileageMonthlyTotal(mileageService.mileageTotal(mileageEntityList));

        Mileages mileages = new Mileages();
        mileages.setMileages(mileageService.getMileageForThisMonth(receiptUser.getRid(), time));
        landingForm.setMileages(mileages.asJson());

		PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return modelAndView;
	}

    /**
     * Loads monthly data for the selected month in the calendar
     *
     * @param monthView
     * @param previousOrNext
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/landing/monthly_expenses", method = RequestMethod.POST)
    public @ResponseBody
    ModelAndView monthlyExpenses(
            @RequestParam("monthView") String monthView,
            @RequestParam("buttonClick") String previousOrNext,
            @ModelAttribute("landingForm") LandingForm landingForm
    ) throws IOException {
        ModelAndView modelAndView = new ModelAndView("/z/landingTabs");
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String pattern = "MMM, yyyy";
        DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
        DateTime monthYear = dtf.parseDateTime(monthView);
        if(previousOrNext.equalsIgnoreCase("next")) {
            monthYear = monthYear.minusMonths(1);
        }

        List<ReceiptEntity> allReceiptsForThisMonth = landingService.getAllReceiptsForThisMonth(receiptUser.getRid(), monthYear);
        ReceiptForMonth receiptForMonth = landingService.getReceiptForMonth(allReceiptsForThisMonth, monthYear);
        landingForm.setReceiptForMonth(receiptForMonth);

        /** Used for donut chart of each receipts with respect to expense types in TAB 1*/
        log.info("Calculating Donut chart - receipt expense");
        populateReceiptExpenseDonutChartDetails(landingForm, allReceiptsForThisMonth);
        return modelAndView;
    }

    /**
     * Populate Receipt expense donut chart
     *
     * @param landingForm
     * @param receipts
     */
    private void populateReceiptExpenseDonutChartDetails(LandingForm landingForm, List<ReceiptEntity> receipts) {
        List<LandingDonutChart> bizByExpenseTypes = new ArrayList<>();
        StringBuilder bizNames_sb = new StringBuilder();
        Map<String, Map<String, BigDecimal>> bizByExpenseTypeMap = landingService.allBusinessByExpenseType(receipts);
        for(String bizName : bizByExpenseTypeMap.keySet()) {
            //bizNames_sb.append("'").append(StringUtils.abbreviate(bizName, LandingDonutChart.OFF_SET, LandingDonutChart.MAX_WIDTH)).append("',");
            bizNames_sb.append("'").append(bizName).append("',");

            LandingDonutChart landingDonutChart = LandingDonutChart.newInstance(bizName);

            BigDecimal sum = BigDecimal.ZERO;
            Map<String, BigDecimal> map = bizByExpenseTypeMap.get(bizName);
            for(BigDecimal value : map.values()) {
                sum = Maths.add(sum, value);
            }
            landingDonutChart.setTotal(sum);

            StringBuilder expenseTypes = new StringBuilder();
            StringBuilder expenseValues = new StringBuilder();
            for(String name : map.keySet()) {
                expenseTypes.append("'").append(name).append("',");
                expenseValues.append(map.get(name)).append(",");
            }
            landingDonutChart.setExpenseTags(expenseTypes.toString().substring(0, expenseTypes.toString().length() - 1));
            landingDonutChart.setExpenseValues(expenseValues.toString().substring(0, expenseValues.toString().length() - 1));

            bizByExpenseTypes.add(landingDonutChart);
        }

        landingForm.setBizNames(bizNames_sb.toString().substring(0, bizNames_sb.toString().length() > 0 ? bizNames_sb.toString().length() - 1 : 0));
        landingForm.setBizByExpenseTypes(bizByExpenseTypes);
    }

    /**
     * For uploading Receipts
     *
     * @param httpServletRequest
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.POST, value = "/landing/upload")
    public @ResponseBody
    String upload(HttpServletRequest httpServletRequest) throws IOException {
        DateTime time = DateUtil.now();
        log.info("Upload a receipt");

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String outcome = "{\"success\" : false}";

        boolean isMultipart = ServletFileUpload.isMultipartContent(httpServletRequest);
        if(isMultipart) {
            MultipartHttpServletRequest multipartHttpRequest = WebUtils.getNativeRequest(httpServletRequest, MultipartHttpServletRequest.class);
            final List<MultipartFile> files = multipartHttpRequest.getFiles("qqfile");
            Assert.state(files.size() > 0, "0 files exist");

            /*
             * process files
             */
            for (MultipartFile multipartFile : files) {
                UploadReceiptImage uploadReceiptImage = UploadReceiptImage.newInstance();
                uploadReceiptImage.setFileData(multipartFile);
                uploadReceiptImage.setUserProfileId(receiptUser.getRid());
                uploadReceiptImage.setFileType(FileTypeEnum.RECEIPT);
                try {
                    landingService.uploadReceipt(receiptUser.getRid(), uploadReceiptImage);
                    outcome = "{\"success\" : true, \"uploadMessage\" : \"File uploaded successfully\"}";
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "success");
                } catch (Exception exce) {
                    outcome = "{\"success\" : false, \"uploadMessage\" : \"" + exce.getLocalizedMessage() + "\"}";
                    log.error("Receipt upload reason={}, for rid={}", exce.getLocalizedMessage(), receiptUser.getRid(), exce);
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in receipt save");
                }
            }
        } else {
            //TODO test with IE
            //http://skillshared.blogspot.com/2012/08/java-class-for-valums-ajax-file.html
            log.warn("Look like IE file upload");
            String filename = httpServletRequest.getHeader("X-File-Name");
            InputStream is = httpServletRequest.getInputStream();
        }
        return outcome;
    }

    /**
     * For uploading Receipts
     *
     * @param httpServletRequest
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.POST, value = "/landing/uploadmileage")
    public @ResponseBody
    String uploadMileage(@PathVariable String documentId, HttpServletRequest httpServletRequest) throws IOException {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Upload a mileage");
        String outcome = "{\"success\" : false}";

        boolean isMultipart = ServletFileUpload.isMultipartContent(httpServletRequest);
        if(isMultipart) {
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) httpServletRequest;
            final List<MultipartFile> files = multipartHttpServletRequest.getFiles("qqfile");
            Assert.state(files.size() > 0, "0 files exist");

            /*
             * process files
             */
            for (MultipartFile multipartFile : files) {
                UploadReceiptImage uploadReceiptImage = UploadReceiptImage.newInstance();
                uploadReceiptImage.setFileData(multipartFile);
                uploadReceiptImage.setUserProfileId(receiptUser.getRid());
                uploadReceiptImage.setFileType(FileTypeEnum.MILEAGE);
                try {
                    landingService.appendMileage(documentId, receiptUser.getRid(), uploadReceiptImage);
                    outcome = "{\"success\" : true, \"uploadMessage\" : \"File uploaded successfully\"}";
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "success");
                } catch (Exception exce) {
                    outcome = "{\"success\" : false, \"uploadMessage\" : \"" + exce.getLocalizedMessage() + "\"}";
                    log.error("Receipt upload reason={}, for rid={}", exce.getLocalizedMessage(), receiptUser.getRid(), exce);
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in receipt save");
                }
            }
        } else {
            //TODO test with IE
            //http://skillshared.blogspot.com/2012/08/java-class-for-valums-ajax-file.html
            log.warn("Look like IE file upload");
            String filename = httpServletRequest.getHeader("X-File-Name");
            InputStream is = httpServletRequest.getInputStream();
        }
        return outcome;
    }

    /**
     * Provides user information of home page through a REST URL
     *
     * @param profileId
     * @param authKey
     * @return
     */
    @RequestMapping(value = "/landing/user/{profileId}/auth/{authKey}.xml", method = RequestMethod.GET, produces="application/xml")
    public @ResponseBody
    LandingView loadRest(@PathVariable String profileId, @PathVariable String authKey) {
        DateTime time = DateUtil.now();
        log.info("Web Service : " + profileId);
        return landingView(profileId, authKey, time);
    }

    /**
     * Provides user information of home page through a JSON URL
     *
     * @param profileId
     * @param authKey
     * @return
     */
    @RequestMapping(value = "/landing/user/{profileId}/auth/{authKey}.json", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody
    String loadJSON(@PathVariable String profileId, @PathVariable String authKey) {
        DateTime time = DateUtil.now();
        log.info("JSON : " + profileId);
        Base landingView = landingView(profileId, authKey, time);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();

        String json = StringUtils.EMPTY;
        try {
            json = ow.writeValueAsString(landingView);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }

        return json;
    }

    /**
     * Provides user information of home page through a JSON URL
     *
     * @param profileId
     * @param authKey
     * @return
     */
    @RequestMapping(value = "/landing/user/{profileId}/auth/{authKey}.htm", method = RequestMethod.GET, produces="text/html")
    public @ResponseBody
    String loadHTML(@PathVariable String profileId, @PathVariable String authKey) {
        DateTime time = DateUtil.now();
        log.info("HTML : " + profileId);
        LandingView landingView = landingView(profileId, authKey, time);
        return landingViewService.landingViewHTMLString(landingView);
    }

    /**
     * Populate Landing View object
     *
     * @param profileId
     * @param authKey
     * @param time
     * @return
     */
    private LandingView landingView(String profileId, String authKey, DateTime time) {
        UserProfileEntity userProfile = authenticate(profileId, authKey);
        if(userProfile == null) {
            Header header = getHeaderForProfileOrAuthFailure();
            LandingView landingView = LandingView.newInstance(StringUtils.EMPTY, StringUtils.EMPTY, header);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
            return landingView;
        } else {
            long pendingCount = landingService.pendingReceipt(profileId);
            List<ReceiptEntity> receipts = landingService.getAllReceiptsForThisMonth(profileId, DateUtil.now());

            LandingView landingView = LandingView.newInstance(userProfile.getReceiptUserId(), userProfile.getEmail(), Header.newInstance(getAuth(profileId)));
            landingView.setPendingCount(pendingCount);
            landingView.setReceipts(receipts);
            landingView.setStatus(Header.RESULT.SUCCESS);

            log.info("Rest/JSON Service returned={}, rid={} ",profileId, userProfile.getReceiptUserId());

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), true);
            return landingView;
        }
    }

    @RequestMapping(value = "/landing/report/{monthYear}", method = RequestMethod.GET)
    public @ResponseBody
    String generateReport(@PathVariable String monthYear) {
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Header header = Header.newInstance(getAuth(receiptUser.getRid()));
        String pattern = "MMM, yyyy";
        try {
            DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
            DateTime dateTime = dtf.parseDateTime(monthYear);
            dateTime = dateTime.plusMonths(1).minusDays(1);

            header.setStatus(Header.RESULT.SUCCESS);
            return reportService.monthlyReport(dateTime,
                    receiptUser.getRid(),
                    receiptUser.getUsername(),
                    header
            );
        } catch(IllegalArgumentException iae) {
            header.setMessage("Invalid parameter. Correct format - " + pattern + " [Please provide parameter shown without quotes - 'Jan, 2013']");
            header.setStatus(Header.RESULT.FAILURE);

            return reportService.monthlyReport(DateTime.now().minusYears(40),
                    receiptUser.getRid(),
                    receiptUser.getUsername(),
                    header
            );
        }
    }

    /* http://stackoverflow.com/questions/12117799/spring-mvc-ajax-form-post-handling-possible-methods-and-their-pros-and-cons */
    @RequestMapping(value = "/landing/invite", method = RequestMethod.POST)
    public @ResponseBody
    String invite(@RequestParam(value="emailId") String emailId) {
        //Always lower case the email address
        String invitedUserEmail = StringUtils.lowerCase(emailId);
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Invitation being sent to: " + invitedUserEmail);

        boolean isValid = EmailValidator.getInstance().isValid(invitedUserEmail);
        if(isValid) {
            UserProfileEntity userProfileEntity = accountService.doesUserExists(invitedUserEmail);
            /**
             * Condition when the user does not exists then invite. Also allow re-invite if the user is not active and
             * is not deleted. The second condition could result in a bug when administrator has made the user inactive.
             * Best solution is to add automated re-invite using quartz/cron job. Make sure there is a count kept to limit
             * the number of invite.
             */
            if(userProfileEntity == null || !userProfileEntity.isActive() && !userProfileEntity.isDeleted()) {
                boolean status;
                if(userProfileEntity == null) {
                    status = mailService.sendInvitation(invitedUserEmail, receiptUser.getRid());
                } else {
                    status = mailService.reSendInvitation(invitedUserEmail, receiptUser.getRid());
                }
                if(status) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Invitation sent to '").append(invitedUserEmail).append("'");
                    notificationService.addNotification(sb.toString(), NotificationTypeEnum.MESSAGE, receiptUser.getRid());
                    return "Invitation Sent to: " + invitedUserEmail;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unsuccessful in sending invitation to '").append(invitedUserEmail).append("'");
                    notificationService.addNotification(sb.toString(), NotificationTypeEnum.MESSAGE, receiptUser.getRid());
                    return "Unsuccessful in sending invitation: " + invitedUserEmail;
                }
            } else if(userProfileEntity.isActive() && !userProfileEntity.isDeleted()) {
                log.info(invitedUserEmail + ", already registered. Thanks!");
                return invitedUserEmail + ", already registered. Thanks!";
            } else if(userProfileEntity.isDeleted()) {
                log.info(invitedUserEmail + ", already registered but no longer with us. Appreciate!");

                //Have to send a positive message
                return invitedUserEmail + ", already invited. Appreciate!";
            } else {
                log.info(invitedUserEmail + ", already invited. Thanks!");
                // TODO can put a condition to check or if user is still in invitation mode or has completed registration
                // TODO Based on either condition we can let user recover password or re-send invitation
                return invitedUserEmail + ", already invited. Thanks!";
            }
        } else {
            return "Invalid Email: " + invitedUserEmail;
        }
    }
}