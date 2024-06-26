package com.receiptofi.mobile.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicStatusLine;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class WebConnectorServiceTest {
    public static final int HTTP_CODE_ERROR = 501;
    public static final int HTTP_CODE_SUCCESS = 201;

    private WebConnectorService webConnectorService;

    @Mock private HttpClient httpClient;
    private HttpResponse response;
    private BasicStatusLine basicStatusLine;
    private Header header;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        response = mock(HttpResponse.class);
        basicStatusLine = mock(BasicStatusLine.class);
        header = mock(Header.class);
        this.webConnectorService = new WebConnectorService(
                "/webapi/mobile/get.htm",
                "could not connect to server",
                "1234567890",
                "8080",
                "http",
                "localhost");
    }

    @Test
    public void testGetHttpPostGetCSRFTokenIOException() throws IOException {
        doThrow(new IOException()).when(httpClient).execute(any(HttpGet.class));
        assertNull(webConnectorService.getHttpPost("/someLink", httpClient));
    }

    @Test
    public void testGetHttpPostGetCSRFTokenStatus501() throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(basicStatusLine);
        when(response.getStatusLine().getStatusCode()).thenReturn(HTTP_CODE_ERROR);
        assertNull(webConnectorService.getHttpPost("/someLink", httpClient));
    }

    @Test
    public void testGetHttpPostGetCSRFTokenHeaderNull() throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(basicStatusLine);
        when(response.getStatusLine().getStatusCode()).thenReturn(HTTP_CODE_SUCCESS);
        when(response.getFirstHeader(anyString())).thenReturn(null);
        assertNull(webConnectorService.getHttpPost("/someLink", httpClient));
    }

    @Test
    public void testGetHttpPostPass() throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(basicStatusLine);
        when(response.getStatusLine().getStatusCode()).thenReturn(HTTP_CODE_SUCCESS);
        when(response.getFirstHeader(anyString())).thenReturn(header);
        HttpPost httpPost = webConnectorService.getHttpPost("/someLink", httpClient);
        assertNotNull(httpPost);
    }

    @Test
    public void testGetHttpPostPassWithSecureProtocol() throws IOException {
        this.webConnectorService = new WebConnectorService(
                "/webapi/mobile/get.htm",
                "could not connect to server",
                "1234567890",
                "443",
                "https",
                "localhost");

        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(basicStatusLine);
        when(response.getStatusLine().getStatusCode()).thenReturn(HTTP_CODE_SUCCESS);
        when(response.getFirstHeader(anyString())).thenReturn(header);
        HttpPost httpPost = webConnectorService.getHttpPost("/someLink", httpClient);
        assertNotNull(httpPost);
    }
}
