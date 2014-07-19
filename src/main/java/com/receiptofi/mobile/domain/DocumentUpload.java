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
 * Date: 7/13/14 9:58 PM
 */
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentUpload {

    @SuppressWarnings("unused")
    @JsonProperty("uploadedDocumentName")
    private String uploadedDocumentName;

    @SuppressWarnings("unused")
    @JsonProperty("unprocessedDocuments")
    private UnprocessedDocuments unprocessedDocuments;

    private DocumentUpload(String uploadedDocumentName, long numberOfUnprocessedFiles) {
        this.uploadedDocumentName = uploadedDocumentName;
        this.unprocessedDocuments = UnprocessedDocuments.newInstance(numberOfUnprocessedFiles);
    }

    public static DocumentUpload newInstance(String uploadedDocumentName, long numberOfUnprocessedFiles) {
        return new DocumentUpload(uploadedDocumentName, numberOfUnprocessedFiles);
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