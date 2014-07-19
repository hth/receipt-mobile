package com.receiptofi.mobile.domain;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * User: hitender
 * Date: 7/18/14 8:13 PM
 */
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class UnprocessedDocuments {

    @SuppressWarnings("unused")
    @JsonProperty("unprocessedCount")
    private long numberOfUnprocessedFiles;

    private UnprocessedDocuments(long numberOfUnprocessedFiles) {
        this.numberOfUnprocessedFiles = numberOfUnprocessedFiles;
    }

    public static UnprocessedDocuments newInstance(long numberOfUnprocessedFiles) {
        return new UnprocessedDocuments(numberOfUnprocessedFiles);
    }

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
