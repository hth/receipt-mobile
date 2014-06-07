package com.receiptofi.web.controller.admin;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.service.BizService;
import com.receiptofi.service.ExternalService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.form.BizForm;
import com.receiptofi.web.validator.BizSearchValidator;
import com.receiptofi.web.validator.BizValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
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
 * User: hitender
 * Date: 7/30/13
 * Time: 4:22 PM
 */
@Controller
@RequestMapping(value = "/admin")
public final class BusinessController {
    private static final Logger log = LoggerFactory.getLogger(BusinessController.class);
    private static final String NEXT_PAGE = "/admin/business";
    private static final String EDIT_PAGE = "/admin/businessEdit";

    @Autowired private ExternalService externalService;
    @Autowired private BizService bizService;

    @Autowired private BizValidator bizValidator;
    @Autowired private BizSearchValidator bizSearchValidator;

    @RequestMapping(value = "/business", method = RequestMethod.GET)
    public ModelAndView loadSearchForm(@ModelAttribute("bizForm") BizForm bizForm, Model model) {

        //Gymnastic to show BindingResult errors if any
        if (model.asMap().containsKey("result")) {
            model.addAttribute("org.springframework.validation.BindingResult.bizForm", model.asMap().get("result"));
        }

        ModelAndView modelAndView = new ModelAndView(NEXT_PAGE);
        return modelAndView;
    }

    @RequestMapping(value = "/business/edit", method = RequestMethod.GET)
    public ModelAndView editStore(
            @RequestParam("nameId") String nameId,
            @RequestParam("storeId") String storeId,
            @ModelAttribute("bizForm") BizForm bizForm
    ) {
        ModelAndView modelAndView = new ModelAndView(EDIT_PAGE);
        BizNameEntity bizNameEntity = bizService.findName(nameId);
        bizForm.setBizNameEntity(bizNameEntity);

        if(StringUtils.isNotEmpty(storeId)) {
            BizStoreEntity bizStoreEntity = bizService.findStore(storeId);
            bizForm.setBizStore(bizStoreEntity);
        }

        return modelAndView;
    }

    /**
     * Reset the form
     *
     * @param redirectAttrs
     * @return
     */
    @RequestMapping(value = "/business", method = RequestMethod.POST, params = "reset")
    public String reset(RedirectAttributes redirectAttrs) {
        DateTime time = DateUtil.now();
        redirectAttrs.addFlashAttribute("bizForm", BizForm.newInstance());
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), true);
        //Re-direct to prevent resubmit
        return "redirect:" + NEXT_PAGE + ".htm";
    }

    /**
     * Edit Biz Name and or Biz Address, Phone.
     *
     * No validation is performed.
     *
     * @param bizForm
     * @return
     */
    @RequestMapping(value = "/business", method = RequestMethod.POST, params = "edit")
    public String editBiz(@ModelAttribute("bizForm") BizForm bizForm, RedirectAttributes redirectAttrs) {

        DateTime time = DateUtil.now();
        redirectAttrs.addFlashAttribute("bizForm", bizForm);

        BizStoreEntity bizStoreEntity;
        if(StringUtils.isNotEmpty(bizForm.getAddressId())) {
            bizStoreEntity = bizService.findStore(bizForm.getAddressId());
            bizStoreEntity.setAddress(bizForm.getAddress());
            bizStoreEntity.setPhone(bizForm.getPhone());
            try {
                externalService.decodeAddress(bizStoreEntity);
                bizService.saveStore(bizStoreEntity);
            } catch (Exception e) {
                log.error("Failed to edit address/phone: " + bizForm.getAddress() + ", " + bizForm.getPhone() + ", :" + e.getLocalizedMessage());
                bizForm.setBizError("Failed to edit address/phone: " + bizForm.getAddress() + ", " + bizForm.getPhone() + ", :" + e.getLocalizedMessage());

                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
                //Re-direct to prevent resubmit
                return "redirect:" + "business/edit" + ".htm" + "?nameId=" + bizForm.getNameId() + "&storeId=" + bizForm.getAddressId();
            }
        }

        BizNameEntity bizNameEntity;
        if(StringUtils.isNotEmpty(bizForm.getNameId())) {
            bizNameEntity = bizService.findName(bizForm.getNameId());
            bizNameEntity.setBusinessName(bizForm.getBusinessName());
            try {
                bizService.saveName(bizNameEntity);
                log.info("Business '" + bizNameEntity.getBusinessName() + "' updated successfully");
            } catch(Exception e) {
                log.error("Failed to edit name: " + bizForm.getBusinessName() + ", " + e.getLocalizedMessage());
                bizForm.setBizError("Failed to edit name: " + bizForm.getBusinessName() + ", " + e.getLocalizedMessage());

                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
                //Re-direct to prevent resubmit
                return "redirect:" + "business/edit" + ".htm" + "?nameId=" + bizForm.getNameId() + "&storeId=" + bizForm.getAddressId();
            }
        }

        Set<BizStoreEntity> bizStoreEntities = searchBizStoreEntities(bizForm);
        bizForm.setLast10BizStore(bizStoreEntities);

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), true);
        //Re-direct to prevent resubmit
        return "redirect:" + NEXT_PAGE + ".htm";
    }

    /**
     * Delete Biz Name and or Biz Address, Phone. when there are no active or inactive receipts referring to any of the
     * stores.
     *
     * No validation is performed.
     *
     * @param bizForm
     * @return
     */
    @RequestMapping(value = "/business", method = RequestMethod.POST, params = "delete_store")
    public String deleteBizStore(@ModelAttribute("bizForm") BizForm bizForm, RedirectAttributes redirectAttrs) {

        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        redirectAttrs.addFlashAttribute("bizForm", bizForm);

        BizStoreEntity bizStoreEntity;
        if(StringUtils.isNotEmpty(bizForm.getAddressId())) {
            bizStoreEntity = bizService.findStore(bizForm.getAddressId());
            BizNameEntity bizNameEntity = bizStoreEntity.getBizName();

            Set<BizStoreEntity> bizStoreEntities = new HashSet<>();
            bizStoreEntities.add(bizStoreEntity);
            bizService.countReceiptForBizStore(bizStoreEntities, bizForm);
            if(bizForm.getReceiptCount().get(bizStoreEntity.getId()) == 0) {
                bizService.deleteBizStore(bizStoreEntity);
                bizForm.setBizSuccess("Deleted store successfully");
                log.info("Deleted stored: " + bizStoreEntity.getAddress() + ", id: " + bizStoreEntity.getId() + ", by user={}", receiptUser.getRid());

                //To make sure no orphan biz name are lingering around
                if(bizService.countReceiptForBizName(bizNameEntity) == 0) {
                    bizService.deleteBizName(bizNameEntity);
                    bizForm.setBizSuccess("Deleted biz name successfully");
                    log.info("Deleted biz name: " + bizNameEntity.getBusinessName() + ", id: " + bizNameEntity.getId() + ", by user={}", receiptUser.getRid());
                }
            } else {
                bizForm.setBizError("Could not delete the store as its currently being referred by a receipt");
            }
        }

        Set<BizStoreEntity> bizStoreEntities = searchBizStoreEntities(bizForm);
        bizForm.setLast10BizStore(bizStoreEntities);

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), true);
        //Re-direct to prevent resubmit
        return "redirect:" + NEXT_PAGE + ".htm";
    }

    /**
     *
     * @param bizForm
     * @param result
     * @return
     */
    @RequestMapping(value = "/business", method = RequestMethod.POST, params = "add")
    public String addBiz(@ModelAttribute("bizForm") BizForm bizForm, BindingResult result, RedirectAttributes redirectAttrs) {

        DateTime time = DateUtil.now();
        redirectAttrs.addFlashAttribute("bizForm", bizForm);

        bizValidator.validate(bizForm, result);
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("result", result);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
            //Re-direct to prevent resubmit
            return "redirect:" + NEXT_PAGE + ".htm";
        } else {
            BizStoreEntity bizStoreEntity = BizStoreEntity.newInstance();
            bizStoreEntity.setAddress(bizForm.getAddress());
            bizStoreEntity.setPhone(bizForm.getPhone());
            try {
                externalService.decodeAddress(bizStoreEntity);
            } catch (Exception e) {
                log.error("Failed to edit address/phone: " + bizForm.getAddress() + ", " + bizForm.getPhone() + ", :" + e.getLocalizedMessage());
                bizForm.setBizError("Failed to edit address/phone: " + bizForm.getAddress() + ", " + bizForm.getPhone() + ", :" + e.getLocalizedMessage());

                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
                //Re-direct to prevent resubmit
                return "redirect:" + NEXT_PAGE + ".htm";
            }

            ReceiptEntity receiptEntity = ReceiptEntity.newInstance();
            receiptEntity.setBizStore(bizStoreEntity);

            BizNameEntity bizNameEntity = BizNameEntity.newInstance();
            bizNameEntity.setBusinessName(bizForm.getBusinessName());
            receiptEntity.setBizName(bizNameEntity);
            try {
                bizService.saveNewBusinessAndOrStore(receiptEntity);
                bizForm.setBizSuccess("Business '" + receiptEntity.getBizName().getBusinessName() + "' added successfully");
            } catch(Exception e) {
                log.error("Failed to edit name: " + bizForm.getBusinessName() + ", " + e.getLocalizedMessage());
                bizForm.setBizError("Failed to edit name: " + bizForm.getBusinessName() + ", " + e.getLocalizedMessage());

                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
                //Re-direct to prevent resubmit
                return "redirect:" + NEXT_PAGE + ".htm";
            }

            if(receiptEntity.getBizName().getId().equals(receiptEntity.getBizStore().getBizName().getId())) {
                redirectAttrs.addFlashAttribute("bizStore", receiptEntity.getBizStore());
                bizForm.setLast10BizStore(bizService.getAllStoresForBusinessName(receiptEntity));
            } else {
                bizForm.setBizError("Address uniquely identified with another Biz Name: " + receiptEntity.getBizStore().getBizName().getBusinessName());
            }

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
            //Re-direct to prevent resubmit
            return "redirect:" + NEXT_PAGE + ".htm";
        }
    }

    /**
     * Search for Biz with either Name, Address, Phone or all or none
     *
     * @param bizForm
     * @param result
     * @return
     */
    @RequestMapping(value = "/business", method = RequestMethod.POST, params = "search")
    public String searchBiz(@ModelAttribute("bizForm") BizForm bizForm, BindingResult result, RedirectAttributes redirectAttrs) {

        DateTime time = DateUtil.now();
        redirectAttrs.addFlashAttribute("bizForm", bizForm);

        bizSearchValidator.validate(bizForm, result);
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("result", result);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
            //Re-direct to prevent resubmit
            return "redirect:" + NEXT_PAGE + ".htm";
        } else {

            Set<BizStoreEntity> bizStoreEntities = searchBizStoreEntities(bizForm);
            bizForm.setLast10BizStore(bizStoreEntities);

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), true);
            //Re-direct to prevent resubmit
            return "redirect:" + NEXT_PAGE + ".htm";
        }
    }

    /**
     * Search for matching biz criteria
     *
     * @param bizForm
     * @return
     */
    private Set<BizStoreEntity> searchBizStoreEntities(BizForm bizForm) {
        String businessName = StringUtils.trim(bizForm.getBusinessName());
        String address = StringUtils.trim(bizForm.getAddress());
        String phone = StringUtils.trim(BizStoreEntity.phoneCleanup(bizForm.getPhone()));
        Set<BizStoreEntity> bizStoreEntities = bizService.bizSearch(businessName, address, phone);
        bizForm.setBizSuccess("Found '" + bizStoreEntities.size() + "' matching business(es).");

        bizService.countReceiptForBizStore(bizStoreEntities, bizForm);
        return bizStoreEntities;
    }
}
