package com.receiptofi.web.validator;

import com.receiptofi.web.form.EvalFeedbackForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * User: hitender
 * Date: 7/20/13
 * Time: 8:29 PM
 */
public final class EvalFeedbackValidator implements  Validator {
    private static final Logger log = LoggerFactory.getLogger(EvalFeedbackValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return EvalFeedbackForm.class.equals(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        log.debug("Executing validation");

        EvalFeedbackForm evalFeedbackForm = (EvalFeedbackForm) obj;
        if(evalFeedbackForm.getComment().length() < 15) {
            errors.rejectValue("comment",
                    "field.length",
                    new Object[] { Integer.valueOf("15") },
                    "Minimum length of 15 characters");
        }

        if(evalFeedbackForm.getFileData().getSize() != 0) {
            if(evalFeedbackForm.getFileData().getSize() > 10485760) {
                errors.rejectValue("fileData",
                        "file.length.high",
                        new Object[] { "" },
                        "Uploaded file size exceeds the file size limitation of 10MB");
            }

            if (evalFeedbackForm.getFileName().length() < 5) {
                errors.rejectValue("fileData",
                        "field.length",
                        new Object[] { Integer.valueOf("5") },
                        "A file name should be minimum of five characters");
            }

            //Can upload SVG as image/svg+xml
            if(!evalFeedbackForm.getFileData().getContentType().startsWith("image/") && !StringUtils.isEmpty(evalFeedbackForm.getFileName())) {
                errors.rejectValue("fileData",
                        "file.data",
                        new Object[] { evalFeedbackForm.getFileName() },
                        ", is not supported. Supported format .JPEG, .JPG, .PNG");
            }
        }
    }
}