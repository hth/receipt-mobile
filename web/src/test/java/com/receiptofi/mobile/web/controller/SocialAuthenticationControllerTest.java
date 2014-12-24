package com.receiptofi.mobile.web.controller;

import static com.receiptofi.mobile.web.controller.AccountControllerTest.ERROR;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.REASON;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.SYSTEM_ERROR;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.SYSTEM_ERROR_CODE;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletResponse;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
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
                .authenticateWeb(anyString(), anyString(), any(HttpClient.class));

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);

        assertEquals("Internal error, please try some time later.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("SEVERE", jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("500", jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
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

        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);
        assertEquals("denied by provider GOOGLE", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("AUTHENTICATION", jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("400", jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
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
