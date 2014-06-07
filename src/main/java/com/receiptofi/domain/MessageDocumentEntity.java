package com.receiptofi.domain;

import com.receiptofi.domain.types.DocumentStatusEnum;
import com.receiptofi.domain.types.UserLevelEnum;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * User: hitender
 * Date: 4/6/13
 * Time: 6:48 PM
 */
@Document(collection = "MESSAGE_DOCUMENT")
public final class MessageDocumentEntity extends BaseEntity {

    //TODO change to document id
    @NotNull
    @Field("RECEIPT_OCR_ID")
    private String documentId;

    @NotNull
    @Field("USER_LEVEL_ENUM")
    private UserLevelEnum level = UserLevelEnum.USER;

    @Email
    @Field("EM")
    String emailId;

    @NotNull
    @Field("USER_PROFILE_ID")
    String userProfileId;

    @NotNull
    @Field("LOCKED")
    private boolean recordLocked = false;

    @NotNull
    @Field("DS_E")
    private DocumentStatusEnum documentStatus;

    private MessageDocumentEntity() {}

    private MessageDocumentEntity(String documentId, UserLevelEnum level, DocumentStatusEnum documentStatus) {
        this.documentId = documentId;
        this.level = level;
        this.documentStatus = documentStatus;
    }

    public static MessageDocumentEntity newInstance(String idReceiptOCR, UserLevelEnum level, DocumentStatusEnum receiptStatus) {
        return new MessageDocumentEntity(idReceiptOCR, level, receiptStatus);
    }

    public String getDocumentId() {
        return documentId;
    }

    public UserLevelEnum getLevel() {
        return level;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getUserProfileId() {
        return userProfileId;
    }

    public void setUserProfileId(String userProfileId) {
        this.userProfileId = userProfileId;
    }

    public boolean isRecordLocked() {
        return recordLocked;
    }

    public void setRecordLocked(boolean recordLocked) {
        this.recordLocked = recordLocked;
    }

    public DocumentStatusEnum getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DocumentStatusEnum documentStatus) {
        this.documentStatus = documentStatus;
    }

    @Override
    public String toString() {
        return "MessageDocumentEntity{" +
                "id='" + id + '\'' +
                ", documentId='" + documentId + '\'' +
                ", level=" + level +
                ", emailId='" + emailId + '\'' +
                ", userProfileId='" + userProfileId + '\'' +
                ", recordLocked=" + recordLocked +
                ", documentStatus=" + documentStatus +
                '}';
    }
}
