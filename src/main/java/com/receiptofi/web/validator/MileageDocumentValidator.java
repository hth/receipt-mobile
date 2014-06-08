package com.receiptofi.web.validator;

import com.receiptofi.web.form.ReceiptDocumentForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * User: hitender
 * Date: 1/25/14 10:59 PM
 */
@Component
public final class MileageDocumentValidator implements Validator {
    private static final Logger log = LoggerFactory.getLogger(MileageDocumentValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return ReceiptDocumentForm.class.equals(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        ReceiptDocumentForm receiptDocumentForm = (ReceiptDocumentForm) obj;
        log.debug("Executing validation for new receiptDocument: " + receiptDocumentForm.getReceiptDocument().getId());

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mileage.start", "field.required", new Object[]{"Start"});

        if (receiptDocumentForm.getMileageEntity().getStart() == 0 && !errors.hasErrors()) {
            errors.rejectValue("mileage.start",
                    "mileage.start",
                    new Object[] { Integer.valueOf("0") },
                    "Number should be greater than zero");
        }
    }

}
