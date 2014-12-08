package com.receiptofi.mobile.web.controller;

import static com.receiptofi.mobile.service.AccountSignupService.REGISTRATION;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.EXISTING_USER;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_JSON;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;
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
import com.receiptofi.mobile.service.AccountSignupService;
import com.receiptofi.service.AccountService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;

public class AccountRegistrationControllerTest {

    public static final String SYSTEM_ERROR_CODE = "systemErrorCode";
    public static final String SYSTEM_ERROR = "systemError";
    public static final String REASON = "reason";
    public static final String ERROR = "error";

    @Mock private AccountService accountService;
    @Mock private AccountSignupService accountSignupService;
    private HttpServletResponse response;

    private AccountRegistrationController accountRegistrationController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        response = mock(HttpServletResponse.class);
        accountRegistrationController = new AccountRegistrationController(5, 2 ,6, accountService, accountSignupService);
    }

    @Test
    public void testRegisterUserJsonInvalid() throws Exception {
        String jsonResponse = accountRegistrationController.registerUser("", response);

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);
        assertEquals(MOBILE_JSON.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(MOBILE_JSON.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("could not parse JSON", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterUserMapIsNull() throws Exception {
        String jsonResponse = accountRegistrationController.registerUser("{}", response);

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
        String json = createJson("", "", "", "");
        String responseJson = accountRegistrationController.registerUser(json, response);

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
    public void testRegisterUserWhenExists() throws Exception {
        String json = createJson("first", "test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(new UserProfileEntity());
        String responseJson = accountRegistrationController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(EXISTING_USER.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(EXISTING_USER.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("user already exists", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("test@receiptofi.com", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, times(1)).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterUserWhenSignupFails() throws Exception {
        String json = createJson("first", "test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        doThrow(new RuntimeException())
                .when(accountSignupService)
                .signup(anyString(), anyString(), anyString(), anyString(), anyString());

        String responseJson = accountRegistrationController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(SEVERE.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(SEVERE.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("failed creating account", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("test@receiptofi.com", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, times(1)).doesUserExists(any(String.class));
    }

    @Test
    public void testRegisterUserWhenDoesNotExists() throws Exception {
        String json = createJson("first", "test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(accountSignupService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        String responseJson = accountRegistrationController.registerUser(json, response);

        verify(accountService, times(1)).doesUserExists(any(String.class));
        assertEquals("{}", responseJson);
    }

    @Test
    public void testRegisterUserWhenDoesNotExists_With_Lastname() throws Exception {
        String jsonResponse = createJson("first last middle", " test@receiptofi.com", "", "XXXXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(accountSignupService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        String responseJson = accountRegistrationController.registerUser(jsonResponse, response);

        verify(accountService, times(1)).doesUserExists(any(String.class));
        assertEquals("{}", responseJson);
    }


    @Test
    public void testRegisterValidation_Failure() throws Exception {
        String json = createJson("f", "t@c", "", "XXXX");
        when(accountService.doesUserExists(anyString())).thenReturn(null);
        when(accountSignupService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("1234");
        String responseJson = accountRegistrationController.registerUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(USER_INPUT.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(USER_INPUT.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("failed data validation", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("t@c", jo.get(ERROR).getAsJsonObject().get(REGISTRATION.EM.name()).getAsString());

        verify(accountService, never()).doesUserExists(any(String.class));
    }

    private String createJson(String firstName, String mail, String birthday, String password) {
        JsonObject json = new JsonObject();
        json.addProperty(REGISTRATION.FN.name(), firstName);
        json.addProperty(REGISTRATION.EM.name(), mail);
        json.addProperty(REGISTRATION.BD.name(), birthday);
        json.addProperty(REGISTRATION.PW.name(), password);

        return new Gson().toJson(json);
    }
}