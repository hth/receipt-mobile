package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 7/13/14 9:58 PM
 */
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentUpload extends AbstractDomain {

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
}