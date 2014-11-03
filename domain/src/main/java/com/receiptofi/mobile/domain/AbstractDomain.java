package com.receiptofi.mobile.domain;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts domain object to string.
 * User: hitender
 * Date: 7/19/14 12:42 AM
 */
public abstract class AbstractDomain {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDomain.class);

    /**
     * This adds tons of accept-charset.
     * Converts this object to JSON representation;
     * do not use annotation as this breaks and content length is set to -1 */
    @Deprecated
    public String asJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Writer writer = new StringWriter();
            mapper.writeValue(writer, this);
            return writer.toString();
        } catch (IOException e) {
            LOG.error("transforming object error={}", e.getLocalizedMessage(), e);
            return "{}";
        }
    }
}
