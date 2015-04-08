package com.receiptofi.mobile.service;

import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.mobile.repository.NotificationManagerMobile;

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
public class NotificationService {

    @Autowired NotificationManagerMobile notificationManagerMobile;

    public List<NotificationEntity> getNotifications(String rid, Date since) {
        return notificationManagerMobile.getNotifications(rid, since);
    }
}
