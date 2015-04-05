package com.receiptofi.mobile.service;

import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.RegisteredDeviceEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.repository.RegisteredDeviceManager;
import com.receiptofi.service.ExpensesService;
import com.receiptofi.service.ItemService;
import com.receiptofi.service.LandingService;
import com.receiptofi.service.NotificationService;
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
    private LandingService landingService;
    private UserProfilePreferenceService userProfilePreferenceService;
    private ItemService itemService;
    private ExpensesService expensesService;
    private NotificationService notificationService;

    @Autowired
    public DeviceService(
            RegisteredDeviceManager registeredDeviceManager,
            LandingService landingService,
            UserProfilePreferenceService userProfilePreferenceService,
            ItemService itemService,
            ExpensesService expensesService,
            NotificationService notificationService
    ) {
        this.registeredDeviceManager = registeredDeviceManager;
        this.landingService = landingService;
        this.userProfilePreferenceService = userProfilePreferenceService;
        this.itemService = itemService;
        this.expensesService = expensesService;
        this.notificationService = notificationService;
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

            List<ReceiptEntity> receipts = landingService.getAllUpdatedReceiptSince(rid, updated);
            if (!receipts.isEmpty()) {
                availableAccountUpdates.addJsonReceipts(receipts);
                for (ReceiptEntity receipt : receipts) {
                    availableAccountUpdates.addJsonReceiptItems(itemService.getAllItemsOfReceipt(receipt.getId()));
                }
            }

            UserProfileEntity userProfile = userProfilePreferenceService.getProfileUpdateSince(rid, updated);
            if (null != userProfile) {
                availableAccountUpdates.setProfile(userProfile);
            }

            List<NotificationEntity> notifications = notificationService.getNotifications(rid, updated);
            if (!notifications.isEmpty()) {
                availableAccountUpdates.setJsonNotifications(notifications);
            }
        }

        availableAccountUpdates.addJsonExpenseTag(expensesService.activeExpenseTypes(rid));
        availableAccountUpdates.setUnprocessedDocuments(landingService.pendingReceipt(rid));
        return availableAccountUpdates;
    }

    /**
     * Gets all available data for RID.
     *
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

        List<ReceiptEntity> receipts = landingService.getAllReceipts(rid);
        if (!receipts.isEmpty()) {
            availableAccountUpdates.addJsonReceipts(receipts);
            for (ReceiptEntity receipt : receipts) {
                availableAccountUpdates.addJsonReceiptItems(itemService.getAllItemsOfReceipt(receipt.getId()));
            }
        }

        UserProfileEntity userProfile = userProfilePreferenceService.findByReceiptUserId(rid);
        if (null != userProfile) {
            availableAccountUpdates.setProfile(userProfile);
        }

        availableAccountUpdates.addJsonExpenseTag(expensesService.activeExpenseTypes(rid));
        availableAccountUpdates.setUnprocessedDocuments(landingService.pendingReceipt(rid));
        availableAccountUpdates.setJsonNotifications(notificationService.getAllNotifications(rid));
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
}
