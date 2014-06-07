package com.receiptofi.web.form;

import com.receiptofi.domain.MileageEntity;

/**
 * User: hitender
 * Date: 1/13/14 8:35 AM
 */
public final class MileageForm {

    private MileageEntity mileage;

    /** Used for showing error messages to user when the request action fails to execute */
    String errorMessage;

    public MileageEntity getMileage() {
        return mileage;
    }

    public void setMileage(MileageEntity mileage) {
        this.mileage = mileage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
