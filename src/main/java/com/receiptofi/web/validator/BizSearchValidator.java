package com.receiptofi.web.validator;

import com.receiptofi.web.form.BizForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * User: hitender
 * Date: 8/8/13
 * Time: 11:07 AM
 */
public final class BizSearchValidator implements Validator {
    private static final Logger log = LoggerFactory.getLogger(BizSearchValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return BizForm.class.equals(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        log.debug("Executing validation");

        BizForm bizForm = (BizForm) obj;
        if(StringUtils.isEmpty(bizForm.getBusinessName()) &&
                StringUtils.isEmpty(bizForm.getAddress()) &&
                StringUtils.isEmpty(bizForm.getPhone())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "businessName",   "field.required", new Object[] { "Biz Name" });
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address",        "field.required", new Object[] { "Address" });
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone",          "field.required", new Object[] { "Phone" });
        }
    }
}
