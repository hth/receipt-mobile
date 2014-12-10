package com.receiptofi.mobile.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.receiptofi.domain.types.ProviderEnum;
import com.receiptofi.mobile.service.SocialAuthenticationService;

import org.apache.http.client.HttpClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;

public class SocialAuthenticationControllerTest {

    @Mock private SocialAuthenticationService socialAuthenticationService;
    private HttpServletResponse response;

    private SocialAuthenticationController socialAuthenticationController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        response = mock(HttpServletResponse.class);
        socialAuthenticationController = new SocialAuthenticationController(socialAuthenticationService);
    }

    @Test
    public void testAuthenticateUserJsonInvalid() throws Exception {
        String jsonResponse = socialAuthenticationController.authenticateUser("", response);
        verify(socialAuthenticationService, never())
                .authenticateWeb(any(String.class), any(String.class), any(HttpClient.class));

        assertEquals("{\"error\":{\"systemErrorCode\":\"500\",\"systemError\":\"SEVERE\",\"reason\":\"Internal error, please try some time later.\"}}", jsonResponse);
    }

    @Test
    public void testAuthenticateUserCredentialFail() throws Exception {
        String json = createJson(ProviderEnum.GOOGLE.name(), "1234");
        when(socialAuthenticationService.authenticateWeb(anyString(), anyString(), any(HttpClient.class)))
                .thenReturn("{\n" +
                        "    \"error\": {\n" +
                        "        \"httpStatus\": \"UNAUTHORIZED\",\n" +
                        "        \"httpStatusCode\": 401,\n" +
                        "        \"reason\": \"denied by provider GOOGLE\",\n" +
                        "        \"systemError\": \"AUTHENTICATION\",\n" +
                        "        \"systemErrorCode\": \"400\"\n" +
                        "    }\n" +
                        "}");

        String jsonResponse = socialAuthenticationController.authenticateUser(json, response);
        assertEquals("{\n" +
                "    \"error\": {\n" +
                "        \"httpStatus\": \"UNAUTHORIZED\",\n" +
                "        \"httpStatusCode\": 401,\n" +
                "        \"reason\": \"denied by provider GOOGLE\",\n" +
                "        \"systemError\": \"AUTHENTICATION\",\n" +
                "        \"systemErrorCode\": \"400\"\n" +
                "    }\n" +
                "}", jsonResponse);
    }

    @Test
    public void testAuthenticateUserCredentialPass() throws Exception {
        String json = createJson(ProviderEnum.GOOGLE.name(), "1234");
        when(socialAuthenticationService.authenticateWeb(anyString(), anyString(), any(HttpClient.class)))
                .thenReturn(
                        "{\"" + SocialAuthenticationController.AUTH + "\":\"123\",\"" +
                                SocialAuthenticationController.MAIL + "\":\"test@receiptofi.com\"}");
        String jsonResponse = socialAuthenticationController.authenticateUser(json, response);

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);

        assertEquals("123", jo.get(SocialAuthenticationController.AUTH).getAsString());
        assertEquals("test@receiptofi.com", jo.get(SocialAuthenticationController.MAIL).getAsString());
    }

    private String createJson(String pid, String accessToken) {
        JsonObject json = new JsonObject();
        json.addProperty("pid", pid);
        json.addProperty("at", accessToken);

        return new Gson().toJson(json);
    }
}
