package com.receiptofi.mobile.service;

import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.RegisteredDeviceEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.repository.RegisteredDeviceManager;
import com.receiptofi.service.UserProfilePreferenceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 8/10/14 1:32 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Service
public class DeviceService {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceService.class);

    private RegisteredDeviceManager registeredDeviceManager;
    private ReceiptMobileService receiptMobileService;
    private UserProfilePreferenceService userProfilePreferenceService;
    private ExpenseTagMobileService expenseTagMobileService;
    private NotificationMobileService notificationMobileService;
    private DocumentMobileService documentMobileService;
    private BillingMobileService billingMobileService;

    @Autowired
    public DeviceService(
            RegisteredDeviceManager registeredDeviceManager,
            UserProfilePreferenceService userProfilePreferenceService,
            ExpenseTagMobileService expenseTagMobileService,
            NotificationMobileService notificationMobileService,
            ReceiptMobileService receiptMobileService,
            DocumentMobileService documentMobileService,
            BillingMobileService billingMobileService
    ) {
        this.registeredDeviceManager = registeredDeviceManager;
        this.userProfilePreferenceService = userProfilePreferenceService;
        this.expenseTagMobileService = expenseTagMobileService;
        this.notificationMobileService = notificationMobileService;
        this.receiptMobileService = receiptMobileService;
        this.documentMobileService = documentMobileService;
        this.billingMobileService = billingMobileService;
    }

    /**
     * Finds if there are new updates since last checked on server.
     *
     * @param rid
     * @param did Device Id
     * @return
     */
    public AvailableAccountUpdates getUpdates(String rid, String did) {
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        RegisteredDeviceEntity registeredDevice = registeredDeviceManager.lastAccessed(rid, did);
        if (null == registeredDevice) {
            registeredDevice = registeredDeviceManager.registerDevice(rid, did);
        }

        if (null != registeredDevice) {
            Date updated = registeredDevice.getUpdated();
            LOG.info("Device last updated date={}", updated);

            List<ReceiptEntity> receipts = receiptMobileService.getAllUpdatedReceiptSince(rid, updated);
            receiptMobileService.getReceiptAndItemUpdates(availableAccountUpdates, receipts);

            UserProfileEntity userProfile = userProfilePreferenceService.getProfileUpdateSince(rid, updated);
            if (null != userProfile) {
                availableAccountUpdates.setProfile(userProfile);
            }

            List<NotificationEntity> notifications = notificationMobileService.getNotifications(rid, updated);
            if (!notifications.isEmpty()) {
                availableAccountUpdates.setJsonNotifications(notifications);
            }

            billingMobileService.getBilling(rid, updated, availableAccountUpdates);
        }

        expenseTagMobileService.getExpenseTag(rid, availableAccountUpdates);
        documentMobileService.getUnprocessedDocuments(rid, availableAccountUpdates);

        return availableAccountUpdates;
    }

    /**
     * Gets all available data for RID.
     * Receipts
     * Items
     * UserProfile
     * ExpenseTag
     * Unprocessed Document
     * Notification
     *
     * @param rid
     * @return
     */
    public AvailableAccountUpdates getAll(String rid) {
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        LOG.info("Device registered now. Getting all updated.");

        List<ReceiptEntity> receipts = receiptMobileService.getAllReceipts(rid);
        receiptMobileService.getReceiptAndItemUpdates(availableAccountUpdates, receipts);

        UserProfileEntity userProfile = userProfilePreferenceService.findByReceiptUserId(rid);
        if (null != userProfile) {
            availableAccountUpdates.setProfile(userProfile);
        }

        availableAccountUpdates.setJsonNotifications(notificationMobileService.getAllNotifications(rid));
        billingMobileService.getBilling(rid, availableAccountUpdates);

        expenseTagMobileService.getExpenseTag(rid, availableAccountUpdates);
        documentMobileService.getUnprocessedDocuments(rid, availableAccountUpdates);

        return availableAccountUpdates;
    }


    /**
     * Checks if the device is registered, if not registered then it registers the device.
     *
     * @param rid
     * @param did
     * @return
     */
    public boolean registerDevice(String rid, String did) {
        boolean registrationSuccess = false;
        RegisteredDeviceEntity registeredDevice = registeredDeviceManager.registerDevice(rid, did);
        if (null == registeredDevice) {
            LOG.error("Failure device registration rid={} did={}", rid, did);
        } else {
            LOG.info("Success device registration rid={} did={}", rid, registeredDevice.getDeviceId());
            registrationSuccess = true;
        }
        return registrationSuccess;
    }

    public boolean isDeviceRegistered(String rid, String did) {
        return registeredDeviceManager.find(rid, did) != null;
    }
}
