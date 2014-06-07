package com.receiptofi.web.helper;

import com.receiptofi.domain.DocumentEntity;

/**
 * User: hitender
 * Date: 7/2/13
 * Time: 1:55 AM
 */
public final class ReceiptOCRHelper {
    private String fileName;
    private long fileSize;
    private DocumentEntity documentEntity;

    private ReceiptOCRHelper() {}

    public static ReceiptOCRHelper newInstance(String fileName, long fileSize, DocumentEntity documentEntity) {
        ReceiptOCRHelper receiptOCRHelper = new ReceiptOCRHelper();
        receiptOCRHelper.setFileName(fileName);
        receiptOCRHelper.setFileSize(fileSize);
        receiptOCRHelper.setDocumentEntity(documentEntity);
        return receiptOCRHelper;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public DocumentEntity getDocumentEntity() {
        return documentEntity;
    }

    public void setDocumentEntity(DocumentEntity documentEntity) {
        this.documentEntity = documentEntity;
    }
}
