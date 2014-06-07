package com.receiptofi.repository;

import com.receiptofi.domain.MessageDocumentEntity;
import com.receiptofi.domain.types.DocumentStatusEnum;

import java.util.List;

import com.mongodb.WriteResult;

/**
 * JMS Message Manager
 *
 * User: hitender
 * Date: 4/6/13
 * Time: 7:28 PM
 */
public interface MessageManager extends RepositoryManager<MessageDocumentEntity> {

    List<MessageDocumentEntity> findWithLimit(DocumentStatusEnum status);

    List<MessageDocumentEntity> findWithLimit(DocumentStatusEnum status, int limit);

    List<MessageDocumentEntity> findUpdateWithLimit(String emailId, String userProfileId, DocumentStatusEnum status);

    List<MessageDocumentEntity> findUpdateWithLimit(String emailId, String userProfileId, DocumentStatusEnum status, int limit);

    List<MessageDocumentEntity> findAllPending();

    List<MessageDocumentEntity> findPending(String emailId, String userProfileId, DocumentStatusEnum status);

    WriteResult updateObject(String receiptOCRId, DocumentStatusEnum statusFind, DocumentStatusEnum statusSet);

    /**
     * On failure the status is reverted back to OCR_PROCESSED. For now the record is kept locked for the same user.
     * Note: User has to complete all the messages in their queue before logging out of their shift.
     *
     * TODO: May be change the parameters in the future by dropping 'value' parameters as this is currently being defaulted as false in the query
     *
     * @param receiptOCRId
     * @param value
     * @return
     */
    WriteResult undoUpdateObject(String receiptOCRId, boolean value, DocumentStatusEnum statusFind, DocumentStatusEnum statusSet);

    /**
     * Delete all the messages that are associated with DocumentEntity.
     * Process will include current and previous re-check request messages for the receipt
     *
     * @param receiptOCRId
     */
    void deleteAllForReceiptOCR(String receiptOCRId);
}
