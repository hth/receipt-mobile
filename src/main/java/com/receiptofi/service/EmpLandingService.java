package com.receiptofi.service;

import com.receiptofi.domain.MessageDocumentEntity;
import com.receiptofi.domain.types.DocumentStatusEnum;
import com.receiptofi.repository.MessageManager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 4/28/13
 * Time: 8:12 PM
 */
@Service
public final class EmpLandingService {

    @Autowired private MessageManager messageManager;

    public List<MessageDocumentEntity> pendingReceipts(String emailId, String profileId, DocumentStatusEnum status) {
        return messageManager.findPending(emailId, profileId, status);
    }

    public List<MessageDocumentEntity> queuedReceipts(String emailId, String profileId) {
        return messageManager.findUpdateWithLimit(emailId, profileId, DocumentStatusEnum.OCR_PROCESSED);
    }

    public List<MessageDocumentEntity> recheck(String emailId, String profileId) {
        return messageManager.findUpdateWithLimit(emailId, profileId, DocumentStatusEnum.TURK_REQUEST);
    }

    public List<MessageDocumentEntity> findAll() {
        return messageManager.getAllObjects();
    }

    public void delete(MessageDocumentEntity messageDocumentEntity) {
        messageManager.deleteHard(messageDocumentEntity);
    }
}
