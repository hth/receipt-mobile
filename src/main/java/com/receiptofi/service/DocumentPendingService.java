package com.receiptofi.service;

import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.repository.DocumentManager;
import com.receiptofi.web.form.PendingReceiptForm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * User: hitender
 * Date: 4/27/13
 * Time: 4:37 AM
 */
@Service
public final class DocumentPendingService {

    @Autowired private DocumentManager documentManager;
    @Autowired private FileDBService fileDBService;

    /**
     * All pending receipt for a user
     *
     * @param userProfileId
     * @return
     */
    public void getAllPending(String userProfileId, PendingReceiptForm pendingReceiptForm) {
        List<DocumentEntity> documentEntityList = documentManager.getAllPending(userProfileId);
        for(DocumentEntity documentEntity : documentEntityList) {
            for(FileSystemEntity scaledId : documentEntity.getFileSystemEntities()) {
                GridFSDBFile gridFSDBFile = fileDBService.getFile(scaledId.getBlobId());
                String originalFileName = (String) gridFSDBFile.getMetaData().get("ORIGINAL_FILENAME");
                pendingReceiptForm.addPending(originalFileName, gridFSDBFile.getLength(), documentEntity);
            }
        }
    }

    /**
     * All pending receipt for a user
     *
     * @param userProfileId
     * @return
     */
    public void getAllRejected(String userProfileId, PendingReceiptForm pendingReceiptForm) {
        List<DocumentEntity> documentEntityList = documentManager.getAllRejected(userProfileId);
        for(DocumentEntity documentEntity : documentEntityList) {
            for(FileSystemEntity scaledId : documentEntity.getFileSystemEntities()) {
                GridFSDBFile gridFSDBFile = fileDBService.getFile(scaledId.getBlobId());
                String originalFileName = (String) gridFSDBFile.getMetaData().get("ORIGINAL_FILENAME");
                pendingReceiptForm.addRejected(originalFileName, gridFSDBFile.getLength(), documentEntity);
            }
        }
    }
}
