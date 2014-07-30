/**
 *
 */
package com.receiptofi.web.controller.access;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.repository.BizNameManager;
import com.receiptofi.service.ReceiptService;
import com.receiptofi.service.UserProfilePreferenceService;
import com.receiptofi.social.domain.site.ReceiptUser;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.web.form.ReceiptForm;
import com.receiptofi.web.helper.ReceiptLandingView;
import com.receiptofi.web.rest.Header;
import com.receiptofi.web.util.PerformanceProfiling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import org.joda.time.DateTime;

/**
 * @author hitender
 * @since Jan 1, 2013 11:55:19 AM
 *
 */
@Controller
@RequestMapping(value = "/access/receipt")
public final class ReceiptController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(ReceiptController.class);

	private static String NEXT_PAGE = "/receipt";
    private static String NEXT_PAGE_BY_BIZ = "/receiptByBiz";

    @Autowired private ReceiptService receiptService;
    @Autowired private BizNameManager bizNameManager;
    @Autowired private UserProfilePreferenceService userProfilePreferenceService;

	@RequestMapping(value = "/{receiptId}", method = RequestMethod.GET)
	public ModelAndView loadForm(@PathVariable String receiptId, @ModelAttribute("receiptForm") ReceiptForm receiptForm) {
        DateTime time = DateUtil.now();
        log.info("Loading Receipt Item with id: " + receiptId);

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ReceiptEntity receiptEntity = receiptService.findReceipt(receiptId, receiptUser.getRid());
        if(receiptEntity == null) {
            //TODO check all get methods that can result in display sensitive data of other users to someone else fishing
            //Possible condition of bookmark or trying to gain access to some unknown receipt
            log.warn("User={}, tried submitting an invalid receipt={}", receiptUser.getRid(), receiptId);
        } else {
            List<ItemEntity> items = receiptService.findItems(receiptEntity);
            List<ExpenseTagEntity> expenseTypes = userProfilePreferenceService.activeExpenseTypes(receiptUser.getRid());

            receiptForm.setReceipt(receiptEntity);
            receiptForm.setItems(items);
            receiptForm.setExpenseTags(expenseTypes);
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return new ModelAndView(NEXT_PAGE);
	}

    @SuppressWarnings("PMD.EmptyIfStmt")
	@RequestMapping(method = RequestMethod.POST, params="delete")
	public String delete(@ModelAttribute("receiptForm") ReceiptForm receiptForm) {
        DateTime time = DateUtil.now();
        log.info("Delete receipt " + receiptForm.getReceipt().getId());

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean task = false;
        try {
            task = receiptService.deleteReceipt(receiptForm.getReceipt().getId(), receiptUser.getRid());
            if(!task) {
                //TODO in case of failure to delete send message to USER
            }
        } catch(Exception exce) {
            log.error("Error occurred during receipt delete: Receipt={}, reason={}", receiptForm.getReceipt().getId(), exce.getLocalizedMessage());
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), task);
		return "redirect:/access/landing.htm";
	}

    @RequestMapping(method = RequestMethod.POST, params="re-check")
    public ModelAndView recheck(@ModelAttribute("receiptForm") ReceiptForm receiptForm) {
        DateTime time = DateUtil.now();
        log.info("Initiating re-check on receipt " + receiptForm.getReceipt().getId());

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            receiptService.reopen(receiptForm.getReceipt().getId(), receiptUser.getRid());
        } catch(Exception exce) {
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
            log.error(exce.getLocalizedMessage() + ", Receipt: " + receiptForm.getReceipt().getId());

            receiptForm.setErrorMessage(exce.getLocalizedMessage());
            return loadForm(receiptForm.getReceipt().getId(), receiptForm);
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return new ModelAndView("redirect:/access/landing.htm");
    }

    @RequestMapping(method = RequestMethod.POST, params="update-expense-type")
    public String expenseUpdate(@ModelAttribute("receiptForm") ReceiptForm receiptForm) {
        DateTime time = DateUtil.now();
        log.info("Initiating Expense Type update on receipt " + receiptForm.getReceipt().getId());

        for(ItemEntity item : receiptForm.getItems()) {
            ExpenseTagEntity expenseType = userProfilePreferenceService.getExpenseType(item.getExpenseTag().getId());
            item.setExpenseTag(expenseType);
            try {
                receiptService.updateItemWithExpenseType(item);
            } catch (Exception e) {
                log.error("Error updating ExpenseType={}, for ItemEntity={}, reason={}", item.getExpenseTag().getId(), item.getId(), e.getLocalizedMessage(), e);
                //TODO send error message back saying update unsuccessful.
            }
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return "redirect:/access/landing.htm";
    }

    /**
     * Delete receipt through REST URL
     *
     * @param receiptId receipt id to delete
     * @param profileId user id
     * @param authKey   auth key
     * @return Header
     */
    @RequestMapping(value = "/d/{receiptId}/user/{profileId}/auth/{authKey}.xml", method=RequestMethod.GET)
    public @ResponseBody
    Header deleteRest(@PathVariable String receiptId, @PathVariable String profileId, @PathVariable String authKey) {
        DateTime time = DateUtil.now();
        log.info("Delete receipt " + receiptId);

        UserProfileEntity userProfile = authenticate(profileId, authKey);
        Header header = Header.newInstance(authKey);
        if(userProfile != null) {
            try {
                boolean task = receiptService.deleteReceipt(receiptId, profileId);
                if(task) {
                    header.setStatus(Header.RESULT.SUCCESS);
                    header.setMessage("Deleted receipt successfully");
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), true);
                    return header;
                } else {
                    header.setStatus(Header.RESULT.FAILURE);
                    header.setMessage("Delete receipt un-successful");
                    PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
                    return header;
                }
            } catch (Exception exce) {
                header.setStatus(Header.RESULT.FAILURE);
                header.setMessage("Delete receipt un-successful");
                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
                return header;
            }
        } else {
            header = getHeaderForProfileOrAuthFailure();
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
            return header;
        }
    }

    /**
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/biz/{id}", method = RequestMethod.GET)
    public ModelAndView receiptByBizName(@PathVariable String id) throws IOException {
        DateTime time = DateUtil.now();
        log.info("Loading Receipts by Biz Name id: " + id);

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ReceiptLandingView> receiptLandingViews = new ArrayList<>();

        ModelAndView modelAndView = new ModelAndView(NEXT_PAGE_BY_BIZ);

        List<BizNameEntity> bizNames = bizNameManager.findAllBizWithMatchingName(id);
        for(BizNameEntity bizNameEntity : bizNames) {
            List<ReceiptEntity> receipts = receiptService.findReceipt(bizNameEntity, receiptUser.getRid());
            for(ReceiptEntity receiptEntity : receipts) {
                receiptLandingViews.add(ReceiptLandingView.newInstance(receiptEntity));
            }
        }

        modelAndView.addObject("receiptLandingViews", receiptLandingViews);

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return modelAndView;
    }
}
