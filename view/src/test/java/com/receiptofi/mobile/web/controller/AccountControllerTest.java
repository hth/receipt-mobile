package com.receiptofi.mobile.web.controller;

import static com.receiptofi.mobile.service.AccountMobileService.REGISTRATION;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_JSON;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_EXISTING;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.service.AccountMobileService;
import com.receiptofi.mobile.web.validator.UserInfoValidator;
import com.receiptofi.service.AccountService;

import org.springframework.util.StringUtils;

import org.junit.Before;
import org.junit.Test;
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
public class AccountControllerTest {

    public static final String SYSTEM_ERROR_CODE = "systemErrorCode";
    public static final String SYSTEM_ERROR = "systemError";
    public static final String REASON = "reason";
    public static final String ERROR = "error";
    private static final int mailLength = 5;
    private static final int nameLength = 3;
    private static final int passwordLength = 6;
    private static final int countryShortNameLength = 2;

    @Mock private AccountMobileService accountMobileService;
    @Mock private AccountService accountService;
    @Mock private HttpServletResponse response;
    @Mock private UserProfileEntity userProfile;
    private UserInfoValidator userInfoValidator;

    private AccountController accountController;

    @Before
    public void setUp() throws Exception {
        userInfoValidator = new UserInfoValidator(mailLength, nameLength, passwordLength, countryShortNameLength);
        MockitoAnnotations.initMocks(this);
        accountController = new AccountController(
                accountService,
                accountMobileService,
                userInfoValidator);
    }

    @Test
    public void testRegisterUserJsonInvalid() throws IOException {
        String jsonResponse = accountController.registerUser("", response);

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);
        assertEquals(MOBILE_JSON.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(MOBILE_JSON.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Could not parse JSON", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterUserBodyIsEmpty() throws IOException {
        String jsonResponse = accountController.registerUser("{}", response);

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed data validation.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.FN.name()).getAsString());
        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());
        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.PW.name()).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterUserBodyHasEmptyValues() throws IOException {
        String json = createJsonForRegistration("", "", "", "");
        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed data validation.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.FN.name()).getAsString());
        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());
        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.PW.name()).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterValidation_Failure_When_Unknown_Element_In_JsonBody() throws IOException {
        String json = createJsonForRegistration("f", "t@c", "", "XXXXXX");
        json = StringUtils.replace(json, "BD", "DB");

        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(accountMobileService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(MOBILE_JSON.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(MOBILE_JSON.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Could not parse [DB]", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).doesUserExists(anyString());
    }

    @Test
    public void testRegisterValidation_Failure() throws IOException {
        String json = createJsonForRegistration("f", "t@c", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed data validation.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("t@c", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, never()).doesUserExists(anyString());
    }

    @Test
    public void testRegisterUserWhenExists() throws IOException {
        String json = createJsonForRegistration("first", "test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(new UserProfileEntity());
        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_EXISTING.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_EXISTING.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("User already exists. Did you forget password?", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("test@receiptofi.com", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, times(1)).doesUserExists(anyString());
        verify(accountMobileService, never()).signup(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testRegisterUserWhenSignupFailsException() throws IOException {
        String json = createJsonForRegistration("first", "test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        doThrow(new RuntimeException())
                .when(accountMobileService)
                .signup(anyString(), anyString(), anyString(), anyString(), anyString());

        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(SEVERE.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(SEVERE.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Something went wrong. Engineers are looking into this.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("test@receiptofi.com", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, times(1)).doesUserExists(anyString());
        verify(accountMobileService, times(1)).signup(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testRegisterUser() throws IOException {
        String json = createJsonForRegistration("first", "test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(accountMobileService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        when(accountMobileService.acceptingSignup()).thenReturn(true);
        String responseJson = accountController.registerUser(json, response);

        verify(accountService, times(1)).doesUserExists(any(String.class));
        verify(accountMobileService, times(1)).signup(anyString(), anyString(), anyString(), anyString(), anyString());
        assertEquals("{}", responseJson);
    }

    @Test
    public void testRegisterUser_Without_Lastname() throws IOException {
        String jsonResponse = createJsonForRegistration("first last middle", " test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(accountMobileService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        when(accountMobileService.acceptingSignup()).thenReturn(true);
        String responseJson = accountController.registerUser(jsonResponse, response);

        verify(accountService, times(1)).doesUserExists(any(String.class));
        verify(accountMobileService, times(1)).signup(anyString(), anyString(), anyString(), anyString(), anyString());
        assertEquals("{}", responseJson);
    }

    private String createJsonForRegistration(String firstName, String mail, String birthday, String password) {
        JsonObject json = new JsonObject();
        json.addProperty(REGISTRATION.FN.name(), firstName);
        json.addProperty(REGISTRATION.EM.name(), mail);
        json.addProperty(REGISTRATION.BD.name(), birthday);
        json.addProperty(REGISTRATION.PW.name(), password);

        return new Gson().toJson(json);
    }

    @Test
    public void testRecoverJsonInvalid() throws IOException {
        String jsonResponse = accountController.recover("", response);

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);
        assertEquals(MOBILE_JSON.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(MOBILE_JSON.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Could not parse JSON.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRecoverUserBodyIsEmpty() throws IOException {
        String jsonResponse = accountController.recover("{}", response);

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed data validation.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRecoverUserBodyHasEmptyValues() throws IOException {
        String json = createJsonForRecover("");
        String responseJson = accountController.recover(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed data validation.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRecoverValidation_Failure_When_Unknown_Element_In_JsonBody() throws IOException {
        String json = createJsonForRecover("t@c.com");
        json = StringUtils.replace(json, "EM", "ME");

        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(accountMobileService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        String responseJson = accountController.recover(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(MOBILE_JSON.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(MOBILE_JSON.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Could not parse [ME]", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).doesUserExists(anyString());
    }

    @Test
    public void testRecoverValidation_Failure() throws IOException {
        String json = createJsonForRecover("t@c");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        String responseJson = accountController.recover(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed data validation.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("t@c", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, never()).doesUserExists(anyString());
    }

    @Test
    public void testRecoverWhenPasswordRecoverFailsException() throws IOException {
        String json = createJsonForRecover("test@receiptofi.com");
        when(accountService.doesUserExists(anyString())).thenReturn(userProfile);
        doThrow(new RuntimeException())
                .when(accountMobileService)
                .recoverAccount(anyString());

        String responseJson = accountController.recover(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(SEVERE.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(SEVERE.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Something went wrong. Engineers are looking into this.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("test@receiptofi.com", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, times(1)).doesUserExists(anyString());
        verify(accountMobileService, times(1)).recoverAccount(anyString());
    }

    @Test
    public void testRecoverWhenUserProfileNull() throws IOException {
        String json = createJsonForRecover("test@receiptofi.com");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        String responseJson = accountController.recover(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_NOT_FOUND.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_NOT_FOUND.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("User with this email address is not registered. Would you like to sign up?", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("test@receiptofi.com", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, times(1)).doesUserExists(anyString());
        verify(accountMobileService, never()).recoverAccount(anyString());
    }

    @Test
    public void testRecoverFalse() throws IOException {
        String json = createJsonForRecover("test@receiptofi.com");
        when(accountService.doesUserExists(anyString())).thenReturn(userProfile);
        when(userProfile.getProviderId()).thenReturn(null);
        when(accountMobileService.recoverAccount(anyString())).thenReturn(false);
        String responseJson = accountController.recover(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(SEVERE.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(SEVERE.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed sending recovery email. Please try again soon.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("test@receiptofi.com", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, times(1)).doesUserExists(any(String.class));
        verify(accountMobileService, times(1)).recoverAccount(anyString());
    }

    @Test
    public void testRecover() throws IOException {
        String json = createJsonForRecover("test@receiptofi.com");
        when(accountService.doesUserExists(anyString())).thenReturn(userProfile);
        when(accountMobileService.recoverAccount(anyString())).thenReturn(true);
        String responseJson = accountController.recover(json, response);

        verify(accountService, times(1)).doesUserExists(any(String.class));
        verify(accountMobileService, times(1)).recoverAccount(anyString());
        assertEquals("{}", responseJson);
    }

    private String createJsonForRecover(String mail) {
        JsonObject json = new JsonObject();
        json.addProperty(REGISTRATION.EM.name(), mail);

        return new Gson().toJson(json);
    }
}