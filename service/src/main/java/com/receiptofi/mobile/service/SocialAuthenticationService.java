package com.receiptofi.mobile.service;

import java.io.IOException;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.SEVERE;

import com.google.gson.Gson;

import com.receiptofi.mobile.domain.ProviderAndAccessToken;
import com.receiptofi.mobile.util.ErrorEncounteredJson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
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
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Component
public class SocialAuthenticationService {
    private static final Logger LOG = LoggerFactory.getLogger(SocialAuthenticationService.class);

    public static final int MAX_RESPONSE_SIZE = 2048;
    public static final int MIN_RESPONSE_SIZE = -1;

    @Value ("${auth.create:/webapi/mobile/auth-create.htm}")
    private String authCreate;

    @Value ("${no.response.from.web.server:could not connect to server}")
    private String noResponseFromWebServer;

    @Autowired private WebConnectorService webConnectorService;

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
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = webConnectorService.getHttpPost(authCreate, httpClient);
        if (httpPost == null) {
            return ErrorEncounteredJson.toJson(noResponseFromWebServer, SEVERE);
        }

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
        if (status >= WebConnectorService.HTTP_STATUS_200 && status < WebConnectorService.HTTP_STATUS_300) {
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
}
