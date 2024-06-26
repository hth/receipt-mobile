package com.receiptofi.mobile.service;

import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.domain.types.PaginationEnum;
import com.receiptofi.mobile.repository.NotificationManagerMobile;
import com.receiptofi.mobile.util.Util;
import com.receiptofi.repository.NotificationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 4/7/15 8:43 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Service
public class NotificationMobileService {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationMobileService.class);

    private NotificationManager notificationManager;
    private NotificationManagerMobile notificationManagerMobile;

    @Autowired
    public NotificationMobileService(
            NotificationManager notificationManager,
            NotificationManagerMobile notificationManagerMobile) {
        this.notificationManager = notificationManager;
        this.notificationManagerMobile = notificationManagerMobile;
    }

    public List<NotificationEntity> getNotifications(String rid, Date since) {
        return notificationManagerMobile.getNotifications(rid, since);
    }

    public List<NotificationEntity> getAllNotifications(String rid) {
        return notificationManager.getNotifications(rid, 0, PaginationEnum.ALL.getLimit());
    }

    public void markNotificationRead(String notificationIds, String rid) {
        List<String> list = Util.convertCommaSeparatedStringToList(notificationIds);
        LOG.info("Notification marked read count={} rid={}", list.size(), rid);

        notificationManager.markNotificationRead(list, rid);
    }
}
