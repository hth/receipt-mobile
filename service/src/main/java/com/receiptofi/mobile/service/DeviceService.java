package com.receiptofi.mobile.service;

import java.util.List;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.RegisteredDeviceEntity;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.repository.RegisteredDeviceManager;
import com.receiptofi.service.LandingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 8/10/14 1:32 PM
 */
@Service
public final class DeviceService {
    private static Logger log = LoggerFactory.getLogger(DeviceService.class);

    private RegisteredDeviceManager registeredDeviceManager;
    private LandingService landingService;

    @Autowired
    public DeviceService(RegisteredDeviceManager registeredDeviceManager, LandingService landingService) {
        this.registeredDeviceManager = registeredDeviceManager;
        this.landingService = landingService;
    }

    /**
     * Finds if there are new updates since last checked on server.
     * @param rid
     * @param did - Device Id
     * @return
     */
    public AvailableAccountUpdates hasUpdate(String rid, String did) {
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        RegisteredDeviceEntity registeredDevice = registeredDeviceManager.lastAccessed(rid, did);
        if(registeredDevice != null) {
            List<ReceiptEntity> receipts = landingService.getAllUpdatedReceiptSince(rid, registeredDevice.getUpdated());
            if(!receipts.isEmpty()) {
                availableAccountUpdates.addReceipts(receipts);
            }
        } else {
            if(!isDeviceRegistered(rid, did)) {
                log.warn("device was not registered until now rid={} did={}", rid, did);
            } else {
                log.error("could not find registered device rid={} did={}", rid, did);
            }
        }
        return availableAccountUpdates;
    }

    /**
     * Checks if the device is registered, if not registered then it registers the device
     * @param rid
     * @param did
     * @return
     */
    public boolean isDeviceRegistered(String rid, String did) {
        boolean registration = registeredDeviceManager.findOrRegisterWhenNotFound(rid, did);
        if(!registration) {
            log.info("device registered successfully rid={} did={}", rid, did);
        } else {
            log.info("device already registered rid={} did={}", rid, did);
        }
        return registration;
    }
}
