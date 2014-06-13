package com.receiptofi.web.helper.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;

/**
 * User: hitender
 * Date: 1/19/14 2:05 AM
 */
public class MileageDateUpdateResponse {

    @JsonProperty("s")
    private boolean success = false;

    @JsonProperty("d")
    private String days;

    @JsonProperty("m")
    private String message = StringUtils.EMPTY;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    //Converts this object to JSON representation
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