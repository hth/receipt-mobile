package com.receiptofi.mobile.service;

import com.receiptofi.mobile.domain.ProviderAndAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.gson.Gson;

/**
 * User: hitender
 * Date: 6/27/14 1:00 AM
 */
@Component
public class SocialAuthenticationService {
    private static Logger log = LoggerFactory.getLogger(SocialAuthenticationService.class);

    @Value("${api.mobile.get:/api/mobile/get.htm}")
    private String apiMobileGetPath;

    @Value("${web.access.api.token}")
    private String webApiAccessToken;

    @Value("${secure.port}")
    private String securePort;

    @Value("${https}")
    private String protocol;

    @Value("${host}")
    private String host;

    @Value("${auth.create:/api/mobile/auth-create.htm}")
    private String authCreate;

    private HttpClient httpClient;

    /**
     * call this on terminal
     * http localhost:9090/receipt-mobile/authenticate.json < ~/Downloads/pid.json
     *
     * @param providerId
     * @param accessToken
     * @return
     */
    public String authenticateWeb(String providerId, String accessToken) {
        log.info("providerId={} accessToken={} webApiAccessToken={}", providerId, accessToken, "*******");
        httpClient = HttpClientBuilder.create().build();

        Header header = getCSRFToken(webApiAccessToken);
        Assert.notNull(header);

        HttpPost httpPost = new HttpPost(protocol + "://" + host + computePort() + "/receipt" + authCreate);
        httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-R-API-MOBILE", webApiAccessToken);
        httpPost.addHeader(header);

        populateEntity(providerId, accessToken, httpPost);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            log.error("error occurred while executing request path={} reason={}", httpPost.getURI(), e.getLocalizedMessage(), e);
        }

        if(null != response) {
            int status = response.getStatusLine().getStatusCode();
            if(status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if(null != entity) {
                    long len = entity.getContentLength();
                    if(len != -1 && len < 2048) {
                        try {
                            return EntityUtils.toString(entity);
                        } catch (IOException e) {
                            log.error("error occurred while parsing entity reason={}", e.getLocalizedMessage(), e);
                        }
                    } else {
                        // Stream too big
                        log.warn("stream size bigger than 2048");
                        return "{}";
                    }
                }
            }
        }

        return "{}";
    }

    /**
     * Create Request Body with pid and at
     *
     * @param providerId
     * @param accessToken
     * @param httpPost
     */
    private void populateEntity(String providerId, String accessToken, HttpPost httpPost) {
        httpPost.setEntity(
                new StringEntity(
                        new Gson().toJson(ProviderAndAccessToken.newInstance(providerId, accessToken)),
                        ContentType.create(MediaType.APPLICATION_JSON_VALUE, "UTF-8")
                )
        );
    }

    /**
     * Used in populating request and setting CSRF. Without this you get forbidden exception.
     * call this on terminal
     * http --verbose localhost:8080/receipt/api/mobile/auth-create.htm Accept:application/json X-R-API-MOBILE:1234567890 X-CSRF-TOKEN:9673034a-3791-40e4-abf0-3e2f9e2fb028
     *
     * @param webApiAccessToken
     * @return
     */
    private Header getCSRFToken(String webApiAccessToken) {
        HttpGet httpGet = new HttpGet(protocol + "://" + host + computePort() + "/receipt" + apiMobileGetPath);
        httpGet.setHeader("X-R-API-MOBILE", webApiAccessToken);
        httpGet.setHeader("Accepts", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        HttpResponse response;
        try {
            response = httpClient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if(status >= 200 && status < 300) {
                return response.getFirstHeader("X-CSRF-TOKEN");
            }
            log.warn("could not make successful call to path={} status={}", apiMobileGetPath, status);
            return null;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    private String computePort() {
        return StringUtils.isEmpty(securePort) ? "" : (":" + securePort);
    }
}