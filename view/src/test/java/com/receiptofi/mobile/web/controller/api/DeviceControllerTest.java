package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.ERROR;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.REASON;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.SYSTEM_ERROR;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.SYSTEM_ERROR_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.receiptofi.domain.types.DeviceTypeEnum;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.DeviceService;
import com.receiptofi.utils.ScrubbedInput;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

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
    @Mock private HttpServletResponse httpServletResponse;

    private DeviceController deviceController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        deviceController = new DeviceController(deviceService, authenticateService);
    }

    @Test
    public void testHasUpdateFailsToFindUser() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        deviceController.updates(
                new ScrubbedInput(""),
                new ScrubbedInput(""),
                new ScrubbedInput("did"),
                new ScrubbedInput(""),
                new ScrubbedInput(""),
                httpServletResponse);

        verify(deviceService, never()).getUpdates(anyString(), anyString(), Matchers.any(DeviceTypeEnum.class), anyString());
    }

    @Test
    public void testHasUpdateException() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        doThrow(new RuntimeException()).when(deviceService).getUpdates(anyString(), anyString(), Matchers.any(DeviceTypeEnum.class), anyString());
        String responseJson = deviceController.updates(
                new ScrubbedInput(""),
                new ScrubbedInput(""),
                new ScrubbedInput("did"),
                new ScrubbedInput(DeviceTypeEnum.A.getName()),
                new ScrubbedInput(""),
                httpServletResponse);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Something went wrong. Engineers are looking into this.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("did", jo.get(ERROR).getAsJsonObject().get("did").getAsString());
    }

    @Test
    public void testHasUpdate() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(deviceService.getUpdates(anyString(), anyString(), Matchers.any(DeviceTypeEnum.class), anyString())).thenReturn(availableAccountUpdates);
        deviceController.updates(
                new ScrubbedInput(""),
                new ScrubbedInput(""),
                new ScrubbedInput("did"),
                new ScrubbedInput(DeviceTypeEnum.A.getName()),
                new ScrubbedInput(""),
                httpServletResponse);

        verify(deviceService, times(1)).getUpdates(anyString(), anyString(), Matchers.any(DeviceTypeEnum.class), anyString());
    }

    @Test
    public void testRegisterDeviceFailsToFindUser() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        assertNull(deviceController.registerDevice(
                new ScrubbedInput(""),
                new ScrubbedInput(""),
                new ScrubbedInput("did"),
                new ScrubbedInput(DeviceTypeEnum.A.getName()),
                new ScrubbedInput(""),
                httpServletResponse));
    }

    @Test
    public void testRegisterDevice() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(deviceService.registerDevice(anyString(), anyString(), Matchers.any(DeviceTypeEnum.class), anyString())).thenReturn(true);
        String responseJson = deviceController.registerDevice(
                new ScrubbedInput(""),
                new ScrubbedInput(""),
                new ScrubbedInput("did"),
                new ScrubbedInput(DeviceTypeEnum.A.getName()),
                new ScrubbedInput(""),
                httpServletResponse);
        
        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertTrue(jo.getAsJsonPrimitive("registered").getAsBoolean());
    }
}
