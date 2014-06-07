package com.receiptofi.domain;

import com.receiptofi.domain.types.NotificationTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * User: hitender
 * Date: 6/30/13
 * Time: 1:29 PM
 */
@Document(collection = "NOTIFICATION")
public final class NotificationEntity extends BaseEntity {
    private static final Logger log = LoggerFactory.getLogger(NotificationEntity.class);
    private static final int OFF_SET = 0;
    private static final int MAX_WIDTH = 45;

    @NotNull
    @Field("MESSAGE")
    private String message;

    @NotNull
    @Field("USER_PROFILE_ID")
    private String userProfileId;

    @NotNull
    @Field("NOTIFIED")
    private boolean notified = false;

    @NotNull
    @Field("NOTIFICATION_ENUM")
    private NotificationTypeEnum notificationType;

    /**
     * Could be a receipt id or Document id
     */
    private String referenceId;

    private NotificationEntity() {}

    public static NotificationEntity newInstance(NotificationTypeEnum notificationType) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setNotificationType(notificationType);
        return notificationEntity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserProfileId() {
        return userProfileId;
    }

    public void setUserProfileId(String userProfileId) {
        this.userProfileId = userProfileId;
    }

    public boolean isNotified() {
        return notified;
    }

    public void markAsNotified() {
        setNotified(true);
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public NotificationTypeEnum getNotificationType() {
        return notificationType;
    }

    private void setNotificationType(NotificationTypeEnum notificationType) {
        this.notificationType = notificationType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    /**
     * Move this to form if exists
     *
     * @return
     */
    public String getNotificationMessage4Display() {
        switch(notificationType) {
            case MESSAGE:
                return getMessage();
            case DOCUMENT:
                return getReceiptUpdateURL(getReferenceId(), getMessage4Display());
            case RECEIPT:
                return getReceiptURL(getReferenceId(), getMessage4Display());
            case EXPENSE_REPORT:
                return getReceiptURL(getReferenceId(), getMessage4Display());
            case MILEAGE:
                return getMileageURL(getReferenceId(), getMessage4Display());
            default:
                log.error("Reached invalid condition in Notification");
                throw new UnsupportedOperationException("Reached invalid condition in Notification");
        }
    }

    public String getNotificationMessage() {
        switch(notificationType) {
            case MESSAGE:
                return getMessage();
            case DOCUMENT:
                return getReceiptUpdateURL(getReferenceId(), getMessage());
            case RECEIPT:
                return getReceiptURL(getReferenceId(), getMessage());
            case EXPENSE_REPORT:
                return getReceiptURL(getReferenceId(), getMessage());
            case MILEAGE:
                return getMileageURL(getReferenceId(), getMessage());
            default:
                log.error("Reached invalid condition in Notification");
                throw new UnsupportedOperationException("Reached invalid condition in Notification");
        }
    }

    @Transient
    private String getReceiptUpdateURL(String referenceId, String message) {
        return "<a href=\"" + "./emp/update/" + referenceId + ".htm" + "\">" + message + "</a>";
    }

    @Transient
    private String getReceiptURL(String referenceId, String message) {
        return "<a href=\"" + "./receipt/" + referenceId + ".htm" + "\">" + message + "</a>";
    }

    @Transient
    private String getMileageURL(String referenceId, String message) {
        return "<a href=\"" + "./modv/" + referenceId + ".htm" + "\">" + message + "</a>";
    }

    /**
     * Used in displaying the message in short form on landing page
     *
     * @return
     */
    @Transient
    private String getMessage4Display() {
        return StringUtils.abbreviate(message, OFF_SET, MAX_WIDTH);
    }
}
