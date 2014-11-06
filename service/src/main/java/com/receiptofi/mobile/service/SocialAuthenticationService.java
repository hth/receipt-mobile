package com.receiptofi.mobile.service;

import java.io.IOException;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.*;

import com.google.gson.Gson;

import com.receiptofi.mobile.domain.ProviderAndAccessToken;
import com.receiptofi.mobile.util.ErrorEncounteredJson;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * User: hitender
 * Date: 6/27/14 1:00 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal"
})
@Component
public class SocialAuthenticationService {
    private static final Logger LOG = LoggerFactory.getLogger(SocialAuthenticationService.class);

    public static final int HTTP_STATUS_200 = 200;
    public static final int HTTP_STATUS_300 = 300;
    public static final int MAX_RESPONSE_SIZE = 2048;
    public static final int MIN_RESPONSE_SIZE = -1;

    @Value ("${api.mobile.get:/webapi/mobile/get.htm}")
    private String apiMobileGetPath;

    @Value ("${web.access.api.token}")
    private String webApiAccessToken;

    @Value ("${secure.port}")
    private String securePort;

    @Value ("${https}")
    private String protocol;

    @Value ("${host}")
    private String host;

    @Value ("${auth.create:/webapi/mobile/auth-create.htm}")
    private String authCreate;

    @Value ("${no.response.from.web.server:could not connect to server}")
    private String noResponseFromWebServer;

    private HttpClient httpClient;

    /**
     * Call this on terminal as below.
     * http localhost:9090/receipt-mobile/authenticate.json < ~/Downloads/pid.json
     *
     * @param providerId
     * @param accessToken
     * @return
     */
    public String authenticateWeb(String providerId, String accessToken) {
        LOG.debug("providerId={} accessToken={} webApiAccessToken={}", providerId, "*******", "*******");
        httpClient = HttpClientBuilder.create().build();

        Header header = getCSRFToken(webApiAccessToken);
        LOG.debug("CSRF received from Web header={}", header);
        if (header == null) {
            return ErrorEncounteredJson.toJson(noResponseFromWebServer, SEVERE);
        }

        LOG.info("calling external URL={}", protocol + "://" + host + computePort() + authCreate);
        HttpPost httpPost = new HttpPost(protocol + "://" + host + computePort() + authCreate);
        LOG.info("complete external call for URI={} webApiAccessToken={}",
                httpPost.getURI().toString(), webApiAccessToken);
        httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-R-API-MOBILE", webApiAccessToken);
        httpPost.addHeader(header);

        populateEntity(providerId, accessToken, httpPost);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            LOG.error("error occurred while executing request path={} reason={}",
                    httpPost.getURI(), e.getLocalizedMessage(), e);
        }

        if (response == null) {
            return ErrorEncounteredJson.toJson(noResponseFromWebServer, SEVERE);
        }

        int status = response.getStatusLine().getStatusCode();
        LOG.debug("status={}", status);
        if (status >= HTTP_STATUS_200 && status < HTTP_STATUS_300) {
            return responseString(response.getEntity());
        }

        LOG.error("server responded with response code={}", status);
        return ErrorEncounteredJson.toJson("not a valid status from server", SEVERE);
    }

    /**
     * Returns response.
     * @param entity
     * @return
     */
    private String responseString(HttpEntity entity) {
        if (entity != null) {
            long len = entity.getContentLength();
            LOG.debug("response length={}", len);
            if (len != MIN_RESPONSE_SIZE && len < MAX_RESPONSE_SIZE) {
                try {
                    String data = EntityUtils.toString(entity);
                    LOG.debug("data={}", data);
                    return data;
                } catch (IOException e) {
                    LOG.error("error occurred while parsing entity reason={}", e.getLocalizedMessage(), e);
                }
            }
        }

        /** Stream too big */
        LOG.warn("stream size bigger than {}", MAX_RESPONSE_SIZE);
        return ErrorEncounteredJson.toJson("stream size bigger than " + MAX_RESPONSE_SIZE, SEVERE);
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
     * Test via terminal
     * http --verbose
     * localhost:8080/receipt/api/mobile/auth-create.htm Accept:application/json
     * X-R-API-MOBILE:1234567890
     * X-CSRF-TOKEN:9673034a-3791-40e4-abf0-3e2f9e2fb028
     *
     * @param webApiAccessToken
     * @return
     */
    private Header getCSRFToken(String webApiAccessToken) {
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
