/**
 *
 */
package com.receiptofi.web.controller.emp;

import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ItemEntityOCR;
import com.receiptofi.domain.MileageEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.service.DocumentUpdateService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.form.ReceiptDocumentForm;
import com.receiptofi.web.validator.MileageDocumentValidator;
import com.receiptofi.web.validator.ReceiptDocumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.joda.time.DateTime;

/**
 * Class manages first processing of a receipt. That includes loading of a receipts by technician.
 * Updating of a receipt by technician. Same is true for recheck of receipt by technician.
 *
 * This same class is used for showing the pending receipt to user
 *
 * @author hitender
 * @since Jan 7, 2013 2:13:22 AM
 */
@Controller
@RequestMapping(value = "/emp")
public final class ReceiptUpdateController {
    private static final Logger log = LoggerFactory.getLogger(ReceiptUpdateController.class);

	private static final String NEXT_PAGE_UPDATE        = "/update";
    private static final String NEXT_PAGE_RECHECK       = "/recheck";
    public static final String REDIRECT_EMP_LANDING_HTM = "redirect:/emp/landing.htm";

    @Autowired private ReceiptDocumentValidator receiptDocumentValidator;
    @Autowired private DocumentUpdateService documentUpdateService;
    @Autowired private MileageDocumentValidator mileageDocumentValidator;

    @Value("${duplicate.receipt}")
    private String duplicateReceiptMessage;

    /**
     * For Technician: Loads new receipts.
     * For User :Method helps user to load either pending new receipt or pending recheck receipt.
     *
     * Added logic to make sure only the user of the receipt or technician can see the receipt.
     *
     * @param receiptOCRId
     * @param receiptDocumentForm
     * @return
     */
	@RequestMapping(value = "/update/{receiptOCRId}", method = RequestMethod.GET)
	public ModelAndView update(
            @PathVariable
            String receiptOCRId,

            @ModelAttribute("receiptDocumentForm")
            ReceiptDocumentForm receiptDocumentForm,

            Model model,
            HttpServletRequest httpServletRequest
    ) {
        updateReceipt(receiptOCRId, receiptDocumentForm, model, httpServletRequest);
        return new ModelAndView(NEXT_PAGE_UPDATE);
	}

    private void updateReceipt(String receiptOCRId, ReceiptDocumentForm receiptDocumentForm, Model model, HttpServletRequest httpServletRequest) {
        DateTime time = DateUtil.now();

        //Gymnastic to show BindingResult errors if any or any special receipt document containing error message
        if (model.asMap().containsKey("result")) {
            //result contains validation errors
            model.addAttribute("org.springframework.validation.BindingResult.receiptDocumentForm", model.asMap().get("result"));
            receiptDocumentForm = (ReceiptDocumentForm) model.asMap().get("receiptDocumentForm");
            loadBasedOnAppropriateUserLevel(receiptOCRId, receiptDocumentForm, httpServletRequest);
        } else if(model.asMap().containsKey("receiptDocumentForm")) {
            //errorMessage here contains any other logical error found
            receiptDocumentForm = (ReceiptDocumentForm) model.asMap().get("receiptDocumentForm");
            loadBasedOnAppropriateUserLevel(receiptOCRId, receiptDocumentForm, httpServletRequest);
        } else {
            loadBasedOnAppropriateUserLevel(receiptOCRId, receiptDocumentForm, httpServletRequest);
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    /**
     * For Technician: Loads recheck receipt
     *
     * @param receiptOCRId
     * @param receiptDocumentForm
     * @return
     */
    @RequestMapping(value = "/recheck/{receiptOCRId}", method = RequestMethod.GET)
    public ModelAndView recheck(
            @PathVariable
            String receiptOCRId,

            @ModelAttribute("receiptDocumentForm")
            ReceiptDocumentForm receiptDocumentForm,

            Model model,
            HttpServletRequest httpServletRequest
    ) {
        updateReceipt(receiptOCRId, receiptDocumentForm, model, httpServletRequest);
        return new ModelAndView(NEXT_PAGE_RECHECK);
    }

    /**
     * Process receipt after submitted by technician
     *
     * @param receiptDocumentForm
     * @param result
     * @return
     */
	@RequestMapping(value = "/submit", method = RequestMethod.POST, params= "receipt-submit")
	public ModelAndView submit(
            @ModelAttribute("receiptDocumentForm")
            ReceiptDocumentForm receiptDocumentForm,

            BindingResult result,
            RedirectAttributes redirectAttrs
    ) {
        DateTime time = DateUtil.now();
        log.info("Turk processing a receipt " + receiptDocumentForm.getReceiptDocument().getId() + " ; Title : " + receiptDocumentForm.getReceiptDocument().getBizName().getBusinessName());
		receiptDocumentValidator.validate(receiptDocumentForm, result);
		if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("result", result);
            redirectAttrs.addFlashAttribute("receiptDocumentForm", receiptDocumentForm);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in result");
            return new ModelAndView("redirect:/emp" + NEXT_PAGE_UPDATE + "/" + receiptDocumentForm.getReceiptDocument().getId() + ".htm");
		}

        try {
            if(documentUpdateService.hasReceiptWithSimilarChecksum(receiptDocumentForm.getReceiptEntity().getChecksum())) {
                log.info("Found pre-existing receipt with similar information for the selected date. Could be rejected and marked as duplicate.");

                receiptDocumentForm.setErrorMessage(duplicateReceiptMessage);
                redirectAttrs.addFlashAttribute("receiptDocumentForm", receiptDocumentForm);

                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in result");
                return new ModelAndView("redirect:/emp" + NEXT_PAGE_UPDATE + "/" + receiptDocumentForm.getReceiptDocument().getId() + ".htm");
            }

            //TODO add validate receipt entity as this can some times be invalid and add logic to recover a broken receipts by admin
            ReceiptEntity receipt = receiptDocumentForm.getReceiptEntity();
            List<ItemEntity> items = receiptDocumentForm.getItemEntity(receipt);
            receiptDocumentForm.updateItemWithTaxAmount(items, receipt);
            DocumentEntity documentForm = receiptDocumentForm.getReceiptDocument();

            documentUpdateService.turkReceipt(receipt, items, documentForm);
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "success");
            return new ModelAndView(REDIRECT_EMP_LANDING_HTM);
        } catch(Exception exce) {
            log.error("Error in Submit Process, reason={}", exce.getLocalizedMessage(), exce);

            receiptDocumentForm.setErrorMessage(exce.getLocalizedMessage());
            redirectAttrs.addFlashAttribute("receiptDocumentForm", receiptDocumentForm);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in receipt save");
            return new ModelAndView("redirect:/emp" + NEXT_PAGE_UPDATE + "/" + receiptDocumentForm.getReceiptDocument().getId() + ".htm");
        }
	}

    /**
     * Process receipt after submitted by technician
     *
     * @param receiptDocumentForm
     * @param result
     * @return
     */
    @RequestMapping(value = "/submitMileage", method = RequestMethod.POST, params= "mileage-submit")
    public ModelAndView submitMileage(
            @ModelAttribute("receiptDocumentForm")
            ReceiptDocumentForm receiptDocumentForm,

            BindingResult result,
            RedirectAttributes redirectAttrs
    ) {
        DateTime time = DateUtil.now();
        switch(receiptDocumentForm.getReceiptDocument().getDocumentOfType()) {
            case MILEAGE:
                log.info("Mileage : ");
                break;
        }

        mileageDocumentValidator.validate(receiptDocumentForm, result);
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("result", result);
            redirectAttrs.addFlashAttribute("receiptDocumentForm", receiptDocumentForm);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in result");
            return new ModelAndView("redirect:/emp" + NEXT_PAGE_UPDATE + "/" + receiptDocumentForm.getReceiptDocument().getId() + ".htm");
        }

        try {
            MileageEntity mileage = receiptDocumentForm.getMileageEntity();
            DocumentEntity receiptOCR = receiptDocumentForm.getReceiptDocument();
            documentUpdateService.turkMileage(mileage, receiptOCR);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "success");
            return new ModelAndView(REDIRECT_EMP_LANDING_HTM);
        } catch(Exception exce) {
            log.error("Error in Submit Process, reason={}", exce.getLocalizedMessage(), exce);

            receiptDocumentForm.setErrorMessage(exce.getLocalizedMessage());
            redirectAttrs.addFlashAttribute("receiptDocumentForm", receiptDocumentForm);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in receipt save");
            return new ModelAndView("redirect:/emp" + NEXT_PAGE_UPDATE + "/" + receiptDocumentForm.getReceiptDocument().getId() + ".htm");
        }
    }

    /**
     * Reject receipt since it can't be processed or its not a receipt
     *
     * @param receiptDocumentForm
     * @return
     */
    @RequestMapping(value = "/submit", method = RequestMethod.POST, params="receipt-reject")
    public ModelAndView reject(
            @ModelAttribute("receiptDocumentForm")
            ReceiptDocumentForm receiptDocumentForm,

            RedirectAttributes redirectAttrs
    ) {
        DateTime time = DateUtil.now();
        log.info("Beginning of Rejecting Document: " + receiptDocumentForm.getReceiptDocument().getId());
        try {
            DocumentEntity receiptOCR = receiptDocumentForm.getReceiptDocument();
            documentUpdateService.turkReject(receiptOCR);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "success");
            return new ModelAndView(REDIRECT_EMP_LANDING_HTM);
        } catch(Exception exce) {
            log.error("Error happened during rejecting receipt : " + receiptDocumentForm.getReceiptDocument().getId(), exce.getLocalizedMessage());

            String message = "Receipt could not be processed for Reject. Contact administrator with Document # ";
            receiptDocumentForm.setErrorMessage(message + receiptDocumentForm.getReceiptDocument().getId());
            redirectAttrs.addFlashAttribute("receiptDocumentForm", receiptDocumentForm);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in receipt reject");
            return new ModelAndView("redirect:/emp" + NEXT_PAGE_UPDATE + "/" + receiptDocumentForm.getReceiptDocument().getId() + ".htm");
        }
    }

    /**
     * Process receipt for after recheck by technician
     *
     * @param receiptDocumentForm
     * @param result
     * @return
     */
    @RequestMapping(value = "/recheck", method = RequestMethod.POST)
    public ModelAndView recheck(
            @ModelAttribute("receiptDocumentForm")
            ReceiptDocumentForm receiptDocumentForm,

            BindingResult result,
            RedirectAttributes redirectAttrs
    ) {
        DateTime time = DateUtil.now();
        log.info("Turk processing a receipt " + receiptDocumentForm.getReceiptDocument().getId() + " ; Title : " + receiptDocumentForm.getReceiptDocument().getBizName().getBusinessName());
        receiptDocumentValidator.validate(receiptDocumentForm, result);
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("result", result);
            redirectAttrs.addFlashAttribute("receiptDocumentForm", receiptDocumentForm);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in result");
            return new ModelAndView("redirect:/emp" + NEXT_PAGE_RECHECK + "/" + receiptDocumentForm.getReceiptDocument().getId() + ".htm");
        }

        try {
            //TODO: Note should not happen as the condition to check for duplicate has already been satisfied when receipt was first processed.
            // Unless Technician has changed the date or some data. Date change should be exclude during re-check. Something to think about.
            if(documentUpdateService.checkIfDuplicate(receiptDocumentForm.getReceiptEntity().getChecksum(), receiptDocumentForm.getReceiptEntity().getId())) {
                log.info("Found pre-existing receipt with similar information for the selected date. Could be rejected and marked as duplicate.");

                receiptDocumentForm.setErrorMessage(duplicateReceiptMessage);
                redirectAttrs.addFlashAttribute("receiptDocumentForm", receiptDocumentForm);

                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in result");
                return new ModelAndView("redirect:/emp" + NEXT_PAGE_RECHECK + "/" + receiptDocumentForm.getReceiptDocument().getId() + ".htm");
            }

            //TODO add validate receipt entity as this can some times be invalid and add logic to recover a broken receipts by admin
            ReceiptEntity receipt = receiptDocumentForm.getReceiptEntity();
            List<ItemEntity> items = receiptDocumentForm.getItemEntity(receipt);
            receiptDocumentForm.updateItemWithTaxAmount(items, receipt);
            DocumentEntity receiptOCR = receiptDocumentForm.getReceiptDocument();

            documentUpdateService.turkReceiptReCheck(receipt, items, receiptOCR);
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "success");
            return new ModelAndView(REDIRECT_EMP_LANDING_HTM);
        } catch(Exception exce) {
            log.error("Error in Recheck process: " + exce.getLocalizedMessage());

            receiptDocumentForm.setErrorMessage(exce.getLocalizedMessage());
            redirectAttrs.addFlashAttribute("receiptDocumentForm", receiptDocumentForm);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in receipt recheck save");
            return new ModelAndView("redirect:/emp" + NEXT_PAGE_RECHECK + "/" + receiptDocumentForm.getReceiptDocument().getId() + ".htm");
        }
    }

    private void loadBasedOnAppropriateUserLevel(String receiptOCRId, ReceiptDocumentForm receiptDocumentForm, HttpServletRequest request) {
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DocumentEntity receipt = documentUpdateService.loadActiveDocumentById(receiptOCRId);
        if(receipt == null || receipt.isDeleted()) {
            if(request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_TECHNICIAN") || request.isUserInRole("ROLE_SUPERVISOR")) {
                log.info("Receipt could not be found. Looks like user deleted the receipt before technician could process it.");
                receiptDocumentForm.setErrorMessage("Receipt could not be found. Looks like user deleted the receipt before technician could process it.");
            } else {
                log.warn("No such receipt exists. Request made by: " + receiptUser.getRid());
                receiptDocumentForm.setErrorMessage("No such receipt exists");
            }
        } else if(request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_TECHNICIAN") || request.isUserInRole("ROLE_SUPERVISOR") || receipt.getUserProfileId().equalsIgnoreCase(receiptUser.getRid())) {
            //Important: The condition below makes sure when validation fails it does not over write the item list
            if(receiptDocumentForm.getReceiptDocument() == null && receiptDocumentForm.getItems() == null) {
                receiptDocumentForm.setReceiptDocument(receipt);

                List<ItemEntityOCR> items = documentUpdateService.loadItemsOfReceipt(receipt);
                receiptDocumentForm.setItems(items);
            }
            //helps load the image on failure
            receiptDocumentForm.getReceiptDocument().setFileSystemEntities(receipt.getFileSystemEntities());
        } else {
            log.warn("Un-authorized access by user: " + receiptUser.getRid() + ", accessing receipt: " + receiptOCRId);
        }
    }
}
