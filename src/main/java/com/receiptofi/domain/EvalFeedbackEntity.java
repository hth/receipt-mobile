package com.receiptofi.domain;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * User: hitender
 * Date: 7/19/13
 * Time: 8:32 AM
 */
@Document(collection = "EVAL_FEEDBACK")
@CompoundIndexes(value = {
        @CompoundIndex(name = "eval_feedback_idx",    def = "{'USER_PROFILE_ID': 1, 'C': 1}",  unique=true),
} )
public final class EvalFeedbackEntity extends BaseEntity {

    @Field("FEEDBACK")
    private String feedback;

    @Field("ATTACHMENT_BLOB_ID")
    private String attachmentBlobId;

    @NotNull
    @Field("RATE")
    private int rating;

    @NotNull
    @Field("USER_PROFILE_ID")
    private String userProfileId;

    private EvalFeedbackEntity() {}

    public static EvalFeedbackEntity newInstance(String feedback, int rating, String userProfileId) {
        EvalFeedbackEntity evalFeedbackEntity = new EvalFeedbackEntity();
        evalFeedbackEntity.setFeedback(feedback);
        evalFeedbackEntity.setRating(rating);
        evalFeedbackEntity.setUserProfileId(userProfileId);
        return evalFeedbackEntity;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getAttachmentBlobId() {
        return attachmentBlobId;
    }

    public void setAttachmentBlobId(String attachmentBlobId) {
        this.attachmentBlobId = attachmentBlobId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUserProfileId() {
        return userProfileId;
    }

    public void setUserProfileId(String userProfileId) {
        this.userProfileId = userProfileId;
    }
}
