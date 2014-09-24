package com.receiptofi.mobile.service;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.RegisteredDeviceEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.repository.RegisteredDeviceManager;
import com.receiptofi.service.LandingService;
import com.receiptofi.service.UserProfilePreferenceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * User: hitender
 * Date: 8/10/14 1:32 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal"
})
@Service
public final class DeviceService {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceService.class);

    private RegisteredDeviceManager registeredDeviceManager;
    private LandingService landingService;
    private UserProfilePreferenceService userProfilePreferenceService;

    @Autowired
    public DeviceService(
            RegisteredDeviceManager registeredDeviceManager,
            LandingService landingService,
            UserProfilePreferenceService userProfilePreferenceService
    ) {
        this.registeredDeviceManager = registeredDeviceManager;
        this.landingService = landingService;
        this.userProfilePreferenceService = userProfilePreferenceService;
    }

    /**
     * Finds if there are new updates since last checked on server.
     *
     * @param rid
     * @param did - Device Id
     * @return
     */
    public AvailableAccountUpdates hasUpdate(String rid, String did) {
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        RegisteredDeviceEntity registeredDevice = registeredDeviceManager.lastAccessed(rid, did);
        if (registeredDevice != null) {
            List<ReceiptEntity> receipts = landingService.getAllUpdatedReceiptSince(rid, registeredDevice.getUpdated());
            if (!receipts.isEmpty()) {
                availableAccountUpdates.setReceipts(receipts);
            }

            UserProfileEntity userProfileEntity = userProfilePreferenceService.getProfileUpdateSince(rid, registeredDevice.getUpdated());
            if (userProfileEntity != null) {
                availableAccountUpdates.setProfile(userProfileEntity);
            }
        } else {
            if (!registerDevice(rid, did)) {
                LOG.warn("device was not registered until now rid={} did={}", rid, did);
            } else {
                LOG.error("could not find registered device rid={} did={}", rid, did);
            }
        }
        return availableAccountUpdates;
    }

    /**
     * Checks if the device is registered, if not registered then it registers the device
     *
     * @param rid
     * @param did
     * @return
     */
    public boolean registerDevice(String rid, String did) {
        RegisteredDeviceEntity registeredDevice = registeredDeviceManager.registerDevice(rid, did);
        if (registeredDevice.getVersion() != null) {
            LOG.info("device registered successfully rid={} did={}", rid, did);
        } else {
            LOG.info("device already registered rid={} did={}", rid, did);
        }
        return true;
    }
}
