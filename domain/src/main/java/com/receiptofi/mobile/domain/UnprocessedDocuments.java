package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Shows count of unprocessed files.
 * User: hitender
 * Date: 7/18/14 8:13 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class UnprocessedDocuments {

    @SuppressWarnings ({"unused"})
    @JsonProperty ("unprocessedCount")
    private long numberOfUnprocessedFiles;

    private UnprocessedDocuments(long numberOfUnprocessedFiles) {
        this.numberOfUnprocessedFiles = numberOfUnprocessedFiles;
    }

    public static UnprocessedDocuments newInstance(long numberOfUnprocessedFiles) {
        return new UnprocessedDocuments(numberOfUnprocessedFiles);
    }

    public long getNumberOfUnprocessedFiles() {
        return numberOfUnprocessedFiles;
    }

    @Override
    public String toString() {
        return "UnprocessedDocuments{" +
                "numberOfUnprocessedFiles=" + numberOfUnprocessedFiles +
                '}';
    }
}
