/**
 *
 */
package com.receiptofi.web.controller.access;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserPreferenceEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.social.domain.site.ReceiptUser;
import com.receiptofi.service.AccountService;
import com.receiptofi.service.ItemService;
import com.receiptofi.service.UserProfilePreferenceService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.web.util.PerformanceProfiling;
import com.receiptofi.web.form.ExpenseTypeForm;
import com.receiptofi.web.form.UserProfilePreferenceForm;
import com.receiptofi.web.validator.ExpenseTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.joda.time.DateTime;

/**
 * Note: Follow PRG model with support for result binding
 *
 * @author hitender
 * @since Jan 14, 2013 11:06:41 PM
 *
 */
@Controller
@RequestMapping(value = "/access/userprofilepreference")
public final class UserProfilePreferenceController {
	private static final Logger log = LoggerFactory.getLogger(UserProfilePreferenceController.class);

	private static final String nextPage = "/userprofilepreference";

    @Autowired private UserProfilePreferenceService userProfilePreferenceService;
    @Autowired private AccountService accountService;
    @Autowired private ItemService itemService;
    @Autowired private ExpenseTypeValidator expenseTypeValidator;

    @PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = "/i", method = RequestMethod.GET)
	public ModelAndView loadForm(
            @ModelAttribute("expenseTypeForm")
            ExpenseTypeForm expenseTypeForm,

            @ModelAttribute("userProfilePreferenceForm")
            UserProfilePreferenceForm userProfilePreferenceForm,

            Model model
    ) throws IOException {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        populateUserProfilePreferenceForm(receiptUser.getRid(), userProfilePreferenceForm);
        ModelAndView modelAndView = populateModel(nextPage, null, userProfilePreferenceForm);

        //Gymnastic to show BindingResult errors if any
        if (model.asMap().containsKey("result")) {
            model.addAttribute("org.springframework.validation.BindingResult.expenseTypeForm", model.asMap().get("result"));
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
		return modelAndView;
	}

    /**
     * Used for adding Expense Type
     *
     * Note: Gymnastic : The form that is being posted should be the last in order. Or else validation fails to work
     * @param userProfilePreferenceForm
     * @param expenseTypeForm
     * @param result
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value="/i", method = RequestMethod.POST)
    public String addExpenseTag(
            @ModelAttribute("userProfilePreferenceForm")
            UserProfilePreferenceForm userProfilePreferenceForm,

            @ModelAttribute("expenseTypeForm")
            ExpenseTypeForm expenseTypeForm,

            BindingResult result,
            RedirectAttributes redirectAttrs) {

        DateTime time = DateUtil.now();
        //There is UI logic based on this. Set the right to be active when responding.
        redirectAttrs.addFlashAttribute("showTab", "#tabs-2");

        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        populateUserProfilePreferenceForm(receiptUser.getRid(), userProfilePreferenceForm);

        expenseTypeValidator.validate(expenseTypeForm, result);
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("result", result);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error in result");

            //Re-direct to prevent resubmit
            return "redirect:/access" + nextPage + "/i" + ".htm";
        }

        try {
            ExpenseTagEntity expenseType = ExpenseTagEntity.newInstance(expenseTypeForm.getTagName(), receiptUser.getRid());
            userProfilePreferenceService.addExpenseType(expenseType);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            result.rejectValue("expName", StringUtils.EMPTY, e.getLocalizedMessage());
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());

        //Re-direct to prevent resubmit
        return "redirect:/access" + nextPage + "/i" + ".htm";
    }

    /**
     * To Show and Hide the expense type
     * //TODO convert to ajax call instead
     *
     * @param expenseTagId
     * @param changeStatTo
     * @return
     */
    @RequestMapping(value="/expenseTagVisible", method = RequestMethod.GET)
    public ModelAndView changeExpenseTypeVisibleStatus(
            @RequestParam(value="id") String expenseTagId,
            @RequestParam(value="status") String changeStatTo,
            @ModelAttribute("expenseTypeForm") ExpenseTypeForm expenseTypeForm,
            @ModelAttribute("userProfilePreferenceForm") UserProfilePreferenceForm userProfilePreferenceForm
    ) {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //Secondary check. In case some one tries to be smart by passing parameters in URL :)
        if(itemService.countItemsUsingExpenseType(expenseTagId, receiptUser.getRid()) == 0) {
            userProfilePreferenceService.modifyVisibilityOfExpenseType(expenseTagId, changeStatTo, receiptUser.getRid());
        }

        populateUserProfilePreferenceForm(receiptUser.getRid(), userProfilePreferenceForm);
        ModelAndView modelAndView = populateModel(nextPage, expenseTypeForm, userProfilePreferenceForm);

        //There is UI logic based on this. Set the right to be active when responding.
        modelAndView.addObject("showTab", "#tabs-2");

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return modelAndView;
    }

    /**
     * Only admin has access to this link. Others get 403 error.
     *
     * @param rid
     * @param expenseTypeForm
     * @return
     * @throws IOException
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/their", method = RequestMethod.GET)
	public ModelAndView getUser(
            @RequestParam("id")
            String rid,

            @ModelAttribute("expenseTypeForm")
            ExpenseTypeForm expenseTypeForm,

            @ModelAttribute("userProfilePreferenceForm")
            UserProfilePreferenceForm userProfilePreferenceForm
    ) throws IOException {
        DateTime time = DateUtil.now();
        populateUserProfilePreferenceForm(rid, userProfilePreferenceForm);
        ModelAndView modelAndView = populateModel(nextPage, expenseTypeForm, userProfilePreferenceForm);
        modelAndView.addObject("id", rid);
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return modelAndView;

	}

    private void populateUserProfilePreferenceForm(String rid, UserProfilePreferenceForm userProfilePreferenceForm) {
        UserProfileEntity userProfile = userProfilePreferenceService.forProfilePreferenceFindByReceiptUserId(rid);
        userProfilePreferenceForm.setUserProfile(userProfile);
    }

    /**
     * Only Admin can update the user level. Others get 403 error. If the user cannot access /their, then its highly
     * unlikely to perform the action below.
     *
     * @param expenseTypeForm
     * @return
     * @throws IOException
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value="/update", method = RequestMethod.POST)
	public String updateUser(
            @ModelAttribute("expenseTypeForm")
            ExpenseTypeForm expenseTypeForm,

            @ModelAttribute("userProfilePreferenceForm")
            UserProfilePreferenceForm userProfilePreferenceForm
    ) throws IOException {
        DateTime time = DateUtil.now();

        UserProfileEntity userProfile = userProfilePreferenceService.forProfilePreferenceFindByReceiptUserId(
                userProfilePreferenceForm.getUserProfile().getReceiptUserId()
        );
        userProfile.setLevel(userProfilePreferenceForm.getUserProfile().getLevel());
        if(!userProfilePreferenceForm.isActive() || !userProfile.isActive()) {
            if(userProfilePreferenceForm.isActive()) {
                userProfile.active();
            } else {
                userProfile.inActive();
            }
        }

        UserAccountEntity userAccount = accountService.changeAccountRolesToMatchUserLevel(
                userProfile.getReceiptUserId(),
                userProfile.getLevel()
        );

        try {
            accountService.saveUserAccount(userAccount);
            userProfilePreferenceService.updateProfile(userProfile);
        } catch (Exception exce) {
            //XXX todo should there be two phase commit
            log.error("Failed updating User Profile, rid={}", userProfile.getReceiptUserId(), exce);
            userProfilePreferenceForm.setErrorMessage("Failed updating user profile " + exce.getLocalizedMessage());
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return "redirect:/access" + nextPage + "/their" + ".htm?id=" + userProfile.getReceiptUserId();
	}

	/**
	 * @param nextPage
     * @param userProfilePreference
	 * @return
	 */
	private ModelAndView populateModel(String nextPage, ExpenseTypeForm expenseTypeForm, UserProfilePreferenceForm userProfilePreference) {
        DateTime time = DateUtil.now();

        UserPreferenceEntity userPreference = userProfilePreferenceService.loadFromProfile(userProfilePreference.getUserProfile());
        userProfilePreference.setUserAuthentication(
                accountService.findByReceiptUserId(
                        userProfilePreference.getUserProfile().getReceiptUserId()
                ).getUserAuthentication()
        );

		ModelAndView modelAndView = new ModelAndView(nextPage);
        userProfilePreference.setUserPreference(userPreference);
        if(expenseTypeForm != null) {
            modelAndView.addObject("expenseTypeForm", expenseTypeForm);
        }

        List<ExpenseTagEntity> expenseTypes = userProfilePreferenceService.allExpenseTypes(userProfilePreference.getUserProfile().getReceiptUserId());
        userProfilePreference.setExpenseTags(expenseTypes);

        Map<String, Long> expenseTypeCount = new HashMap<>();
        int count = 0;
        for(ExpenseTagEntity expenseType : expenseTypes) {
            if(expenseType.isActive()) {
                count++;
            }

            expenseTypeCount.put(
                    expenseType.getTagName(),
                    itemService.countItemsUsingExpenseType(expenseType.getId(), userProfilePreference.getUserProfile().getId())
            );
        }

        userProfilePreference.setExpenseTagCount(expenseTypeCount);
        userProfilePreference.setVisibleExpenseTags(count);

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
		return modelAndView;
	}
}
