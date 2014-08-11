package com.receiptofi.mobile.domain;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * User: hitender
 * Date: 7/19/14 12:42 AM
 */
public abstract class AbstractDomain {
    private static final Logger log = LoggerFactory.getLogger(AbstractDomain.class);

    /** Converts this object to JSON representation; do not use annotation as this breaks and content length is set to -1 */
    public String asJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Writer writer = new StringWriter();
            mapper.writeValue(writer, this);
            return writer.toString();
        } catch (IOException e) {
            log.error("transforming object error={}", e.getLocalizedMessage(), e);
            return "{}";
        }
    }
}