package com.receiptofi.mobile.service;

import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.domain.types.PaginationEnum;
import com.receiptofi.mobile.repository.NotificationManagerMobile;
import com.receiptofi.repository.NotificationManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
public class MobileNotificationService {

    @Autowired NotificationManager notificationManager;
    @Autowired NotificationManagerMobile notificationManagerMobile;

    public List<NotificationEntity> getNotifications(String rid, Date since) {
        return notificationManagerMobile.getNotifications(rid, since);
    }

    public List<NotificationEntity> getAllNotifications(String rid) {
        return notificationManager.getNotifications(rid, 0, PaginationEnum.ALL.getLimit());
    }
}
