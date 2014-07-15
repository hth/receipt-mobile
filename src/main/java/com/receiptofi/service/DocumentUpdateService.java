package com.receiptofi.service;

import com.receiptofi.domain.CommentEntity;
import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ItemEntityOCR;
import com.receiptofi.domain.MileageEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.types.DocumentStatusEnum;
import com.receiptofi.domain.types.NotificationTypeEnum;
import com.receiptofi.repository.CommentManager;
import com.receiptofi.repository.DocumentManager;
import com.receiptofi.repository.ItemManager;
import com.receiptofi.repository.ItemOCRManager;
import com.receiptofi.repository.MessageManager;
import com.receiptofi.repository.ReceiptManager;
import com.receiptofi.repository.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * User: hitender
 * Date: 4/25/13
 * Time: 2:09 PM
 */
@Service
public final class DocumentUpdateService {
    private static final Logger log = LoggerFactory.getLogger(DocumentUpdateService.class);

    @Autowired private DocumentManager documentManager;
    @Autowired private ItemOCRManager itemOCRManager;
    @Autowired private ReceiptManager receiptManager;
    @Autowired private ItemManager itemManager;
    @Autowired private MessageManager messageManager;
    @Autowired private BizService bizService;
    @Autowired private UserProfilePreferenceService userProfilePreferenceService;
    @Autowired private CommentManager commentManager;
    @Autowired private NotificationService notificationService;
    @Autowired private StorageManager storageManager;
    @Autowired private FileSystemService fileSystemService;
    @Autowired private MileageService mileageService;

    public DocumentEntity loadActiveDocumentById(String id) {
        return documentManager.findActiveOne(id);
    }

    public DocumentEntity loadRejectedDocumentById(String id) {
        return documentManager.findRejectedOne(id);
    }

    public DocumentEntity findOne(String documentId, String userProfileId) {
        return documentManager.findOne(documentId, userProfileId);
    }

    public List<ItemEntityOCR> loadItemsOfReceipt(DocumentEntity receiptEntity) {
        return itemOCRManager.getWhereReceipt(receiptEntity);
    }

    /**
     * This method is used when technician saves the processed receipt for the first time
     *
     * @param receipt
     * @param items
     * @param documentForm
     * @throws Exception
     */
    public void turkReceipt(ReceiptEntity receipt, List<ItemEntity> items, DocumentEntity documentForm) throws Exception {
        try {
            DocumentEntity documentEntity = loadActiveDocumentById(documentForm.getId());

            receipt.setFileSystemEntities(documentEntity.getFileSystemEntities());

            documentForm.setFileSystemEntities(documentEntity.getFileSystemEntities());

            //update the version number as the value could have changed by rotating receipt image through ajax
            documentForm.setVersion(documentEntity.getVersion());

            bizService.saveNewBusinessAndOrStore(receipt);
            receiptManager.save(receipt);

            populateItemsWithBizName(items, receipt);
            itemManager.saveObjects(items);

            bizService.saveNewBusinessAndOrStore(documentForm);
            documentForm.setDocumentStatus(DocumentStatusEnum.TURK_PROCESSED);
            documentForm.setReceiptId(receipt.getId());
            documentForm.inActive();
            documentManager.save(documentForm);

            updateMessageManager(documentForm, DocumentStatusEnum.OCR_PROCESSED, DocumentStatusEnum.TURK_PROCESSED);

            StringBuilder sb = new StringBuilder();
            sb.append(receipt.getTotalString());
            sb.append(" '").append(receipt.getBizName().getBusinessName()).append("' ");
            sb.append("receipt processed");
            notificationService.addNotification(sb.toString(), NotificationTypeEnum.RECEIPT, receipt);

        } catch(Exception exce) {
            log.error("Revert all the transaction for Receipt={}, ReceiptOCR={}, reason={}", receipt.getId(), documentForm.getId(), exce.getLocalizedMessage(), exce);

            //For rollback
            if(StringUtils.isNotEmpty(receipt.getId())) {
                long sizeReceiptInitial = receiptManager.collectionSize();
                long sizeItemInitial = itemManager.collectionSize();

                itemManager.deleteWhereReceipt(receipt);
                receiptManager.deleteHard(receipt);

                long sizeReceiptFinal = receiptManager.collectionSize();
                long sizeItemFinal = itemManager.collectionSize();
                if(sizeReceiptInitial == sizeReceiptFinal) {
                    log.warn("Initial receipt size and Final receipt size are same={}:{}", sizeReceiptInitial, sizeReceiptFinal);
                } else {
                    log.warn("Initial receipt size={}, Final receipt size={}. Removed Receipt={}", sizeReceiptInitial, sizeReceiptFinal, receipt.getId());
                }

                if(sizeItemInitial == sizeItemFinal) {
                    log.warn("Initial item size and Final item size are same={}:{}", sizeItemInitial, sizeItemFinal);
                } else {
                    log.warn("Initial item size={}, Final item size={}", sizeItemInitial, sizeItemFinal);
                }

                documentForm.setDocumentStatus(DocumentStatusEnum.OCR_PROCESSED);
                documentManager.save(documentForm);
                //log.error("Failed to rollback Document: " + documentForm.getId() + ", error message: " + e.getLocalizedMessage());

                messageManager.undoUpdateObject(documentForm.getId(), false, DocumentStatusEnum.TURK_PROCESSED, DocumentStatusEnum.OCR_PROCESSED);
                //End of roll back

                log.info("Complete with rollback: throwing exception");
            }
            throw new Exception(exce.getLocalizedMessage());
        }
    }

    /**
     * This method is executed when Technician is re-checking the receipt
     *
     * @param receipt
     * @param items
     * @param receiptDocument
     * @throws Exception
     */
    public void turkReceiptReCheck(ReceiptEntity receipt, List<ItemEntity> items, DocumentEntity receiptDocument) throws Exception {
        ReceiptEntity fetchedReceipt = null;
        try {
            DocumentEntity documentEntity = loadActiveDocumentById(receiptDocument.getId());

            receipt.setFileSystemEntities(documentEntity.getFileSystemEntities());

            receiptDocument.setFileSystemEntities(documentEntity.getFileSystemEntities());

            //update the version number as the value could have changed by rotating receipt image through ajax
            receiptDocument.setVersion(documentEntity.getVersion());

            bizService.saveNewBusinessAndOrStore(receipt);
            if(StringUtils.isNotEmpty(receipt.getId())) {
                fetchedReceipt = receiptManager.findOne(receipt.getId());
                if(fetchedReceipt == null) {
                    // By creating new receipt with old id, we move the pending receipt from the list back to users account
                    log.warn("Something had gone wrong with original Receipt={}, so creating another with old receipt id", receipt.getId());
                } else {
                    receipt.setVersion(fetchedReceipt.getVersion());
                    receipt.setCreated(fetchedReceipt.getCreated());
                }
            }

            populateItemsWithBizName(items, receipt);
            itemManager.saveObjects(items);

            bizService.saveNewBusinessAndOrStore(receiptDocument);
            receiptDocument.setDocumentStatus(DocumentStatusEnum.TURK_PROCESSED);
            receiptDocument.inActive();

            //Only recheck comments are updated by technician. Receipt notes are never modified
            if(StringUtils.isEmpty(receiptDocument.getRecheckComment().getText())) {
                CommentEntity comment = receiptDocument.getRecheckComment();
                commentManager.deleteHard(comment);
                receiptDocument.setRecheckComment(null);
                receipt.setRecheckComment(null);
            } else {
                CommentEntity comment = receiptDocument.getRecheckComment();
                if(StringUtils.isEmpty(comment.getId())) {
                    comment.setId(null);
                }

                /**
                 * If the comment is not equal then it means Technician has modified the comment and this needs
                 * to be updated with new time. Else do not update the time of recheck comment
                 */
                String fetchedRecheckComment = StringUtils.EMPTY;
                if(fetchedReceipt != null && fetchedReceipt.getRecheckComment() != null) {
                    fetchedRecheckComment = fetchedReceipt.getRecheckComment().getText();
                }
                if(!comment.getText().equalsIgnoreCase(fetchedRecheckComment)) {
                    comment.setUpdated();
                    commentManager.save(comment);
                }
                receiptDocument.setRecheckComment(comment);
                receipt.setRecheckComment(comment);
            }

            //Since Technician cannot change notes at least we gotta make sure we are not adding one when the Id for notes are missing
            if(StringUtils.isEmpty(receiptDocument.getNotes().getId())) {
                receiptDocument.setNotes(null);
                receipt.setNotes(null);
            }

            receiptManager.save(receipt);
            documentManager.save(receiptDocument);

            updateMessageManager(receiptDocument, DocumentStatusEnum.TURK_REQUEST, DocumentStatusEnum.TURK_PROCESSED);

            StringBuilder sb = new StringBuilder();
            sb.append(receipt.getTotalString());
            sb.append(" '").append(receipt.getBizName().getBusinessName()).append("' ");
            sb.append("receipt re-checked");
            notificationService.addNotification(sb.toString(), NotificationTypeEnum.RECEIPT, receipt);

        } catch(Exception exce) {
            log.error("Revert all the transaction for Receipt={}, ReceiptOCR={}, reason={}", receipt.getId(), receiptDocument.getId(), exce.getLocalizedMessage(), exce);

            //For rollback
            if(StringUtils.isNotEmpty(receipt.getId())) {
                long sizeReceiptInitial = receiptManager.collectionSize();
                long sizeItemInitial = itemManager.collectionSize();

                itemManager.deleteWhereReceipt(receipt);
                receiptManager.deleteHard(receipt);

                long sizeReceiptFinal = receiptManager.collectionSize();
                long sizeItemFinal = itemManager.collectionSize();
                if(sizeReceiptInitial == sizeReceiptFinal) {
                    log.warn("Initial receipt size and Final receipt size are same={}:{}", sizeReceiptInitial, sizeReceiptFinal);
                } else {
                    log.warn("Initial receipt size={}, Final receipt size={}. Removed Receipt={}", sizeReceiptInitial, sizeReceiptFinal, receipt.getId());
                }

                if(sizeItemInitial == sizeItemFinal) {
                    log.warn("Initial item size and Final item size are same={}:{}", sizeItemInitial, sizeItemFinal);
                } else {
                    log.warn("Initial item size={}, Final item size={}", sizeItemInitial, sizeItemFinal);
                }

                receiptDocument.setDocumentStatus(DocumentStatusEnum.OCR_PROCESSED);
                documentManager.save(receiptDocument);
                //log.error("Failed to rollback Document: " + documentForm.getId() + ", error message: " + e.getLocalizedMessage());

                messageManager.undoUpdateObject(receiptDocument.getId(), false, DocumentStatusEnum.TURK_PROCESSED, DocumentStatusEnum.TURK_REQUEST);
                //End of roll back

                log.info("Complete with rollback: throwing exception");
            }
            throw new Exception(exce.getLocalizedMessage());
        }
    }

    private void updateMessageManager(DocumentEntity receiptOCR, DocumentStatusEnum from, DocumentStatusEnum to) {
        try {
            messageManager.updateObject(receiptOCR.getId(), from, to);
        } catch(Exception exce) {
            log.error(exce.getLocalizedMessage());
            messageManager.undoUpdateObject(receiptOCR.getId(), false, to, from);
            throw exce;
        }
    }

    /**
     * Reject receipt when invalid or un-readable
     *
     * @param receiptOCR
     * @throws Exception
     */
    public void turkReject(DocumentEntity receiptOCR) throws Exception {
        try {
            DocumentEntity document = loadActiveDocumentById(receiptOCR.getId());
            document.setDocumentStatus(DocumentStatusEnum.TURK_RECEIPT_REJECT);
            document.setDocumentOfType(receiptOCR.getDocumentOfType());
            document.setBizName(null);
            document.setBizStore(null);
            document.inActive();
            document.markAsDeleted();
            documentManager.save(document);

            try {
                messageManager.updateObject(document.getId(), DocumentStatusEnum.OCR_PROCESSED, DocumentStatusEnum.TURK_RECEIPT_REJECT);
            } catch(Exception exce) {
                log.error(exce.getLocalizedMessage());
                messageManager.undoUpdateObject(document.getId(), false, DocumentStatusEnum.TURK_RECEIPT_REJECT, DocumentStatusEnum.OCR_PROCESSED);
                throw exce;
            }
            itemOCRManager.deleteWhereReceipt(document);

            fileSystemService.deleteSoft(document.getFileSystemEntities());
            storageManager.deleteSoft(document.getFileSystemEntities());
            GridFSDBFile gridFSDBFile = storageManager.get(document.getFileSystemEntities().iterator().next().getBlobId());
            DBObject dbObject =  gridFSDBFile.getMetaData();

            StringBuilder sb = new StringBuilder();
            sb.append("Could not process receipt '").append(dbObject.get("ORIGINAL_FILENAME")).append("'");
            notificationService.addNotification(sb.toString(), NotificationTypeEnum.DOCUMENT, document);

        } catch(Exception exce) {
            log.error("Revert all the transaction for ReceiptOCR={}. Rejection of a receipt failed, reason={}", receiptOCR.getId(), exce.getLocalizedMessage(), exce);

            receiptOCR.setDocumentStatus(DocumentStatusEnum.OCR_PROCESSED);
            receiptOCR.active();
            documentManager.save(receiptOCR);
            //log.error("Failed to rollback Document: " + documentForm.getId() + ", error message: " + e.getLocalizedMessage());

            messageManager.undoUpdateObject(receiptOCR.getId(), false, DocumentStatusEnum.TURK_RECEIPT_REJECT, DocumentStatusEnum.OCR_PROCESSED);
            //End of roll back

            log.info("Complete with rollback: throwing exception");
        }
    }

    /**
     * Delete all the associated data with Document like Item OCR, and
     * Message Receipt Entity OCR including deletion of with Document
     * But cannot delete ReceiptOCR when the receipt has been processed once and now it pending for re-check
     *
     *
     * @param receiptOCR
     */
    public void deletePendingReceiptOCR(DocumentEntity receiptOCR) {
        DocumentEntity documentEntity = loadActiveDocumentById(receiptOCR.getId());
        if(documentEntity == null || !StringUtils.isEmpty(documentEntity.getReceiptId())) {
            log.warn("User trying to delete processed Document={}, Receipt={}", receiptOCR.getId(), receiptOCR.getReceiptId());
        } else {
            deleteReceiptOCR(documentEntity);
        }
    }

    /**
     * Delete all the associated data with Document like Item OCR, and
     * Message Receipt Entity OCR including deletion of with Document
     * But cannot delete ReceiptOCR when the receipt has been processed once and now it pending for re-check
     *
     *
     * @param receiptOCR
     */
    public void deleteRejectedReceiptOCR(DocumentEntity receiptOCR) {
        DocumentEntity documentEntity = loadRejectedDocumentById(receiptOCR.getId());
        if(documentEntity == null || !StringUtils.isEmpty(documentEntity.getReceiptId())) {
            log.warn("User trying to delete processed Document={}, Receipt={}", receiptOCR.getId(), receiptOCR.getReceiptId());
        } else {
            deleteReceiptOCR(documentEntity);
        }
    }

    private void deleteReceiptOCR(DocumentEntity documentEntity) {
        documentManager.deleteHard(documentEntity);
        itemOCRManager.deleteWhereReceipt(documentEntity);
        messageManager.deleteAllForReceiptOCR(documentEntity.getId());
        storageManager.deleteHard(documentEntity.getFileSystemEntities());
        fileSystemService.deleteHard(documentEntity.getFileSystemEntities());
    }

    /**
     * Populates items with BizNameEntity
     *
     * @param items
     * @param receiptEntity
     */
    private void populateItemsWithBizName(List<ItemEntity> items, ReceiptEntity receiptEntity) {
        for(ItemEntity item : items) {
            item.setBizName(receiptEntity.getBizName());
            populateWithExpenseType(item);
        }
    }

    /**
     * when Items are populated with just an Id of the expenseType. This normally happens during Re-Check condition.
     * The following code makes sures objects are populated with just not id but with complete object instead
     * //TODO in future keep an eye on this object as during save of an ItemEntity the @DBRef expenseType is saved as Id instead of an object. As of now it is saved and updated
     *
     * @param item
     */
    private void populateWithExpenseType(ItemEntity item) {
        if(item.getExpenseTag() != null && item.getExpenseTag().getId() != null) {
            ExpenseTagEntity expenseType = userProfilePreferenceService.getExpenseType(item.getExpenseTag().getId());
            item.setExpenseTag(expenseType);
        }
    }

    /**
     * Condition to check if the record already exists
     *
     * @param checkSum
     * @return
     */
    public boolean checkIfDuplicate(String checkSum, String id) {
        return receiptManager.notDeletedChecksumDuplicate(checkSum, id);
    }

    /**
     * Does a similar receipt exists with this checksum. This can be used when adding new receipt or while soft deleting
     * of a receipt.
     *
     * @param checksum
     * @return
     */
    public boolean hasReceiptWithSimilarChecksum(String checksum) {
        return receiptManager.hasRecordWithSimilarChecksum(checksum);
    }

    public void turkMileage(MileageEntity mileageEntity, DocumentEntity documentForm) {
        try {
            DocumentEntity documentEntity = loadActiveDocumentById(documentForm.getId());

            mileageEntity.setFileSystemEntities(documentEntity.getFileSystemEntities());
            mileageEntity.setDocumentId(documentEntity.getId());
            mileageService.save(mileageEntity);

            documentForm.setFileSystemEntities(documentEntity.getFileSystemEntities());

            //update the version number as the value could have changed by rotating receipt image through ajax
            documentForm.setVersion(documentEntity.getVersion());
            documentForm.setDocumentStatus(DocumentStatusEnum.TURK_PROCESSED);
            documentForm.setReceiptId(mileageEntity.getId());
            documentForm.inActive();
            documentManager.save(documentForm);

            updateMessageManager(documentForm, DocumentStatusEnum.OCR_PROCESSED, DocumentStatusEnum.TURK_PROCESSED);

            StringBuilder sb = new StringBuilder();
            sb.append(mileageEntity.getStart());
            sb.append(", ");
            sb.append("odometer reading processed");
            notificationService.addNotification(sb.toString(), NotificationTypeEnum.MILEAGE, mileageEntity);
        } catch(DuplicateKeyException duplicateKeyException) {
            log.error(duplicateKeyException.getLocalizedMessage(), duplicateKeyException);
            throw new RuntimeException("Found existing record with similar odometer reading");
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            //add roll back
        }
    }
}
