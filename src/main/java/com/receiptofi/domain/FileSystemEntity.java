package com.receiptofi.domain;

import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.beans.Transient;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * User: hitender
 * Date: 12/13/13 12:47 AM
 */
@Document(collection = "FILE_SYSTEM")
public final class FileSystemEntity extends BaseEntity {

    @NotNull
    @Field("BLOB_ID")
    private String blobId;

    @NotNull
    @Field("H")
    private int height;

    @NotNull
    @Field("W")
    private int width;

    @NotNull
    @Field("ORIENTATION")
    private int imageOrientation = 0;

    @NotNull
    @Field("SEQUENCE")
    private int sequence;

    /** To keep bean happy */
    public FileSystemEntity() {}

    public FileSystemEntity(String blobId, BufferedImage bufferedImage, int imageOrientation, int sequence) {
        this.blobId = blobId;
        this.height = bufferedImage.getHeight();
        this.width = bufferedImage.getWidth();
        this.imageOrientation = imageOrientation;
        this.sequence = sequence;
    }

    public String getBlobId() {
        return blobId;
    }

    public void setBlobId(String blobId) {
        this.blobId = blobId;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getImageOrientation() {
        return imageOrientation;
    }

    public void setImageOrientation(int imageOrientation) {
        this.imageOrientation = imageOrientation;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Transient
    public void switchHeightAndWidth() {
        int tempHeight = this.height;
        this.height = this.width;
        this.width = tempHeight;
    }
}
