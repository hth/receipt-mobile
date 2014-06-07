package com.receiptofi.service;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.MileageEntity;
import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.types.NotificationTypeEnum;
import com.receiptofi.repository.NotificationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 6/30/13
 * Time: 2:07 PM
 */
@Service
public final class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired private NotificationManager notificationManager;

    /**
     * Hide notification from user
     *
     * @param message
     * @param notificationTypeEnum
     * @param id
     * @param userProfileId
     * @param notified
     */
    public void addNotification(String message, NotificationTypeEnum notificationTypeEnum, String id, String userProfileId, boolean notified) {
        NotificationEntity notificationEntity = NotificationEntity.newInstance(notificationTypeEnum);
        notificationEntity.setMessage(message);
        notificationEntity.setUserProfileId(userProfileId);
        if(notified) {
            notificationEntity.markAsNotified();
        }
        notificationEntity.setReferenceId(id);

        try {
            notificationManager.save(notificationEntity);
        } catch (Exception exce) {
            String sb = "Failed adding notification: " + exce.getLocalizedMessage() + ", with message: " + message + ", for user: " + userProfileId;
            log.error(sb);
        }
    }

    /**
     * Show notification to the user
     *
     * @param message
     * @param userProfileId
     */
    public void addNotification(String message, NotificationTypeEnum notificationTypeEnum, String userProfileId) {
        if(notificationTypeEnum == NotificationTypeEnum.MESSAGE) {
            addNotification(message, notificationTypeEnum, null, userProfileId, true);
        } else {
            throw new UnsupportedOperationException("Incorrect method call for Notification Type");
        }
    }

    /**
     *
     * @param message
     * @param notificationTypeEnum
     * @param supportedEntity
     */
    public void addNotification(String message, NotificationTypeEnum notificationTypeEnum, BaseEntity supportedEntity) {
        switch (notificationTypeEnum) {
            case EXPENSE_REPORT:
                addNotification(message, notificationTypeEnum, supportedEntity.getId(), ((ReceiptEntity) supportedEntity).getUserProfileId(), true);
                break;
            case RECEIPT:
                addNotification(message, notificationTypeEnum, supportedEntity.getId(), ((ReceiptEntity) supportedEntity).getUserProfileId(), true);
                break;
            case INVOICE:
                addNotification(message, notificationTypeEnum, supportedEntity.getId(), ((ReceiptEntity) supportedEntity).getUserProfileId(), true);
                break;
            case MILEAGE:
                addNotification(message, notificationTypeEnum, supportedEntity.getId(), ((MileageEntity) supportedEntity).getUserProfileId(), true);
                break;
            case DOCUMENT:
                addNotification(message, notificationTypeEnum, supportedEntity.getId(), ((DocumentEntity) supportedEntity).getUserProfileId(), true);
                break;
            default:
                throw new UnsupportedOperationException("Incorrect method call for Notification Type");
        }
    }

    /**
     * List all the notification in descending order
     *
     * @param userProfileId
     * @return
     */
    public List<NotificationEntity> notifications(String userProfileId, int limit) {
        return notificationManager.getAllNotification(userProfileId, limit);
    }

    /**
     * List last five notification in descending order
     *
     * @param userProfileId
     * @return
     */
    public List<NotificationEntity> notifications(String userProfileId) {
        return notifications(userProfileId, NotificationManager.LIMIT_FIVE);
    }
}
