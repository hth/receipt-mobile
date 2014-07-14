package com.receiptofi.mobile.domain;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * User: hitender
 * Date: 7/13/14 9:58 PM
 */
public final class DocumentUpload {

    @JsonProperty("document")
    private String documentName;

    private DocumentUpload(String documentName) {
        this.documentName = documentName;
    }

    public static DocumentUpload newInstance(String documentName) {
        return new DocumentUpload(documentName);
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