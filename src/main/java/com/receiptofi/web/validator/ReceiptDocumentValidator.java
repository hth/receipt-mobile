package com.receiptofi.web.validator;

import com.receiptofi.domain.ItemEntityOCR;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.Formatter;
import com.receiptofi.utils.Maths;
import com.receiptofi.web.form.ReceiptDocumentForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author hitender
 * @since Jan 10, 2013 10:00:24 PM
 */
@Component
public final class ReceiptDocumentValidator implements Validator {
    private static final Logger log = LoggerFactory.getLogger(ReceiptDocumentValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return ReceiptDocumentForm.class.equals(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        ReceiptDocumentForm receiptDocumentForm = (ReceiptDocumentForm) obj;
        log.debug("Executing validation for new receiptDocument: " + receiptDocumentForm.getReceiptDocument().getId());

        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors,
                "receiptDocument.bizName.businessName",
                "field.required",
                new Object[]{"Biz Name"}
        );
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors,
                "receiptDocument.receiptDate",
                "field.required",
                new Object[]{"Date"}
        );
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors,
                "receiptDocument.total",
                "field.required",
                new Object[]{"Total"}
        );
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors,
                "receiptDocument.subTotal",
                "field.required",
                new Object[]{"Sub Total"}
        );

        try {
            DateUtil.getDateFromString(receiptDocumentForm.getReceiptDocument().getReceiptDate());
        } catch (IllegalArgumentException exce) {
            errors.rejectValue("receiptDocument.receiptDate", "field.date", new Object[]{receiptDocumentForm.getReceiptDocument().getReceiptDate()}, "Unsupported date format");
        }

        int count = 0;
        BigDecimal subTotal = BigDecimal.ZERO;
        if (receiptDocumentForm.getItems() != null) {
            boolean conditionFailed = false;
            int conditionFailedCounter = 0;
            for (ItemEntityOCR item : receiptDocumentForm.getItems()) {
                if (StringUtils.isNotEmpty(item.getName()) && StringUtils.isNotEmpty(item.getPrice()) && item.getQuantity() != null) {
                    try {
                        subTotal = Maths.add(subTotal, Maths.multiply(Formatter.getCurrencyFormatted(item.getPrice()), item.getQuantity()));
                    } catch (ParseException | NumberFormatException exception) {
                        log.error("Exception during update of receipt: " + receiptDocumentForm.getReceiptDocument().getId() + ", with error message: " + exception.getLocalizedMessage());
                        errors.rejectValue("items[" + count + "].price", "field.currency", new Object[]{item.getPrice()}, "Unsupported currency format");
                    }
                } else {
                    /** Count need to check the condition below */
                    conditionFailed = true;
                    conditionFailedCounter ++;
                }
                count++;
            }

            /** This condition is added to make sure no receipt is added without at least one valid item in the list */
            if(conditionFailed && receiptDocumentForm.getItems().size() == conditionFailedCounter) {
                log.error("Exception during update of receipt: " + receiptDocumentForm.getReceiptDocument().getId() + ", as no items were found");
                errors.rejectValue("receiptDocument", "item.required", new Object[]{"Item(s)"}, "Items required to submit a receipt");
            }
        } else {
            log.error("Exception during update of receipt: " + receiptDocumentForm.getReceiptDocument().getId() + ", as no items were found");
            errors.rejectValue("receiptDocumentForm", "item.required", new Object[]{"Item(s)"}, "Items required to submit a receipt");
        }

        BigDecimal submittedSubTotal = null;
        if (StringUtils.isNotEmpty(receiptDocumentForm.getReceiptDocument().getSubTotal())) {
            try {
                submittedSubTotal = Formatter.getCurrencyFormatted(receiptDocumentForm.getReceiptDocument().getSubTotal());
                subTotal = Maths.adjustScale(subTotal);
                int comparedValue = submittedSubTotal.compareTo(subTotal);
                if (comparedValue > 0) {
                    if(!Maths.withInRange(submittedSubTotal, subTotal)) {
                        errors.rejectValue("receiptDocument.subTotal", "field.currency.match.first",
                                new Object[]{receiptDocumentForm.getReceiptDocument().getSubTotal(), subTotal.toString()},
                                "Summation not adding up");
                    } else {
                        log.warn("Found difference in Calculated subTotal: " + subTotal +
                                ", submittedSubTotal: " + submittedSubTotal +
                                ". Which is less than application specified diff of " +
                                Maths.ACCEPTED_RANGE_IN_LOWEST_DENOMINATION);
                    }

                } else if (comparedValue < 0) {
                    if(!Maths.withInRange(submittedSubTotal, subTotal)) {
                        errors.rejectValue("receiptDocument.subTotal", "field.currency.match.second",
                            new Object[]{receiptDocumentForm.getReceiptDocument().getSubTotal(), subTotal.toString()},
                            "Summation not adding up");
                    } else {
                        log.warn("Found difference in Calculated subTotal: " + subTotal +
                                ", submittedSubTotal: " + submittedSubTotal +
                                ". Which is less than application specified diff of " +
                                Maths.ACCEPTED_RANGE_IN_LOWEST_DENOMINATION);
                    }
                }
            } catch (ParseException | NumberFormatException e) {
                errors.rejectValue("receiptDocument.subTotal", "field.currency", new Object[]{receiptDocumentForm.getReceiptDocument().getSubTotal()}, "Unsupported currency format");
            }
        }

        /** Compute total = tax + subtotal with provided total */
        BigDecimal total = null;
        if (StringUtils.isNotEmpty(receiptDocumentForm.getReceiptDocument().getTotal())) {
            try {
                total = Formatter.getCurrencyFormatted(receiptDocumentForm.getReceiptDocument().getTotal());
            } catch (ParseException | NumberFormatException e) {
                errors.rejectValue("receiptDocument.total", "field.currency", new Object[]{receiptDocumentForm.getReceiptDocument().getTotal()}, "Unsupported currency format");
            }

            try {
                if(submittedSubTotal != null && total != null) {
                    BigDecimal tax = Formatter.getCurrencyFormatted(receiptDocumentForm.getReceiptDocument().getTax());
                    //Since this is going to be displayed to user setting the scale to two.
                    BigDecimal calculatedTotal = Maths.add(submittedSubTotal, tax).setScale(Maths.SCALE_TWO);
                    if(calculatedTotal.compareTo(total) != 0) {
                        errors.rejectValue("receiptDocument.total", "field.receipt.total",
                                new Object[]{receiptDocumentForm.getReceiptDocument().getTotal(), calculatedTotal.toString()},
                                "Summation not adding up");
                    }
                } else {
                    errors.rejectValue("receiptDocument.total", "field.currency.cannot.compute", new Object[]{receiptDocumentForm.getReceiptDocument().getTotal()}, "Cannot compute because of previous error(s)");
                }
            } catch (ParseException | NumberFormatException exception) {
                log.error("Exception during update of receipt: " + receiptDocumentForm.getReceiptDocument().getId() + ", with error message: " + exception.getLocalizedMessage());
                errors.rejectValue("receiptDocument.tax", "field.currency", new Object[]{receiptDocumentForm.getReceiptDocument().getTax()}, "Unsupported currency format");
            }
        }
    }
}
