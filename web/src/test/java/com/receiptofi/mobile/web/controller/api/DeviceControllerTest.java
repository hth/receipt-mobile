package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.DeviceService;
import com.receiptofi.mobile.service.MobileAccountService;
import com.receiptofi.mobile.web.controller.AccountControllerTest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class DeviceControllerTest {

    @Mock private DeviceService deviceService;
    @Mock private AuthenticateService authenticateService;
    @Mock private AvailableAccountUpdates availableAccountUpdates;

    private DeviceController deviceController;
    @Mock private HttpServletResponse httpServletResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        deviceController = new DeviceController(deviceService, authenticateService);
    }

    @Test
    public void testHasUpdateFailsToFindUser() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        deviceController.hasUpdate("", "", "did", httpServletResponse);
        verify(deviceService, never()).hasUpdate(anyString(), anyString());
    }

    @Test
    public void testHasUpdateException() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        doThrow(new RuntimeException()).when(deviceService).hasUpdate(anyString(), anyString());
        String responseJson = deviceController.hasUpdate("", "", "did", httpServletResponse);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("something went wrong", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("did", jo.get(ERROR).getAsJsonObject().get("did").getAsString());
    }

    @Test
    public void testHasUpdate() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(deviceService.hasUpdate(anyString(), anyString())).thenReturn(availableAccountUpdates);
        deviceController.hasUpdate("", "", "did", httpServletResponse);
        verify(deviceService, times(1)).hasUpdate(anyString(), anyString());
    }

    @Test
    public void testRegisterDeviceFailsToFindUser() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        assertNull(deviceController.hasUpdate("", "", "did", httpServletResponse));
    }

    @Test
    public void testRegisterDevice() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(deviceService.registerDevice(anyString(), anyString())).thenReturn(true);
        assertTrue(deviceController.registerDevice("", "", "did", httpServletResponse).isRegistered());
    }
}
