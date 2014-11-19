package com.receiptofi.mobile.service;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicStatusLine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

@RunWith (MockitoJUnitRunner.class)
public class SocialAuthenticationServiceTest {

    public static final String SYSTEM_ERROR_CODE = "systemErrorCode";
    public static final String SYSTEM_ERROR = "systemError";
    public static final String REASON = "reason";
    public static final String ERROR = "error";

    @Mock private WebConnectorService webConnectorService;
    private SocialAuthenticationService socialAuthenticationService;

    private HttpPost httpPost;
    private HttpClient httpClient;
    private HttpResponse httpResponse;
    private BasicStatusLine basicStatusLine;
    private HttpEntity httpEntity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        httpPost = mock(HttpPost.class);
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        basicStatusLine = mock(BasicStatusLine.class);
        httpEntity = mock(HttpEntity.class);
        socialAuthenticationService = new SocialAuthenticationService(webConnectorService);
    }

    @Test
    public void testAuthenticateWebHttpPostNull() {
        when(webConnectorService.getHttpPost(anyString(), any(HttpClient.class))).thenReturn(null);
        when(webConnectorService.getNoResponseFromWebServer()).thenReturn("could not connect to server");
        String jsonResponse = socialAuthenticationService.authenticateWeb("", "", httpClient);
        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);

        assertEquals(SEVERE.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(SEVERE.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("could not connect to server", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
    }

    @Test
    public void testAuthenticateWebHttpClientThrowsException() throws IOException {
        when(webConnectorService.getHttpPost(anyString(), any(HttpClient.class))).thenReturn(httpPost);
        doThrow(new IOException()).when(httpClient).execute(httpPost);
        when(webConnectorService.getNoResponseFromWebServer()).thenReturn("could not connect to server");

        String jsonResponse = socialAuthenticationService.authenticateWeb("", "", httpClient);
        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);

        assertEquals(SEVERE.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(SEVERE.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("could not connect to server", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
    }

    @Test
    public void testAuthenticateWebResponseNull() throws IOException {
        when(webConnectorService.getHttpPost(anyString(), any(HttpClient.class))).thenReturn(httpPost);
        when(httpClient.execute(httpPost)).thenReturn(null);
        when(webConnectorService.getNoResponseFromWebServer()).thenReturn("could not connect to server");

        String jsonResponse = socialAuthenticationService.authenticateWeb("", "", httpClient);
        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);

        assertEquals(SEVERE.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(SEVERE.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("could not connect to server", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
    }

    @Test
    public void testAuthenticateWebStatus501() throws IOException {
        when(webConnectorService.getHttpPost(anyString(), any(HttpClient.class))).thenReturn(httpPost);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(basicStatusLine);
        when(basicStatusLine.getStatusCode()).thenReturn(WebConnectorServiceTest.HTTP_CODE_ERROR);

        String jsonResponse = socialAuthenticationService.authenticateWeb("", "", httpClient);
        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);

        assertEquals(SEVERE.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(SEVERE.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("not a valid status from server", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
    }

    @Test
    public void testAuthenticateWebSuccess() throws IOException {
        when(webConnectorService.getHttpPost(anyString(), any(HttpClient.class))).thenReturn(httpPost);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(basicStatusLine);
        when(basicStatusLine.getStatusCode()).thenReturn(WebConnectorServiceTest.HTTP_CODE_SUCCESS);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContentLength()).thenReturn(10L);
        InputStream stubInputStream = IOUtils.toInputStream("{\"X-R-AUTH\" : \"123\", \"X-R-MAIL\" : \"t@t.com\"}");
        when(httpEntity.getContent()).thenReturn(stubInputStream);

        String jsonResponse = socialAuthenticationService.authenticateWeb("", "", httpClient);
        JsonObject jo = (JsonObject) new JsonParser().parse(jsonResponse);

        assertEquals("123", jo.get("X-R-AUTH").getAsString());
        assertEquals("t@t.com", jo.get("X-R-MAIL").getAsString());
    }
}
