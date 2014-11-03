package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Shows count of unprocessed files.
 * User: hitender
 * Date: 7/18/14 8:13 PM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public final class UnprocessedDocuments {

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("unprocessedCount")
    private long numberOfUnprocessedFiles;

    private UnprocessedDocuments(long numberOfUnprocessedFiles) {
        this.numberOfUnprocessedFiles = numberOfUnprocessedFiles;
    }

    public static UnprocessedDocuments newInstance(long numberOfUnprocessedFiles) {
        return new UnprocessedDocuments(numberOfUnprocessedFiles);
    }
}
