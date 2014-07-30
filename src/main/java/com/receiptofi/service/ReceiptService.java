package com.receiptofi.service;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;
import com.receiptofi.domain.CommentEntity;
import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ItemEntityOCR;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.domain.types.CommentTypeEnum;
import com.receiptofi.domain.types.DocumentStatusEnum;
import com.receiptofi.repository.CommentManager;
import com.receiptofi.repository.DocumentManager;
import com.receiptofi.repository.ItemManager;
import com.receiptofi.repository.ItemOCRManager;
import com.receiptofi.repository.ReceiptManager;
import com.receiptofi.repository.StorageManager;
import com.receiptofi.repository.UserProfileManager;
import com.receiptofi.service.routes.FileUploadDocumentSenderJMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 4/27/13
 * Time: 1:09 PM
 */
@Service
public final class ReceiptService {
    private static Logger log = LoggerFactory.getLogger(ReceiptService.class);

    @Autowired private ReceiptManager receiptManager;
    @Autowired private DocumentManager documentManager;
    @Autowired private DocumentUpdateService documentUpdateService;
    @Autowired private StorageManager storageManager;
    @Autowired private ItemManager itemManager;
    @Autowired private ItemOCRManager itemOCRManager;
    @Autowired private UserProfileManager userProfileManager;
    @Autowired private FileUploadDocumentSenderJMS senderJMS;
    @Autowired private CommentManager commentManager;
    @Autowired private FileSystemService fileSystemService;

    /**
     * Find receipt for a receipt id for a specific user profile id
     *
     * @param receiptId
     * @return
     */
    public ReceiptEntity findReceipt(String receiptId, String userProfileId) {
        return receiptManager.findReceipt(receiptId, userProfileId);
    }

    /**
     *
     * @param dateTime
     * @param userProfileId
     * @return
     */
    public List<ReceiptEntity> findReceipt(DateTime dateTime, String userProfileId) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthOfYear();
        int day = dateTime.getDayOfMonth();

        return receiptManager.findThisDayReceipts(year, month, day, userProfileId);
    }

    /**
     * Find items for a receipt
     *
     * @param receiptEntity
     * @return
     */
    public List<ItemEntity> findItems(ReceiptEntity receiptEntity) {
        return itemManager.getWhereReceipt(receiptEntity);
    }

    /**
     * Delete a Receipt and its associated data
     * @param receiptId - Receipt id to delete
     */
    public boolean deleteReceipt(String receiptId, String userProfileId) throws Exception {
        ReceiptEntity receipt = receiptManager.findOne(receiptId, userProfileId);
        if(receipt == null) {
            return false;
        }
        if(receipt.isActive()) {
            itemManager.deleteSoft(receipt);
            fileSystemService.deleteSoft(receipt.getFileSystemEntities());
            storageManager.deleteSoft(receipt.getFileSystemEntities());

            if(receipt.getRecheckComment() != null && !StringUtils.isEmpty(receipt.getRecheckComment().getId())) {
                commentManager.deleteHard(receipt.getRecheckComment());
            }
            if(receipt.getNotes() != null && !StringUtils.isEmpty(receipt.getNotes().getId())) {
                commentManager.deleteHard(receipt.getNotes());
            }

            if(!StringUtils.isEmpty(receipt.getReceiptOCRId())) {
                DocumentEntity documentEntity = documentManager.findOne(receipt.getReceiptOCRId(), userProfileId);
                if(documentEntity != null) {
                    itemOCRManager.deleteWhereReceipt(documentEntity);
                    documentManager.deleteHard(documentEntity);
                    receipt.setReceiptOCRId(null);
                }
            }

            receiptManager.deleteSoft(receipt);
            return true;
        } else {
            log.error("Attempt to delete inactive Receipt={}, Browser Back Action performed", receipt.getId());
            throw new Exception("Receipt no longer exists");
        }
    }

    /**
     * Inactive the receipt and active ReceiptOCR. Delete all the ItemOCR and recreate from Items. Then delete all the items.
     * @param receiptId
     * @param userProfileId
     * @throws Exception
     */
    public void reopen(String receiptId, String userProfileId) throws Exception {
        try {
            ReceiptEntity receipt = receiptManager.findOne(receiptId, userProfileId);
            if(receipt.getReceiptOCRId() == null) {
                log.error("No receiptOCR id found in Receipt={}, aborting the reopen process", receipt.getId());
                throw new Exception("Receipt could not be requested for Re-Check. Contact administrator with Receipt # " + receipt.getId() + ", contact Administrator with the Id");
            } else {
                if(receipt.isActive()) {
                    receipt.inActive();
                    List<ItemEntity> items = itemManager.getWhereReceipt(receipt);

                    DocumentEntity receiptOCR = documentManager.findOne(receipt.getReceiptOCRId(), userProfileId);
                    receiptOCR.active();
                    receiptOCR.setDocumentStatus(DocumentStatusEnum.TURK_REQUEST);
                    receiptOCR.setRecheckComment(receipt.getRecheckComment());
                    receiptOCR.setNotes(receipt.getNotes());

                    /** All activity at the end is better because you never know what could go wrong during populating other data */
                    receipt.setReceiptStatus(DocumentStatusEnum.TURK_REQUEST);
                    receiptManager.save(receipt);
                    documentManager.save(receiptOCR);
                    itemOCRManager.deleteWhereReceipt(receiptOCR);

                    List<ItemEntityOCR> ocrItems = getItemEntityFromItemEntityOCR(items, receiptOCR);
                    itemOCRManager.saveObjects(ocrItems);
                    itemManager.deleteWhereReceipt(receipt);

                    log.info("DocumentEntity @Id after save: " + receiptOCR.getId());
                    UserProfileEntity userProfile = userProfileManager.findByReceiptUserId(receiptOCR.getUserProfileId());
                    senderJMS.send(receiptOCR, userProfile);
                } else {
                    log.error("Attempt to invoke re-check on Receipt={}, Browser Back Action performed", receipt.getId());
                    throw new Exception("Receipt no longer exists");
                }
            }
        } catch (Exception e) {
            log.error("Exception during customer requesting receipt recheck operation, reason={}", e.getLocalizedMessage(), e);

            //Need to send a well formatted error message to customer instead of jumbled mumbled exception stacktrace
            throw new Exception(
                    "Exception occurred during requesting receipt recheck operation for Receipt # " +
                            receiptId +
                            ", contact Administrator with the Id"
            );
        }
    }



    /**
     * Used when data is read from Receipt and Item Entity during re-check process
     *
     * @param items
     * @param receiptOCR
     * @return
     */
    public List<ItemEntityOCR> getItemEntityFromItemEntityOCR(List<ItemEntity> items, DocumentEntity receiptOCR) {
        List<ItemEntityOCR> listOfItems = new ArrayList<>();

        for(ItemEntity item : items) {
            if(StringUtils.isNotEmpty(item.getName())) {
                ItemEntityOCR itemOCR = ItemEntityOCR.newInstance();
                itemOCR.setName(item.getName());
                itemOCR.setPrice(item.getPrice().toString());
                itemOCR.setTaxed(item.getTaxed());
                itemOCR.setSequence(item.getSequence());
                itemOCR.setReceipt(receiptOCR);
                itemOCR.setUserProfileId(receiptOCR.getUserProfileId());
                itemOCR.setExpenseTag(item.getExpenseTag());
                itemOCR.setCreated(item.getCreated());
                itemOCR.setQuantity(item.getQuantity());
                itemOCR.setUpdated();

                itemOCR.setBizName(receiptOCR.getBizName());
                listOfItems.add(itemOCR);
            }
        }

        return listOfItems;
    }

    /**
     * Updates the ItemEntity with changed ExpenseType
     *
     * @param item
     */
    public void updateItemWithExpenseType(ItemEntity item) throws Exception {
        itemManager.updateItemWithExpenseType(item);
    }

    /**
     * Saves notes to receipt
     *
     * @param notes
     * @param receiptId
     * @param userProfileId
     * @return
     */
    public boolean updateReceiptNotes(String notes, String receiptId, String userProfileId) {
        ReceiptEntity receiptEntity = receiptManager.findReceipt(receiptId, userProfileId);
        CommentEntity commentEntity = receiptEntity.getNotes();
        boolean commentEntityBoolean = false;
        if(commentEntity == null) {
            commentEntityBoolean = true;
            commentEntity = CommentEntity.newInstance(CommentTypeEnum.NOTES);
            commentEntity.setText(notes);
        } else {
            commentEntity.setText(notes);
        }
        try {
            commentEntity.setUpdated();
            commentManager.save(commentEntity);
            if(commentEntityBoolean) {
                receiptEntity.setNotes(commentEntity);
                receiptManager.save(receiptEntity);
            }
            return true;
        } catch (Exception exce) {
            log.error("Failed updating notes for Receipt={}, reason={}", receiptId, exce.getLocalizedMessage(), exce);
            return false;
        }
    }

    /**
     * Saves recheck comment to receipt
     *
     * @param comment
     * @param receiptId
     * @param userProfileId
     * @return
     */
    public boolean updateReceiptComment(String comment, String receiptId, String userProfileId) {
        ReceiptEntity receiptEntity = receiptManager.findReceipt(receiptId, userProfileId);
        CommentEntity commentEntity = receiptEntity.getRecheckComment();
        boolean commentEntityBoolean = false;
        if(commentEntity == null) {
            commentEntityBoolean = true;
            commentEntity = CommentEntity.newInstance(CommentTypeEnum.RECHECK);
            commentEntity.setText(comment);
        } else {
            commentEntity.setText(comment);
        }
        try {
            commentEntity.setUpdated();
            commentManager.save(commentEntity);
            if(commentEntityBoolean) {
                receiptEntity.setRecheckComment(commentEntity);
                receiptManager.save(receiptEntity);
            }
            return true;
        } catch (Exception exce) {
            log.error("Failed updating comment for Receipt={}, reason={}", receiptId, exce.getLocalizedMessage(), exce);
            return false;
        }
    }

    /**
     * Saves recheck comment to Document
     *
     * @param comment
     * @param documentId
     * @return
     */
    public boolean updateDocumentComment(String comment, String documentId) {
        DocumentEntity documentEntity = documentUpdateService.loadActiveDocumentById(documentId);
        CommentEntity commentEntity = documentEntity.getRecheckComment();
        boolean commentEntityBoolean = false;
        if(commentEntity == null) {
            commentEntityBoolean = true;
            commentEntity = CommentEntity.newInstance(CommentTypeEnum.RECHECK);
            commentEntity.setText(comment);
        } else {
            commentEntity.setText(comment);
        }
        try {
            commentEntity.setUpdated();
            commentManager.save(commentEntity);
            if(commentEntityBoolean) {
                documentEntity.setRecheckComment(commentEntity);
                documentManager.save(documentEntity);
            }
            return true;
        } catch (Exception exce) {
            log.error("Failed updating comment for ReceiptOCR={}, reason={}", documentId, exce.getLocalizedMessage(), exce);
            return false;
        }
    }

    /**
     *
     * @param bizNameEntity
     * @param userProfileId
     * @return
     */
    public List<ReceiptEntity> findReceipt(BizNameEntity bizNameEntity, String userProfileId) {
        return receiptManager.findReceipt(bizNameEntity, userProfileId);
    }

    /**
     * Counts all the valid and invalid receipt that has referred the store
     *
     * @param bizStoreEntity
     * @return
     */
    public long countAllReceiptForAStore(BizStoreEntity bizStoreEntity) {
        return receiptManager.countAllReceiptForAStore(bizStoreEntity);
    }

    /**
     * Counts all the valid and invalid receipt that has referred the biz name
     *
     * @param bizNameEntity
     * @return
     */
    public long countAllReceiptForABizName(BizNameEntity bizNameEntity) {
        return receiptManager.countAllReceiptForABizName(bizNameEntity);
    }

    /**
     * Used for updating expense report info in the receipt
     *
     * @param receiptEntity
     * @return
     */
    public boolean updateReceiptWithExpReportFilename(ReceiptEntity receiptEntity) {
        try {
            receiptManager.save(receiptEntity);
        } catch (Exception e) {
            log.error("Failed updating ReceiptEntity with Expense Report Filename, reason={}", e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public void removeExpensofiFilenameReference(String filename) {
        receiptManager.removeExpensofiFilenameReference(filename);
    }
}
