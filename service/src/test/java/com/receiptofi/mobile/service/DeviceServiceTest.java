package com.receiptofi.mobile.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;
import com.receiptofi.domain.CommentEntity;
import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.RegisteredDeviceEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.types.BilledStatusEnum;
import com.receiptofi.repository.RegisteredDeviceManager;
import com.receiptofi.service.ExpensesService;
import com.receiptofi.service.UserProfilePreferenceService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class DeviceServiceTest {

    @Mock private RegisteredDeviceManager registeredDeviceManager;
    @Mock private MobileReceiptService mobileReceiptService;
    @Mock private UserProfilePreferenceService userProfilePreferenceService;
    @Mock private ExpensesService expensesService;
    @Mock private MobileNotificationService mobileNotificationService;
    @Mock private DocumentMobileService documentMobileService;

    @Mock private RegisteredDeviceEntity registeredDeviceEntity;
    @Mock private ReceiptEntity receipt;
    @Mock private UserProfileEntity userProfile;
    @Mock private BizNameEntity bizName;
    @Mock private BizStoreEntity bizStore;
    @Mock private CommentEntity comment;
    @Mock private FileSystemEntity fileSystem;
    @Mock private ItemEntity item;
    @Mock private ExpenseTagEntity expenseTag;

    private DeviceService deviceService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        deviceService = new DeviceService(
                registeredDeviceManager,
                userProfilePreferenceService,
                expensesService,
                mobileNotificationService,
                mobileReceiptService,
                documentMobileService);

        when(receipt.getBizName()).thenReturn(bizName);
        when(receipt.getBizStore()).thenReturn(bizStore);
        when(receipt.getNotes()).thenReturn(comment);
        when(receipt.getFileSystemEntities()).thenReturn(Collections.singletonList(fileSystem));
        when(receipt.getReceiptDate()).thenReturn(new Date());
        when(receipt.getBilledStatus()).thenReturn(BilledStatusEnum.P);

        when(item.getReceipt()).thenReturn(receipt);
        when(item.getExpenseTag()).thenReturn(expenseTag);
    }

    @Test
    public void testHasUpdateWhenNull() {
        when(registeredDeviceManager.lastAccessed(anyString(), anyString())).thenReturn(null);
        assertTrue("Receipt empty", deviceService.getUpdates(anyString(), anyString()).getJsonReceipts().isEmpty());
    }

    @Test
    public void testHasUpdateNoUserProfileUpdate() {
        when(registeredDeviceManager.lastAccessed(anyString(), anyString())).thenReturn(registeredDeviceEntity);
        when(registeredDeviceEntity.getUpdated()).thenReturn(new Date());
        when(mobileReceiptService.getAllUpdatedReceiptSince(anyString(), any(Date.class))).thenReturn(Collections.singletonList(receipt));
        when(userProfilePreferenceService.getProfileUpdateSince(anyString(), any(Date.class))).thenReturn(null);
        assertNull("UserProfile empty", deviceService.getUpdates(anyString(), anyString()).getProfile());
    }

    @Test
    public void testHasUpdateNoReceiptUpdates() {
        when(registeredDeviceManager.lastAccessed(anyString(), anyString())).thenReturn(registeredDeviceEntity);
        when(registeredDeviceEntity.getUpdated()).thenReturn(new Date());
        when(mobileReceiptService.getAllUpdatedReceiptSince(anyString(), any(Date.class))).thenReturn(new ArrayList<>());
        when(userProfilePreferenceService.getProfileUpdateSince(anyString(), any(Date.class))).thenReturn(userProfile);
        assertTrue("Receipts is empty", deviceService.getUpdates(anyString(), anyString()).getJsonReceipts().isEmpty());
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
