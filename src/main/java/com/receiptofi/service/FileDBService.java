package com.receiptofi.service;

import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.shared.UploadReceiptImage;
import com.receiptofi.repository.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * User: hitender
 * Date: 4/27/13
 * Time: 12:59 PM
 */
@Service
public final class FileDBService {
    private static final Logger log = LoggerFactory.getLogger(FileDBService.class);

    @Autowired private StorageManager storageManager;

    /**
     * Load file from database
     *
     * @param fileId
     * @return
     */
    public GridFSDBFile getFile(String fileId) {
        return storageManager.get(fileId);
    }

    public int getFSDBSize() {
        return storageManager.getSize();
    }

    public String saveFile(UploadReceiptImage uploadReceiptImage) throws IOException {
        return storageManager.saveFile(uploadReceiptImage);
    }

    public void deleteHard(String fileId) {
        storageManager.deleteHard(fileId);
    }

    public void deleteHard(Collection<FileSystemEntity> fileId) {
        storageManager.deleteHard(fileId);
    }
}