package com.receiptofi.mobile.repository;

import com.receiptofi.domain.NotificationEntity;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 4/7/15 8:39 PM
 */
public interface NotificationManagerMobile extends RepositoryManager<NotificationEntity> {

    List<NotificationEntity> getNotifications(String rid, Date since);
}
