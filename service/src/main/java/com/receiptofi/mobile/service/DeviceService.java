package com.receiptofi.mobile.service;

import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.RegisteredDeviceEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.types.DeviceTypeEnum;
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
    private FriendMobileService friendMobileService;
    private SplitExpensesMobileService splitExpensesMobileService;
    private CouponMobileService couponMobileService;
    private PaymentCardMobileService paymentCardMobileService;

    @Autowired
    public DeviceService(
            RegisteredDeviceManager registeredDeviceManager,
            UserProfilePreferenceService userProfilePreferenceService,
            ExpenseTagMobileService expenseTagMobileService,
            NotificationMobileService notificationMobileService,
            ReceiptMobileService receiptMobileService,
            DocumentMobileService documentMobileService,
            BillingMobileService billingMobileService,
            FriendMobileService friendMobileService,
            SplitExpensesMobileService splitExpensesMobileService,
            CouponMobileService couponMobileService,
            PaymentCardMobileService paymentCardMobileService
    ) {
        this.registeredDeviceManager = registeredDeviceManager;
        this.userProfilePreferenceService = userProfilePreferenceService;
        this.expenseTagMobileService = expenseTagMobileService;
        this.notificationMobileService = notificationMobileService;
        this.receiptMobileService = receiptMobileService;
        this.documentMobileService = documentMobileService;
        this.billingMobileService = billingMobileService;
        this.friendMobileService = friendMobileService;
        this.splitExpensesMobileService = splitExpensesMobileService;
        this.couponMobileService = couponMobileService;
        this.paymentCardMobileService = paymentCardMobileService;
    }

    /**
     * Finds if there are new updates since last checked on server.
     *
     * @param rid
     * @param did        Device Id
     * @param deviceType iPhone or Android
     * @param token      Notification token
     * @return
     */
    public AvailableAccountUpdates getUpdates(String rid, String did, DeviceTypeEnum deviceType, String token) {
        AvailableAccountUpdates availableAccountUpdates;
        if (!isDeviceRegistered(rid, did)) {
            LOG.info("Device registered rid={} did={}", rid, did);
            registeredDeviceManager.registerDevice(rid, did, deviceType, token);
            availableAccountUpdates = getAll(rid);
        } else {
            availableAccountUpdates = getUpdates(rid, did, token);
        }

        LOG.info("{} {}", availableAccountUpdates.getType(), availableAccountUpdates);
        return availableAccountUpdates;
    }

    /**
     * Every time iPhone App is opened, token is received and that token is sent across.
     *
     * @param rid
     * @param did
     * @param token
     * @return
     */
    private AvailableAccountUpdates getUpdates(String rid, String did, String token) {
        return getAvailableAccountUpdates(lastAccessed(rid, did, token));
    }

    public AvailableAccountUpdates getUpdates(String rid, String did) {
        return getAvailableAccountUpdates(lastAccessed(rid, did));
    }

    private AvailableAccountUpdates getAvailableAccountUpdates(RegisteredDeviceEntity registeredDevice) {
        String rid = registeredDevice.getReceiptUserId();

        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        Date updated = registeredDevice.getUpdated();
        LOG.info("rid={} did={} last updated={}", rid, registeredDevice.getDeviceId(), updated);

        List<ReceiptEntity> receipts = receiptMobileService.getAllUpdatedReceiptSince(rid, updated);
        receiptMobileService.getReceiptAndItemUpdates(availableAccountUpdates, rid, receipts);

        UserProfileEntity userProfile = userProfilePreferenceService.getProfileUpdateSince(rid, updated);
        if (null != userProfile) {
            availableAccountUpdates.setProfile(userProfile);
        }

        List<NotificationEntity> notifications = notificationMobileService.getNotifications(rid, updated);
        if (!notifications.isEmpty()) {
            availableAccountUpdates.setJsonNotifications(notifications);
        }

        billingMobileService.getBilling(rid, updated, availableAccountUpdates);

        //Put this under since update condition; add updated
        expenseTagMobileService.getAllExpenseTags(rid, availableAccountUpdates);
        documentMobileService.getUnprocessedDocuments(rid, availableAccountUpdates);

        //TODO remove me or change this to get only updates. I have not yet decided.
        friendMobileService.getActiveFriends(rid, availableAccountUpdates);
        friendMobileService.getPendingFriends(rid, availableAccountUpdates);
        friendMobileService.getAwaitingFriends(rid, availableAccountUpdates);

        splitExpensesMobileService.getJsonOwe(rid, availableAccountUpdates);
        splitExpensesMobileService.getJsonOwesOther(rid, availableAccountUpdates);

        couponMobileService.getCouponUpdateSince(rid, updated, availableAccountUpdates);
        paymentCardMobileService.getPaymentCardUpdatedSince(rid, updated, availableAccountUpdates);

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
     * Friends
     * PendingFriends
     * AwaitingFriends
     *
     * @param rid
     * @return
     */
    public AvailableAccountUpdates getAll(String rid) {
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance(AvailableAccountUpdates.Type.ALL);
        LOG.info("Getting all data rid={}", rid);

        List<ReceiptEntity> receipts = receiptMobileService.getAllReceipts(rid);
        receiptMobileService.getReceiptAndItemUpdates(availableAccountUpdates, rid, receipts);

        UserProfileEntity userProfile = userProfilePreferenceService.findByReceiptUserId(rid);
        if (null != userProfile) {
            availableAccountUpdates.setProfile(userProfile);
        }

        availableAccountUpdates.setJsonNotifications(notificationMobileService.getAllNotifications(rid));
        billingMobileService.getBilling(rid, availableAccountUpdates);

        expenseTagMobileService.getAllExpenseTags(rid, availableAccountUpdates);
        documentMobileService.getUnprocessedDocuments(rid, availableAccountUpdates);

        friendMobileService.getActiveFriends(rid, availableAccountUpdates);
        friendMobileService.getPendingFriends(rid, availableAccountUpdates);
        friendMobileService.getAwaitingFriends(rid, availableAccountUpdates);

        splitExpensesMobileService.getJsonOwe(rid, availableAccountUpdates);
        splitExpensesMobileService.getJsonOwesOther(rid, availableAccountUpdates);

        couponMobileService.getAll(rid, availableAccountUpdates);
        paymentCardMobileService.getAll(rid, availableAccountUpdates);

        return availableAccountUpdates;
    }

    /**
     * Checks if the device is registered, if not registered then it registers the device.
     *
     * @param rid
     * @param did
     * @param deviceType iPhone or Android
     * @return
     */
    public boolean registerDevice(String rid, String did, DeviceTypeEnum deviceType, String token) {
        boolean registrationSuccess = false;
        RegisteredDeviceEntity registeredDevice = registeredDeviceManager.registerDevice(rid, did, deviceType, token);
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

    public RegisteredDeviceEntity lastAccessed(String rid, String did) {
        return registeredDeviceManager.lastAccessed(rid, did);
    }

    private RegisteredDeviceEntity lastAccessed(String rid, String did, String token) {
        return registeredDeviceManager.lastAccessed(rid, did, token);
    }
}
