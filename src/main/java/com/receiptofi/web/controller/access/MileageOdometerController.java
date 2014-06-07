package com.receiptofi.web.controller.access;

import com.receiptofi.domain.MileageEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.service.MileageService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.form.MileageForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * User: hitender
 * Date: 1/13/14 8:25 AM
 */
@Controller
@RequestMapping(value = "/access/modv")
public final class MileageOdometerController {
    private static final Logger log = LoggerFactory.getLogger(LandingController.class);

    @Autowired private MileageService mileageService;

    @Value("${MODEL_VIEW:/mileage}")
    private String NEXT_PAGE;

    @RequestMapping(value = "/{mileageId}", method = RequestMethod.GET)
    public ModelAndView loadForm(
            @PathVariable
            String mileageId,

            @ModelAttribute("mileageForm")
            MileageForm mileageForm,

            Model model
    ) {
        DateTime time = DateUtil.now();
        log.info("Loading MileageEntity with id: " + mileageId);

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //Gymnastic to show BindingResult errors if any
        if (model.asMap().containsKey("result")) {
            model.addAttribute("org.springframework.validation.BindingResult.mileageForm", model.asMap().get("result"));

            MileageEntity mileageEntity = mileageService.getMileage(mileageId, receiptUser.getRid());

            mileageForm = (MileageForm) model.asMap().get("mileageForm");
            mileageForm.setMileage(mileageEntity);
        } else {
            MileageEntity mileageEntity = mileageService.getMileage(mileageId, receiptUser.getRid());
            if(mileageEntity != null) {
                mileageForm.setMileage(mileageEntity);
            } else {
                //TODO check all get methods that can result in display sensitive data of other users to someone else fishing
                //Possible condition of bookmark or trying to gain access to some unknown receipt
                log.warn("User " + receiptUser.getRid() + ", tried submitting an invalid mileage id: " + mileageId);
            }
        }

        ModelAndView modelAndView = new ModelAndView(NEXT_PAGE);

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST, params="delete")
    public ModelAndView delete(
            @ModelAttribute("mileageForm")
            MileageForm mileageForm,

            BindingResult result,
            RedirectAttributes redirectAttrs
    ) {
        DateTime time = DateUtil.now();
        log.info("Delete mileage " + mileageForm.getMileage().getId());

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            if(!mileageService.deleteHardMileage(mileageForm.getMileage().getId(), receiptUser.getRid())) {
                redirectAttrs.addFlashAttribute("result", result);

                mileageForm.setErrorMessage("Delete request failed to execute");
                redirectAttrs.addFlashAttribute("mileageForm", mileageForm);

                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in deleting mileage");
                return new ModelAndView("redirect:/access/modv/" + mileageForm.getMileage().getId() + ".htm");
            }
        } catch(Exception exce) {
            log.error("Error occurred during receipt delete: Receipt Id: " + mileageForm.getMileage().getId() + ", error message: " + exce.getLocalizedMessage());
            result.rejectValue("errorMessage", "", "Delete request failed to execute");
            redirectAttrs.addFlashAttribute("result", result);

            //set the error message to display to user
            mileageForm.setErrorMessage("Delete request failed to execute");
            redirectAttrs.addFlashAttribute("mileageForm", mileageForm);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in deleting mileage");
            return new ModelAndView("redirect:/access/modv/" + mileageForm.getMileage().getId() + ".htm");
        }

        ModelAndView modelAndView = new ModelAndView("redirect:/access/landing.htm");
        modelAndView.addObject("showTab", "1");
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "deleted mileage successfully");
        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST, params="split")
    public ModelAndView split(@ModelAttribute("mileageForm") MileageForm mileageForm,
                              BindingResult result, RedirectAttributes redirectAttrs) {

        DateTime time = DateUtil.now();
        log.info("Split mileage " + mileageForm.getMileage().getId());

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            mileageService.split(mileageForm.getMileage().getId(), receiptUser.getRid());
        } catch(Exception exce) {
            log.error("Error occurred during splitting mileage: Mileage Id: " + mileageForm.getMileage().getId() + ", error message: " + exce.getLocalizedMessage());
            result.rejectValue("errorMessage", "", exce.getLocalizedMessage());
            redirectAttrs.addFlashAttribute("result", result);

            //set the error message to display to user
            mileageForm.setErrorMessage(exce.getLocalizedMessage());
            redirectAttrs.addFlashAttribute("mileageForm", mileageForm);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in receipt save");
            return new ModelAndView("redirect:/access/modv/" + mileageForm.getMileage().getId() + ".htm");
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
        ModelAndView modelAndView = new ModelAndView("redirect:/access/landing.htm");
        modelAndView.addObject("showTab", "1");
        return modelAndView;
    }
}