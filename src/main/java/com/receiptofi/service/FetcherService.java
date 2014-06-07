package com.receiptofi.service;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;
import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.repository.BizNameManager;
import com.receiptofi.repository.BizStoreManager;
import com.receiptofi.repository.ItemManager;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.Formatter;
import com.receiptofi.utils.PerformanceProfiling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 4/28/13
 * Time: 7:57 PM
 */
@Service
public final class FetcherService {
    private static final Logger log = LoggerFactory.getLogger(FetcherService.class);

    @Autowired private ItemManager itemManager;
    @Autowired private BizNameManager bizNameManager;
    @Autowired private BizStoreManager bizStoreManager;
    @Autowired private FileSystemService fileSystemService;

    /**
     * This method is called from AJAX to get the matching list of users in the system
     *
     * @param bizName
     * @return
     */
    public Set<String> findDistinctBizName(String bizName) {
        DateTime time = DateUtil.now();
        log.info("Search for Biz Name: " + bizName);
        Set<String> titles = bizNameManager.findAllDistinctBizStr(bizName);
        log.info("found business.. total size " + titles.size());
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return titles;
    }

    /**
     *
     * @param bizAddress
     * @param bizName
     * @return
     */
    public Set<String> findDistinctBizAddress(String bizAddress, String bizName) {
        DateTime time = DateUtil.now();
        log.info("Search for Biz address: " + bizAddress + ", within Biz Name: " + bizName);
        Set<String> address = new HashSet<>();

        BizNameEntity bizNameEntity = bizNameManager.findOneByName(bizName);
        if(bizNameEntity != null) {
            List<BizStoreEntity> list = bizStoreManager.getAllWithJustSpecificField(bizAddress, bizNameEntity, BizStoreManager.ADDRESS);
            for(BizStoreEntity bizStoreEntity : list) {
                address.add(bizStoreEntity.getAddress());
            }

            log.info("found address(es).. total size " + list.size() + ", but unique items size: " + address.size());
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return address;
    }

    /**
     *
     * @param bizAddress
     * @param bizName
     * @return
     */
    public Set<String> findDistinctBizPhone(String bizPhone, String bizAddress, String bizName) {
        DateTime time = DateUtil.now();
        log.info("Search for Biz address: " + bizAddress + ", within Biz Name: " + bizName);
        Set<String> phone = new HashSet<>();

        BizNameEntity bizNameEntity = bizNameManager.findOneByName(bizName);
        if(bizNameEntity != null) {
            List<BizStoreEntity> list = bizStoreManager.getAllWithJustSpecificField(bizPhone, bizAddress, bizNameEntity, BizStoreManager.PHONE);

            for(BizStoreEntity bizStoreEntity : list) {
                phone.add(Formatter.phone(bizStoreEntity.getPhone()));
            }

            log.info("found item.. total size " + list.size() + ", but unique items size: " + phone.size());
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return phone;
    }

    /**
     * This method is called from AJAX to get the matching list of items in the system.
     * Populates with just the 'name' of the item
     *
     * @param itemName
     * @param bizName
     * @return
     */
    public Set<String> findDistinctItems(String itemName, String bizName) {
        DateTime time = DateUtil.now();
        log.info("Search for item name: " + itemName + ", within Biz Name: " + bizName);
        List<ItemEntity> itemList = itemManager.findItems(itemName, bizName);

        Set<String> items = new HashSet<>();
        for(ItemEntity re : itemList) {
            items.add(re.getName());
        }

        log.info("found item.. total size " + itemList.size() + ", but unique items size: " + items.size());
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return items;
    }

    public void changeFSImageOrientation(String fileSystemId, int imageOrientation, String blobId) throws Exception {
        DateTime time = DateUtil.now();
        FileSystemEntity fileSystemEntity = fileSystemService.findById(fileSystemId);
        if(blobId.equalsIgnoreCase(fileSystemEntity.getBlobId())) {
            fileSystemEntity.setImageOrientation(fileSystemEntity.getImageOrientation() + imageOrientation);
            fileSystemEntity.switchHeightAndWidth();
            fileSystemService.save(fileSystemEntity);
        }
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
    }
}
