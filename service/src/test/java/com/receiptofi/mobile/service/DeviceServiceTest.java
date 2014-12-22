package com.receiptofi.mobile.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;
import com.receiptofi.domain.CommentEntity;
import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.RegisteredDeviceEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.domain.mapping.BizName;
import com.receiptofi.mobile.domain.mapping.BizStore;
import com.receiptofi.repository.RegisteredDeviceManager;
import com.receiptofi.service.LandingService;
import com.receiptofi.service.UserProfilePreferenceService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class DeviceServiceTest {

    @Mock private RegisteredDeviceManager registeredDeviceManager;
    @Mock private LandingService landingService;
    @Mock private UserProfilePreferenceService userProfilePreferenceService;

    @Mock private RegisteredDeviceEntity registeredDeviceEntity;
    @Mock private ReceiptEntity receipt;
    @Mock private UserProfileEntity userProfile;
    @Mock private BizNameEntity bizName;
    @Mock private BizStoreEntity bizStore;
    @Mock private CommentEntity comment;
    @Mock private FileSystemEntity fileSystem;

    private DeviceService deviceService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        deviceService = new DeviceService(registeredDeviceManager, landingService, userProfilePreferenceService);

        when(receipt.getBizName()).thenReturn(bizName);
        when(receipt.getBizStore()).thenReturn(bizStore);
        when(receipt.getNotes()).thenReturn(comment);
        when(receipt.getFileSystemEntities()).thenReturn(Arrays.asList(fileSystem));
        when(receipt.getReceiptDate()).thenReturn(new Date());
    }

    @Test
    public void testHasUpdateWhenNull() {
        when(registeredDeviceManager.lastAccessed(anyString(), anyString())).thenReturn(null);
        assertTrue("Receipt empty", deviceService.hasUpdate(anyString(), anyString()).getReceipts().isEmpty());
    }

    @Test
    public void testHasUpdateNoUserProfileUpdate() {
        when(registeredDeviceManager.lastAccessed(anyString(), anyString())).thenReturn(registeredDeviceEntity);
        when(registeredDeviceEntity.getUpdated()).thenReturn(new Date());
        when(landingService.getAllUpdatedReceiptSince(anyString(), any(Date.class))).thenReturn(Arrays.asList(receipt));
        when(userProfilePreferenceService.getProfileUpdateSince(anyString(), any(Date.class))).thenReturn(null);
        assertFalse("Receipts not empty", deviceService.hasUpdate(anyString(), anyString()).getReceipts().isEmpty());
        assertNull("UserProfile empty", deviceService.hasUpdate(anyString(), anyString()).getProfile());
    }

    @Test
    public void testHasUpdateNoReceiptUpdates() {
        when(registeredDeviceManager.lastAccessed(anyString(), anyString())).thenReturn(registeredDeviceEntity);
        when(registeredDeviceEntity.getUpdated()).thenReturn(new Date());
        when(landingService.getAllUpdatedReceiptSince(anyString(), any(Date.class))).thenReturn(new ArrayList<ReceiptEntity>());
        when(userProfilePreferenceService.getProfileUpdateSince(anyString(), any(Date.class))).thenReturn(userProfile);
        assertTrue("Receipts is empty", deviceService.hasUpdate(anyString(), anyString()).getReceipts().isEmpty());
        assertNotNull("UserProfile not empty", deviceService.hasUpdate(anyString(), anyString()).getProfile());
    }

    @Test
    public void testHasUpdate() {
        when(registeredDeviceManager.lastAccessed(anyString(), anyString())).thenReturn(registeredDeviceEntity);
        when(registeredDeviceEntity.getUpdated()).thenReturn(new Date());
        when(landingService.getAllUpdatedReceiptSince(anyString(), any(Date.class))).thenReturn(Arrays.asList(receipt));
        when(userProfilePreferenceService.getProfileUpdateSince(anyString(), any(Date.class))).thenReturn(userProfile);
        assertFalse("Receipts not empty", deviceService.hasUpdate(anyString(), anyString()).getReceipts().isEmpty());
    }

    @Test
    public void testRegisterDeviceFalse() {
        when(registeredDeviceManager.registerDevice(anyString(), anyString())).thenReturn(null);
        assertFalse("Device registration failure", deviceService.registerDevice(anyString(), anyString()));
    }

    @Test
    public void testRegisterDeviceTrue() {
        when(registeredDeviceManager.registerDevice(anyString(), anyString())).thenReturn(registeredDeviceEntity);
        assertTrue("Device registration success", deviceService.registerDevice(anyString(), anyString()));
    }
}