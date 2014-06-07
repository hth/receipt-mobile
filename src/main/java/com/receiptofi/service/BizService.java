package com.receiptofi.service;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;
import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.repository.BizNameManager;
import com.receiptofi.repository.BizStoreManager;
import com.receiptofi.web.form.BizForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 8/8/13
 * Time: 8:48 AM
 */
@Service
public final class BizService {
    private static final Logger log = LoggerFactory.getLogger(BizService.class);

    @Autowired private BizNameManager bizNameManager;
    @Autowired private BizStoreManager bizStoreManager;
    @Autowired private ExternalService externalService;
    @Autowired private ReceiptService receiptService;

    public BizNameEntity findName(String bizId) {
        return bizNameManager.findOne(bizId);
    }

    public void saveName(BizNameEntity bizNameEntity) throws Exception {
        bizNameManager.save(bizNameEntity);
    }

    public BizStoreEntity findStore(String storeId) {
        return bizStoreManager.findOne(storeId);
    }

    public void saveStore(BizStoreEntity bizStoreEntity) throws Exception {
        bizStoreManager.save(bizStoreEntity);
    }

    public Set<BizStoreEntity> bizSearch(String businessName, String bizAddress, String bizPhone) {
        Set<BizStoreEntity> bizStoreEntities = new HashSet<>();

        if(StringUtils.isNotEmpty(businessName)) {
            List<BizNameEntity> bizNameEntities = bizNameManager.findAllBizWithMatchingName(businessName);
            for(BizNameEntity bizNameEntity : bizNameEntities) {
                List<BizStoreEntity> bizStores = bizStoreManager.findAllWithStartingAddressStartingPhone(bizAddress, bizPhone, bizNameEntity);
                bizStoreEntities.addAll(bizStores);
            }
        } else {
            List<BizStoreEntity> bizStores = bizStoreManager.findAllWithStartingAddressStartingPhone(bizAddress, bizPhone, null);
            bizStoreEntities.addAll(bizStores);
        }
        return bizStoreEntities;
    }

    public void countReceiptForBizStore(Set<BizStoreEntity> bizStoreEntities, BizForm bizForm) {
        for(BizStoreEntity bizStoreEntity : bizStoreEntities) {
            long count = receiptService.countAllReceiptForAStore(bizStoreEntity);
            bizForm.addReceiptCount(bizStoreEntity.getId(), count);
        }
    }

    public long countReceiptForBizName(BizNameEntity bizNameEntity) {
        return receiptService.countAllReceiptForABizName(bizNameEntity);
    }

    /**
     * This method is being used by Admin to create new Business and Stores. Also this method is being used by receipt update to do the same.
     *
     * @param receiptEntity
     */
    public void saveNewBusinessAndOrStore(ReceiptEntity receiptEntity) throws Exception {
        BizNameEntity bizNameEntity = receiptEntity.getBizName();
        BizStoreEntity bizStoreEntity = receiptEntity.getBizStore();

        BizNameEntity bizName = bizNameManager.findOneByName(bizNameEntity.getBusinessName());
        if(bizName == null) {
            try {
                bizNameManager.save(bizNameEntity);

                bizStoreEntity.setBizName(bizNameEntity);
                externalService.decodeAddress(bizStoreEntity);
                bizStoreManager.save(bizStoreEntity);

                receiptEntity.setBizName(bizNameEntity);
                receiptEntity.setBizStore(bizStoreEntity);
            } catch (DuplicateKeyException | IOException e) {
                log.error(e.getLocalizedMessage());

                if(StringUtils.isNotEmpty(bizNameEntity.getId())) {
                    bizNameManager.deleteHard(bizNameEntity);
                }
                BizStoreEntity biz = bizStoreManager.findOne(bizStoreEntity);
                throw new Exception("Address and Phone already registered with another Business Name: " + biz.getBizName().getBusinessName());
            }
        } else {
            BizStoreEntity bizStore = bizStoreManager.findOne(bizStoreEntity);
            if(bizStore == null) {
                try {
                    bizStoreEntity.setBizName(bizName);
                    externalService.decodeAddress(bizStoreEntity);
                    bizStoreManager.save(bizStoreEntity);

                    receiptEntity.setBizName(bizName);
                    receiptEntity.setBizStore(bizStoreEntity);
                } catch (DuplicateKeyException | IOException e) {
                    log.error(e.getLocalizedMessage());
                    BizStoreEntity biz = bizStoreManager.findOne(bizStoreEntity);
                    throw new Exception("Address and Phone already registered with another Business Name: " + biz.getBizName().getBusinessName());
                }
            } else {
                receiptEntity.setBizName(bizName);
                receiptEntity.setBizStore(bizStore);
            }
        }
    }

    /**
     * This method is being used by Admin to create new Business and Stores. Also this method is being used by receipt update to do the same.
     *
     * @param document
     */
    public void saveNewBusinessAndOrStore(DocumentEntity document) throws Exception {
        BizNameEntity bizNameEntity = document.getBizName();
        BizStoreEntity bizStoreEntity = document.getBizStore();

        BizNameEntity bizName = bizNameManager.findOneByName(bizNameEntity.getBusinessName());
        if(bizName == null) {
            try {
                bizNameManager.save(bizNameEntity);

                bizStoreEntity.setBizName(bizNameEntity);
                externalService.decodeAddress(bizStoreEntity);
                bizStoreManager.save(bizStoreEntity);

                document.setBizName(bizNameEntity);
                document.setBizStore(bizStoreEntity);
            } catch (DuplicateKeyException e) {
                log.error(e.getLocalizedMessage());

                if(StringUtils.isNotEmpty(bizNameEntity.getId())) {
                    bizNameManager.deleteHard(bizNameEntity);
                }
                BizStoreEntity biz = bizStoreManager.findOne(bizStoreEntity);
                throw new Exception("Address and Phone already registered with another Business Name: " + biz.getBizName().getBusinessName());
            }
        } else {
            BizStoreEntity bizStore = bizStoreManager.findOne(bizStoreEntity);
            if(bizStore == null) {
                try {
                    bizStoreEntity.setBizName(bizName);
                    externalService.decodeAddress(bizStoreEntity);
                    bizStoreManager.save(bizStoreEntity);

                    document.setBizName(bizName);
                    document.setBizStore(bizStoreEntity);
                } catch (DuplicateKeyException e) {
                    log.error(e.getLocalizedMessage());
                    BizStoreEntity biz = bizStoreManager.findOne(bizStoreEntity);
                    throw new Exception("Address and Phone already registered with another Business Name: " + biz.getBizName().getBusinessName());
                }
            } else {
                document.setBizName(bizName);
                document.setBizStore(bizStore);
            }
        }
    }

    /**
     * Find last ten business stores for business name
     *
     * @param receiptEntity
     * @return
     */
    public Set<BizStoreEntity> getAllStoresForBusinessName(ReceiptEntity receiptEntity) {
        Set<BizStoreEntity> bizStoreEntities = new HashSet<>();
        bizStoreEntities.addAll(bizStoreManager.findAllWithStartingAddressStartingPhone(null, null, receiptEntity.getBizName()));
        return bizStoreEntities;
    }

    public void deleteBizStore(BizStoreEntity bizStore) {
        bizStoreManager.deleteHard(bizStore);
    }

    public void deleteBizName(BizNameEntity bizName) {
        bizNameManager.deleteHard(bizName);
    }
}
