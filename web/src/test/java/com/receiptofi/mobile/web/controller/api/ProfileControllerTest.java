package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.ERROR;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.REASON;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.SYSTEM_ERROR;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.SYSTEM_ERROR_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.AccountService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class ProfileControllerTest {

    @Mock private AuthenticateService authenticateService;
    @Mock private AccountService accountService;
    @Mock private HttpServletResponse httpServletResponse;
    @Mock private UserAccountEntity userAccountEntity;
    @Mock private UserAuthenticationEntity userAuthenticationEntity;

    private ProfileController profileController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        profileController = new ProfileController(authenticateService, accountService);

        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getId()).thenReturn("");
        when(userAuthenticationEntity.getCreated()).thenReturn(new Date());
        when(userAuthenticationEntity.getCreated()).thenReturn(new Date());
    }

    @Test
    public void testUpdateMailNull() throws IOException {
        when(authenticateService.findUserAccount(anyString(), anyString())).thenReturn(null);
        assertNull(profileController.updateMail("m", "z", "", httpServletResponse));
        verify(accountService, never()).saveUserAccount(any(UserAccountEntity.class));
    }

    @Test
    public void testUpdateMailValidation() throws IOException {
        when(authenticateService.findUserAccount(anyString(), anyString())).thenReturn(userAccountEntity);
        String responseJson = profileController.updateMail("m", "z", createJsonForMail(""), httpServletResponse);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("failed data validation", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).saveUserAccount(any(UserAccountEntity.class));
    }

    @Test
    public void testUpdateMail() throws IOException {
        when(authenticateService.findUserAccount(anyString(), anyString())).thenReturn(userAccountEntity);
        doNothing().when(accountService).saveUserAccount(userAccountEntity);
        profileController.updateMail("m", "z", createJsonForMail("p@x.com"), httpServletResponse);
        verify(accountService, times(1)).saveUserAccount(any(UserAccountEntity.class));
    }

    @Test
    public void testUpdatePasswordNull() throws IOException {
        when(authenticateService.findUserAccount(anyString(), anyString())).thenReturn(null);
        assertNull(profileController.updatePassword("m", "z", "", httpServletResponse));
        verify(accountService, never()).updateAuthentication(any(UserAuthenticationEntity.class));
    }

    @Test
    public void testUpdatePasswordValidation() throws IOException {
        when(authenticateService.findUserAccount(anyString(), anyString())).thenReturn(userAccountEntity);
        String responseJson = profileController.updatePassword("m", "z", createJsonForPassword(""), httpServletResponse);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("failed data validation", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).updateAuthentication(any(UserAuthenticationEntity.class));
    }

    @Test
    public void testUpdatePassword() throws IOException {
        when(authenticateService.findUserAccount(anyString(), anyString())).thenReturn(userAccountEntity);
        doNothing().when(accountService).updateAuthentication(userAuthenticationEntity);
        profileController.updatePassword("m", "z", createJsonForPassword("p"), httpServletResponse);
        verify(accountService, times(1)).updateAuthentication(any(UserAuthenticationEntity.class));
    }

    private String createJsonForMail(String mail) {
        JsonObject json = new JsonObject();
        json.addProperty("UID", mail);

        return new Gson().toJson(json);
    }

    private String createJsonForPassword(String password) {
        JsonObject json = new JsonObject();
        json.addProperty("PA", password);

        return new Gson().toJson(json);
    }
}