package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Details of document uploaded.
 * User: hitender
 * Date: 7/13/14 9:58 PM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class DocumentUpload extends AbstractDomain {

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("uploadedDocumentName")
    private String uploadedDocumentName;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("blobId")
    private String blobId;

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("unprocessedDocuments")
    private UnprocessedDocuments unprocessedDocuments;

    private DocumentUpload(String uploadedDocumentName, String blobId, long numberOfUnprocessedFiles) {
        this.uploadedDocumentName = uploadedDocumentName;
        this.blobId = blobId;
        this.unprocessedDocuments = UnprocessedDocuments.newInstance(numberOfUnprocessedFiles);
    }

    public static DocumentUpload newInstance(
            String uploadedDocumentName,
            String blobId,
            long numberOfUnprocessedFiles
    ) {
        return new DocumentUpload(uploadedDocumentName, blobId, numberOfUnprocessedFiles);
    }
}
