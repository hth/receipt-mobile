package com.receiptofi.mobile.domain;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * User: hitender
 * Date: 7/19/14 12:42 AM
 */
public abstract class AbstractDomain {

    /** Converts this object to JSON representation; do not use annotation as this breaks and content length is set to -1 */
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