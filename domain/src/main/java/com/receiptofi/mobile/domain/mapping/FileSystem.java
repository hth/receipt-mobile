package com.receiptofi.mobile.domain.mapping;

import com.receiptofi.domain.FileSystemEntity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 8/24/14 11:27 PM
 */
@JsonAutoDetect (
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
public final class FileSystem {

    @SuppressWarnings("unused")
    @JsonProperty ("blobId")
    private String blobId;

    @SuppressWarnings("unused")
    @JsonProperty ("orientation")
    private int imageOrientation = 0;

    @SuppressWarnings("unused")
    @JsonProperty ("sequence")
    private int sequence;

    private FileSystem(String blobId, int imageOrientation, int sequence) {
        this.blobId = blobId;
        this.imageOrientation = imageOrientation;
        this.sequence = sequence;
    }

    public static FileSystem newInstance(FileSystemEntity fileSystemEntity) {
        return new FileSystem(
                fileSystemEntity.getBlobId(),
                fileSystemEntity.getImageOrientation(),
                fileSystemEntity.getSequence()
        );
    }
}
