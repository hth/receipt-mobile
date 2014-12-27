package com.receiptofi.mobile.web.controller;

import static com.receiptofi.mobile.service.MobileAccountService.REGISTRATION;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_JSON;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_EXISTING;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.mobile.service.MobileAccountService;
import com.receiptofi.service.AccountService;

import org.springframework.util.StringUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    private static final int nameLength = 2;
    private static final int passwordLength = 6;

    @Mock private MobileAccountService mobileAccountService;
    @Mock private AccountService accountService;
    @Mock private HttpServletResponse response;

    private AccountController accountController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        accountController = new AccountController(
                mailLength,
                nameLength,
                passwordLength,
                accountService,
                mobileAccountService);
    }

    @Test
    public void testRegisterUserJsonInvalid() throws Exception {
        String jsonResponse = accountController.registerUser("", response);

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);
        assertEquals(MOBILE_JSON.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(MOBILE_JSON.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("could not parse JSON", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterUserMapIsNull() throws Exception {
        String jsonResponse = accountController.registerUser("{}", response);

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("failed data validation", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.FN.name()).getAsString());
        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());
        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.PW.name()).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterUserMapIsBlank() throws Exception {
        String json = createJsonForRegistration("", "", "", "");
        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("failed data validation", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.FN.name()).getAsString());
        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());
        assertEquals("Empty", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.PW.name()).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterValidation_Failure_When_Unknown_Element_In_JsonBody() throws Exception {
        String json = createJsonForRegistration("f", "t@c", "", "XXXXXX");
        json = StringUtils.replace(json, "BD", "DB");

        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(mobileAccountService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(MOBILE_JSON.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(MOBILE_JSON.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("could not parse [DB]", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).doesUserExists(anyString());
    }

    @Test
    public void testRegisterValidation_Failure() throws Exception {
        String json = createJsonForRegistration("f", "t@c", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("failed data validation", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("t@c", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, never()).doesUserExists(anyString());
    }

    @Test
    public void testRegisterUserWhenExists() throws Exception {
        String json = createJsonForRegistration("first", "test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(new UserProfileEntity());
        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_EXISTING.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_EXISTING.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("user already exists", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("test@receiptofi.com", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, times(1)).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterUserWhenSignupFails() throws Exception {
        String json = createJsonForRegistration("first", "test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        doThrow(new RuntimeException())
                .when(mobileAccountService)
                .signup(anyString(), anyString(), anyString(), anyString(), anyString());

        String responseJson = accountController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(SEVERE.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(SEVERE.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("failed creating account", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("test@receiptofi.com", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, times(1)).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterUser() throws Exception {
        String json = createJsonForRegistration("first", "test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(mobileAccountService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        String responseJson = accountController.registerUser(json, response);

        verify(accountService, times(1)).doesUserExists(any(String.class));
        verify(mobileAccountService, times(1)).signup(anyString(), anyString(), anyString(), anyString(), anyString());
        assertEquals("{}", responseJson);
    }

    @Test
    public void testRegisterUser_Without_Lastname() throws Exception {
        String jsonResponse = createJsonForRegistration("first last middle", " test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(mobileAccountService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        String responseJson = accountController.registerUser(jsonResponse, response);

        verify(accountService, times(1)).doesUserExists(any(String.class));
        verify(mobileAccountService, times(1)).signup(anyString(), anyString(), anyString(), anyString(), anyString());
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
}