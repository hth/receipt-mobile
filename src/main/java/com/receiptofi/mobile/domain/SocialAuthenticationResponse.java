package com.receiptofi.mobile.domain;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class does not have jackson annotation as the content length always is -1.
 * Without annotation seems to be working fine
 *
 * User: hitender
 * Date: 6/27/14 1:55 AM
 */
public final class SocialAuthenticationResponse {
    @JsonProperty("mail")
    private String userId;

    @JsonProperty("auth")
    private String authorizationCode;

    @JsonProperty("message")
    private String message;

    private SocialAuthenticationResponse() {
    }

    public static SocialAuthenticationResponse newInstance() {
        return new SocialAuthenticationResponse();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Converts this object to JSON representation; do not use annotation as this breaks and content length is set to -1
     */
    public String asJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Writer writer = new StringWriter();
            mapper.writeValue(writer, this);
            return writer.toString();
        } catch (IOException e) {
            return "{}";
        }
    }
}
