package com.receiptofi.mobile.service;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Helper class to create connection to Web Application for provided endpoint.
 *
 * User: hitender
 * Date: 11/8/14 12:32 AM
 */
@Component
public class WebConnectorService {
    private static final Logger LOG = LoggerFactory.getLogger(WebConnectorService.class);

    public static final int HTTP_STATUS_200 = 200;
    public static final int HTTP_STATUS_300 = 300;

    @Value ("${api.mobile.get:/webapi/mobile/get.htm}")
    private String apiMobileGetPath;

    @Value ("${no.response.from.web.server:could not connect to server}")
    private String noResponseFromWebServer;

    @Value ("${web.access.api.token}")
    private String webApiAccessToken;

    @Value ("${secure.port}")
    private String securePort;

    @Value ("${https}")
    private String protocol;

    @Value ("${host}")
    private String host;

    /**
     * Creates Http Post for supplied endpoint.
     * @param endPoint
     * @param httpClient
     * @return
     */
    protected HttpPost getHttpPost(String endPoint, HttpClient httpClient) {
        Header header = getCSRFToken(webApiAccessToken, httpClient);
        LOG.debug("CSRF received from Web header={}", header);
        if (header == null) {
            return null;
        }

        LOG.info("calling external URL={}", protocol + "://" + host + computePort() + endPoint);
        HttpPost httpPost = new HttpPost(protocol + "://" + host + computePort() + endPoint);
        LOG.info("external call complete URI={} webApiAccessToken={}", httpPost.getURI().toString(), webApiAccessToken);

        httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-R-API-MOBILE", webApiAccessToken);
        httpPost.addHeader(header);
        return httpPost;
    }

    /**
     * Used in populating request and setting CSRF. Without this you get forbidden exception.
     * Test via terminal
     * http --verbose
     * localhost:8080/receipt/api/mobile/auth-create.htm Accept:application/json
     * X-R-API-MOBILE:1234567890
     * X-CSRF-TOKEN:9673034a-3791-40e4-abf0-3e2f9e2fb028
     *
     * @param webApiAccessToken
     * @return
     */
    private Header getCSRFToken(String webApiAccessToken, HttpClient httpClient) {
        LOG.info("CSRF for mobile external call URL={}", protocol + "://" + host + computePort() + apiMobileGetPath);
        HttpGet httpGet = new HttpGet(protocol + "://" + host + computePort() + apiMobileGetPath);
        httpGet.setHeader("X-R-API-MOBILE", webApiAccessToken);
        httpGet.setHeader("Accepts", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        HttpResponse response;
        try {
            response = httpClient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status >= HTTP_STATUS_200 && status < HTTP_STATUS_300) {
                return response.getFirstHeader("X-CSRF-TOKEN");
            }
            LOG.warn("could not make successful call to path={} status={}", apiMobileGetPath, status);
            return null;
        } catch (IOException e) {
            LOG.error("{} reason={}", noResponseFromWebServer, e.getLocalizedMessage(), e);
        }
        return null;
    }

    private String computePort() {
        if ("443".equals(securePort) && "https".equals(protocol)) {
            return "";
        }
        return StringUtils.isEmpty(securePort) ? "" : ":" + securePort;
    }
}
