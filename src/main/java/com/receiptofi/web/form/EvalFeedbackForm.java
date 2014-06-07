package com.receiptofi.web.form;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * User: hitender
 * Date: 7/19/13
 * Time: 8:27 AM
 */
public final class EvalFeedbackForm {

    private String comment;

    /* Rating can be set to zero in case user would not like to set any value for feedback */
    private int rating = 0;

    private CommonsMultipartFile fileData;

    @SuppressWarnings("unused")
    public String getComment() {
        return comment;
    }

    @SuppressWarnings("unused")
    public void setComment(String comment) {
        this.comment = comment;
    }

    @SuppressWarnings("unused")
    public int getRating() {
        return rating;
    }

    @SuppressWarnings("unused")
    public void setRating(int rating) {
        this.rating = rating;
    }

    @SuppressWarnings("unused")
    public CommonsMultipartFile getFileData() {
        return fileData;
    }

    @SuppressWarnings("unused")
    public void setFileData(CommonsMultipartFile fileData) {
        this.fileData = fileData;
    }

    public String getFileName() {
        return this.fileData.getFileItem().getName();
    }
}

