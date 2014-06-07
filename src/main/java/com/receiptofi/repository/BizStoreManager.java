package com.receiptofi.repository;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;

import java.util.List;

/**
 * User: hitender
 * Date: 4/22/13
 * Time: 11:20 PM
 */
public interface BizStoreManager extends RepositoryManager<BizStoreEntity> {
    int STORE_LIMIT = 10;

    //TODO use annotation instead
    /** Field name */
    String ADDRESS = "ADDRESS";
    String PHONE = "PHONE";

    BizStoreEntity noStore();

    BizStoreEntity findOne(BizStoreEntity bizStoreEntity);

    /**
     * Search for specific Biz, Address or Phone. Limited to 30
     *
     * @param bizAddress
     * @param bizPhone
     * @param bizNameEntity
     * @return
     */
    List<BizStoreEntity> findAllWithStartingAddressStartingPhone(String bizAddress, String bizPhone, BizNameEntity bizNameEntity);
    List<BizStoreEntity> findAllWithAnyAddressAnyPhone(String bizAddress, String bizPhone, BizNameEntity bizNameEntity);

    /**
     * Used for Ajax. Populates BizStoreEntity with just fieldName.
     *
     * @param bizAddress
     * @param bizNameEntity
     * @return
     */
    List<BizStoreEntity> getAllWithJustSpecificField(String bizAddress, BizNameEntity bizNameEntity, String fieldName);

    /**
     * Used for Ajax. Populates BizStoreEntity with just fieldName.
     *
     * @param bizPhone
     * @param bizAddress
     * @param bizNameEntity
     * @return
     */
    List<BizStoreEntity> getAllWithJustSpecificField(String bizPhone, String bizAddress, BizNameEntity bizNameEntity, String fieldName);

    /**
     * BizStore sorted on create date and limited to latest records
     *
     * @param bizNameEntity
     * @param limit
     * @return
     *
     * @deprecated replaced by findAllWithStartingAddressStartingPhone
     */
    @Deprecated
    List<BizStoreEntity> findAllAddress(BizNameEntity bizNameEntity, int limit);
}
