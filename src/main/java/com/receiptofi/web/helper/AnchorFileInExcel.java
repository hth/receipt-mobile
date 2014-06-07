package com.receiptofi.web.helper;

/**
 * User: hitender
 * Date: 12/24/13 10:01 PM
 */
public final class AnchorFileInExcel {

    private byte[] bytes;
    private String contentType;

    public AnchorFileInExcel(byte[] bytes, String contentType) {
        this.bytes = bytes;
        this.contentType = contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getContentType() {
        return contentType;
    }
}
