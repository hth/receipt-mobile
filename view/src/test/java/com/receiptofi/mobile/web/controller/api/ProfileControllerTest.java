package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_EXISTING;
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
import com.receiptofi.mobile.service.AccountMobileService;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.web.validator.UserInfoValidator;
import com.receiptofi.service.AccountService;
import com.receiptofi.utils.ScrubbedInput;

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
    @Mock private AccountMobileService accountMobileService;
    private UserInfoValidator userInfoValidator;
    private ProfileController profileController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userInfoValidator = new UserInfoValidator(5, 3, 6, 2);
        profileController = new ProfileController(
                authenticateService,
                accountService,
                accountMobileService,
                userInfoValidator
        );

        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getId()).thenReturn("");
        when(userAuthenticationEntity.getCreated()).thenReturn(new Date());
        when(userAuthenticationEntity.getCreated()).thenReturn(new Date());
    }

    @Test
    public void testUpdateMailNull() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        assertNull(profileController.mail(new ScrubbedInput("m"), new ScrubbedInput("z"), "", httpServletResponse));

        verify(accountService, never()).findByUserId(anyString());
    }

    @Test
    public void testUpdateMailValidation() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("");
        String responseJson = profileController.mail(new ScrubbedInput("m"), new ScrubbedInput("z"), createJsonForMail(""), httpServletResponse);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed data validation.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).findByUserId(anyString());
    }

    @Test
    public void testUpdateMailUserExists() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("");
        when(accountService.findByUserId(anyString())).thenReturn(userAccountEntity);

        String responseJson = profileController.mail(new ScrubbedInput("m"), new ScrubbedInput("z"), createJsonForMail("p@x.com"), httpServletResponse);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_EXISTING.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_EXISTING.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("User already exists with this mail.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("p@x.com", jo.get(ERROR).getAsJsonObject().get(AccountMobileService.REGISTRATION.EM.name()).getAsString());

        verify(accountMobileService, never()).changeUID(anyString(), anyString());
    }

    @Test
    public void testUpdateMail() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("");
        when(accountService.findByUserId(anyString())).thenReturn(null);
        when(accountMobileService.changeUID(anyString(), anyString())).thenReturn(userAccountEntity);

        profileController.mail(new ScrubbedInput("m"), new ScrubbedInput("z"), createJsonForMail("p@x.com"), httpServletResponse);

        verify(accountMobileService, times(1)).changeUID(anyString(), anyString());
    }

    @Test
    public void testUpdatePasswordNull() throws IOException {
        when(authenticateService.findUserAccount(anyString(), anyString())).thenReturn(null);
        assertNull(profileController.password(new ScrubbedInput("m"), new ScrubbedInput("z"), "", httpServletResponse));
        verify(accountService, never()).updateAuthentication(any(UserAuthenticationEntity.class));
    }

    @Test
    public void testUpdatePasswordValidation() throws IOException {
        when(authenticateService.findUserAccount(anyString(), anyString())).thenReturn(userAccountEntity);
        String responseJson = profileController.password(new ScrubbedInput("m"), new ScrubbedInput("z"), createJsonForPassword(""), httpServletResponse);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed data validation.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).updateAuthentication(any(UserAuthenticationEntity.class));
    }

    @Test
    public void testUpdatePassword() throws IOException {
        when(authenticateService.findUserAccount(anyString(), anyString())).thenReturn(userAccountEntity);
        when(userAccountEntity.isAccountValidated()).thenReturn(true);
        doNothing().when(accountService).updateAuthentication(userAuthenticationEntity);
        profileController.password(new ScrubbedInput("m"), new ScrubbedInput("z"), createJsonForPassword("password"), httpServletResponse);
        verify(accountService, times(1)).updateAuthentication(any(UserAuthenticationEntity.class));
    }

    private String createJsonForMail(String mail) {
        JsonObject json = new JsonObject();
        json.addProperty(AccountMobileService.REGISTRATION.EM.name(), mail);

        return new Gson().toJson(json);
    }

    private String createJsonForPassword(String password) {
        JsonObject json = new JsonObject();
        json.addProperty(AccountMobileService.REGISTRATION.PW.name(), password);

        return new Gson().toJson(json);
    }
}