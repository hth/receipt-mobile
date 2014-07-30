package com.receiptofi.web.controller.access;

import com.receiptofi.service.EvalFeedbackService;
import com.receiptofi.social.domain.site.ReceiptUser;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.web.util.PerformanceProfiling;
import com.receiptofi.web.form.EvalFeedbackForm;
import com.receiptofi.web.util.TextInputScrubber;
import com.receiptofi.web.validator.EvalFeedbackValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 7/19/13
 * Time: 8:19 AM
 */
@Controller
@RequestMapping(value = "/access/eval")
public final class EvalFeedbackController {
    private static final Logger log = LoggerFactory.getLogger(EvalFeedbackController.class);

    /* Refers to feedback.jsp and next one to feedbackConfirm.jsp */
    private static final String NEXT_PAGE_IS_CALLED_FEEDBACK            = "/eval/feedback";
    private static final String NEXT_PAGE_IS_CALLED_FEEDBACK_CONFIRM    = "/eval/feedbackConfirm";

    /* For confirming which page to show */
    private static final String SUCCESS_EVAL = "success_eval_feedback";

    @Autowired EvalFeedbackService evalFeedbackService;
    @Autowired EvalFeedbackValidator evalFeedbackValidator;

    @RequestMapping(method = RequestMethod.GET, value = "/feedback")
    public ModelAndView loadForm(@ModelAttribute("evalFeedbackForm") EvalFeedbackForm evalFeedbackForm) {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Feedback loadForm: " + receiptUser.getRid());
        ModelAndView modelAndView = new ModelAndView(NEXT_PAGE_IS_CALLED_FEEDBACK);
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/feedback")
    public ModelAndView postForm(@ModelAttribute("evalFeedbackForm") EvalFeedbackForm evalFeedbackForm,
                                 HttpServletRequest httpServletRequest, BindingResult result) {

        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        evalFeedbackValidator.validate(evalFeedbackForm, result);
        if (result.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView(NEXT_PAGE_IS_CALLED_FEEDBACK);
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in result check");
            return modelAndView;
        }

        evalFeedbackService.addFeedback(TextInputScrubber.scrub(evalFeedbackForm.getComment()), evalFeedbackForm.getRating(), evalFeedbackForm.getFileData(), receiptUser.getRid());
        log.info("Feedback saved successfully");

        httpServletRequest.getSession().setAttribute(SUCCESS_EVAL, true);
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return new ModelAndView("redirect:/access" + NEXT_PAGE_IS_CALLED_FEEDBACK_CONFIRM + ".htm");
    }

    /**
     * Add this gymnastic to make sure the page does not process when refreshed again or bookmarked.
     *
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/feedbackConfirm")
    public String recoverConfirm(HttpServletRequest httpServletRequest) throws IOException {
        Enumeration<String> attributes = httpServletRequest.getSession().getAttributeNames();
        while(attributes.hasMoreElements()) {
            String attributeName = attributes.nextElement();
            if(attributeName.equals(SUCCESS_EVAL)) {
                boolean condition = (boolean) httpServletRequest.getSession().getAttribute(SUCCESS_EVAL);
                if(condition) {
                    httpServletRequest.getSession().setAttribute(SUCCESS_EVAL, false);
                    return NEXT_PAGE_IS_CALLED_FEEDBACK_CONFIRM;
                }
            }
        }
        return "redirect:/access" + NEXT_PAGE_IS_CALLED_FEEDBACK + ".htm";
    }
}
