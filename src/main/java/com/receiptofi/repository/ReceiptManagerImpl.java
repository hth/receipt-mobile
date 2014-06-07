/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.types.DocumentStatusEnum;
import com.receiptofi.domain.value.ReceiptGrouped;
import com.receiptofi.domain.value.ReceiptGroupedByBizLocation;
import com.receiptofi.utils.DateUtil;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.entityUpdate;
import static com.receiptofi.repository.util.AppendAdditionalFields.isActive;
import static com.receiptofi.repository.util.AppendAdditionalFields.isNotDeleted;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import org.joda.time.DateTime;

import com.mongodb.WriteResult;

/**
 * @author hitender
 * @since Dec 26, 2012 9:17:04 PM
 *
 */
@Repository
public final class ReceiptManagerImpl implements ReceiptManager {
	private static final Logger log = LoggerFactory.getLogger(ReceiptManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(ReceiptEntity.class, Document.class, "collection");

    @Value("${displayMonths:13}")
    int displayMonths;

	@Autowired private MongoTemplate mongoTemplate;
    @Autowired private ItemManager itemManager;
    @Autowired private FileSystemManager fileSystemManager;
    @Autowired private StorageManager storageManager;

	@Override
    public List<ReceiptEntity> getAllObjects() {
		return mongoTemplate.findAll(ReceiptEntity.class, TABLE);
	}

	@Override
    public List<ReceiptEntity> getAllReceipts(String userProfileId) {
        Criteria criteria = where("USER_PROFILE_ID").is(userProfileId)
                .andOperator(
                        isActive(),
                        isNotDeleted()
                );

        Sort sort = new Sort(DESC, "RECEIPT_DATE").and(new Sort(DESC, "C"));
        return mongoTemplate.find(query(criteria).with(sort), ReceiptEntity.class, TABLE);
    }

    @Override
    public List<ReceiptEntity> getAllReceiptsForTheYear(String userProfileId, DateTime startOfTheYear) {
        Criteria criteria = where("USER_PROFILE_ID").is(userProfileId)
                .and("RECEIPT_DATE").gte(startOfTheYear)
                .andOperator(
                        isActive(),
                        isNotDeleted()
                );

		Sort sort = new Sort(DESC, "RECEIPT_DATE").and(new Sort(DESC, "C"));
		return mongoTemplate.find(query(criteria).with(sort), ReceiptEntity.class, TABLE);
	}

    @Override
    public List<ReceiptEntity> getAllReceiptsForThisMonth(String userProfileId, DateTime monthYear) {
        Criteria criteria = where("USER_PROFILE_ID").is(userProfileId)
                .and("MONTH").is(monthYear.getMonthOfYear())
                .and("YEAR").is(monthYear.getYear())
                .andOperator(
                        isActive(),
                        isNotDeleted()
                );

        Sort sort = new Sort(DESC, "RECEIPT_DATE").and(new Sort(DESC, "C"));
        return mongoTemplate.find(query(criteria).with(sort), ReceiptEntity.class, TABLE);
    }

	@Override
    public Iterator<ReceiptGrouped> getAllObjectsGroupedByDate(String userProfileId) {
        GroupBy groupBy = GroupBy.key("DAY", "MONTH", "YEAR")
                .initialDocument("{ total: 0 }")
                .reduceFunction("function(obj, result) { " +
                        "  result.day = obj.DAY; " +
                        "  result.month = obj.MONTH; " +
                        "  result.year = obj.YEAR; " +
                        "  result.total += obj.TOTAL; " +
                        "}");

        Criteria criteria = where("USER_PROFILE_ID").is(userProfileId)
                .andOperator(
                        isActive(),
                        isNotDeleted()
                );

        GroupByResults<ReceiptGrouped> results = mongoTemplate.group(criteria, TABLE, groupBy, ReceiptGrouped.class);
        return results.iterator();
	}

    //TODO find a way to format the total in group by
    @Override
    public Iterator<ReceiptGrouped> getAllObjectsGroupedByMonth(String userProfileId) {
        GroupBy groupBy = GroupBy.key("MONTH", "YEAR")
                .initialDocument("{ total: 0 }")
                .reduceFunction("function(obj, result) { " +
                        "  result.month = obj.MONTH; " +
                        "  result.year = obj.YEAR; " +
                        "  result.total += obj.TOTAL; " +
                        "}");

        DateTime date = DateUtil.now().minusMonths(displayMonths);
        DateTime since = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0);
        Criteria criteria = where("USER_PROFILE_ID").is(userProfileId)
                .and("RECEIPT_DATE").gte(since.toDate())
                .andOperator(
                        isActive(),
                        isNotDeleted()
                );

        GroupByResults<ReceiptGrouped> results = mongoTemplate.group(criteria, TABLE, groupBy, ReceiptGrouped.class);
        return results.iterator();
    }

    public Iterator<ReceiptGroupedByBizLocation> getAllReceiptGroupedByBizLocation(String userProfileId) {
        GroupBy groupBy = GroupBy.key("BIZ_STORE", "BIZ_NAME")
                .initialDocument("{ total: 0 }")
                .reduceFunction("function(obj, result) { " +
                        "  result.total += obj.TOTAL; " +
                        "  result.bizStore = obj.BIZ_STORE; " +
                        "  result.bizName = obj.BIZ_NAME; " +
                        "}");


        DateTime date = DateUtil.now().minusMonths(displayMonths);
        DateTime since = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0);
        Criteria criteria = where("USER_PROFILE_ID").is(userProfileId)
                .and("RECEIPT_DATE").gte(since.toDate())
                .andOperator(
                        isActive(),
                        isNotDeleted()
                );

        GroupByResults<ReceiptGroupedByBizLocation> results = mongoTemplate.group(criteria, TABLE, groupBy, ReceiptGroupedByBizLocation.class);
        return results.iterator();
    }

    //http://stackoverflow.com/questions/12949870/spring-mongotemplate-find-special-column
    @Override
    public List<String> findTitles(String title) {
        Criteria criteria = where("TITLE").regex(title, "i");
        Query query = query(criteria);

        //This makes just one of the field populated
        query.fields().include("TITLE");
        List<ReceiptEntity> receipts = mongoTemplate.find(query, ReceiptEntity.class, TABLE);

        List<String> titles = new ArrayList<>();
        for(ReceiptEntity re : receipts) {
            titles.add(re.getBizName().getBusinessName());
        }

        return titles;
    }

    @Override
	public void save(ReceiptEntity object) {
		mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
		try {
			// Cannot use insert because insert does not perform update like save.
			// Save will always try to update or create new record.
			// mongoTemplate.insert(object, TABLE);

            if(object.getId() != null) {
                object.setUpdated();
            }
            object.computeChecksum();
			mongoTemplate.save(object, TABLE);
		} catch (DataIntegrityViolationException e) {
			log.error("Duplicate record entry for ReceiptEntity={}", e);
            //todo should throw a better exception; this is highly likely to happen any time soon
            throw new RuntimeException(e.getMessage());
		}
	}

    /**
     * Use findReceipt method instead of findOne
     *
     * @param id
     * @return
     */
    @Deprecated
	@Override
    public ReceiptEntity findOne(String id) {
		return mongoTemplate.findOne(query(where("id").is(id)), ReceiptEntity.class, TABLE);
	}

    @Override
    public ReceiptEntity findOne(String receiptId, String userProfileId) {
        Query query = query(where("id").is(receiptId)
                .and("USER_PROFILE_ID").is(userProfileId));

        return mongoTemplate.findOne(query, ReceiptEntity.class, TABLE);
    }

    /**
     * Use this method instead of findOne
     *
     * @param receiptId
     * @param userProfileId
     * @return
     */
    @Override
    public ReceiptEntity findReceipt(String receiptId, String userProfileId) {
        Query query = query(where("id").is(receiptId)
                .and("USER_PROFILE_ID").is(userProfileId)
                .andOperator(
                        isActive(),
                        isNotDeleted()
                )
        );
        return mongoTemplate.findOne(query, ReceiptEntity.class, TABLE);
    }

    @Override
    public List<ReceiptEntity> findReceipt(BizNameEntity bizNameEntity, String userProfileId) {
        Criteria criteria = where("userProfileId").is(userProfileId)
                .and("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()))
                .andOperator(
                        isActive(),
                        isNotDeleted()
                );

        Sort sort = new Sort(DESC, "RECEIPT_DATE");
        return mongoTemplate.find(query(criteria).with(sort), ReceiptEntity.class, TABLE);
    }

    @Override
    public ReceiptEntity findWithReceiptOCR(String receiptOCRId) {
        Query query = query(where("RECEIPT_OCR_ID").is(receiptOCRId));
        return mongoTemplate.findOne(query, ReceiptEntity.class, TABLE);
    }

	@Override
	public WriteResult updateObject(String id, String name) {
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public void deleteHard(ReceiptEntity object) {
		mongoTemplate.remove(object, TABLE);
	}

    @Override
    public void deleteSoft(ReceiptEntity object) {
        //Deleted check sum need re-calculation
        object.markAsDeleted();

        //Re-calculate check sum for deleted object
        object.computeChecksum();
        String checksum = object.getChecksum();

        if(hasRecordWithSimilarChecksum(checksum)) {
            removeCompleteReminiscenceOfSoftDeletedReceipt(checksum);
        }

        Query query = query(where("id").is(object.getId()));
        Update update = Update.update("D", true).set("CHECK_SUM", checksum);
        mongoTemplate.updateFirst(query, entityUpdate(update), ReceiptEntity.class);
    }

    /**
     * When a receipt is marked as soft delete receipt then previously soft deleted receipt is completely removed
     *
     * @param checksum
     */
    private void removeCompleteReminiscenceOfSoftDeletedReceipt(String checksum) {
        Criteria criteria = where("CHECK_SUM").is(checksum);
        List<ReceiptEntity> duplicateDeletedReceipts = mongoTemplate.find(query(criteria), ReceiptEntity.class, TABLE);
        for(ReceiptEntity receiptEntity : duplicateDeletedReceipts) {
            itemManager.deleteWhereReceipt(receiptEntity);
            fileSystemManager.deleteHard(receiptEntity.getFileSystemEntities());
            storageManager.deleteHard(receiptEntity.getFileSystemEntities());
            deleteHard(receiptEntity);
        }
    }

    @Override
    public long countAllReceiptForAStore(BizStoreEntity bizStoreEntity) {
        Criteria criteria = where("BIZ_STORE.$id").is(new ObjectId(bizStoreEntity.getId()));
        return mongoTemplate.count(query(criteria), TABLE);
    }

    @Override
    public long countAllReceiptForABizName(BizNameEntity bizNameEntity) {
        Criteria criteria = where("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()));
        return mongoTemplate.count(query(criteria), TABLE);
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }

    @Override
    public List<ReceiptEntity> findThisDayReceipts(int year, int month, int day, String userProfileId) {
        Criteria criteria = where("USER_PROFILE_ID").is(userProfileId)
                .and("YEAR").is(year)
                .and("MONTH").is(month)
                .and("DAY").is(day)
                .andOperator(
                        isActive(),
                        isNotDeleted()
                );

        Sort sort = new Sort(DESC, "RECEIPT_DATE");
        return mongoTemplate.find(query(criteria).with(sort), ReceiptEntity.class, TABLE);
    }

    @Override
    public boolean notDeletedChecksumDuplicate(String checksum, String id) {
        //Active condition is required for re-check criteria
        return mongoTemplate.find(checksumQueryIfDuplicateExists(checksum, id), ReceiptEntity.class, TABLE).size() > 0;
    }

    @Override
    public ReceiptEntity findNotDeletedChecksumDuplicate(String checksum, String id) {
        return mongoTemplate.findOne(checksumQueryIfDuplicateExists(checksum, id), ReceiptEntity.class, TABLE);
    }

    @Override
    public boolean hasRecordWithSimilarChecksum(String checksum) {
        return mongoTemplate.find(checksumQuery(checksum), ReceiptEntity.class, TABLE).size() > 0;
    }

    @Override
    public void removeExpensofiFilenameReference(String filename) {
        mongoTemplate.findAndModify(query(where("EXP_FILENAME").is(filename)), Update.update("EXP_FILENAME", ""), ReceiptEntity.class);
    }

    private Query checksumQuery(String checksum) {
        return query(where("CHECK_SUM").is(checksum));
    }

    /**
     * Ignore the current id receipt and see if there is another receipt with similar checksum exists
     *
     * @param checksum
     * @param id
     * @return
     */
    private Query checksumQueryIfDuplicateExists(String checksum, String id) {
        Query query = checksumQuery(checksum)
                .addCriteria(isNotDeleted()
                        .orOperator(
                                where("DS_E").is(DocumentStatusEnum.TURK_REQUEST.getName()),
                                where("DS_E").is(DocumentStatusEnum.TURK_PROCESSED.getName()),
                                where("A").is(true),
                                where("A").is(false)
                        )
                );

        if(!StringUtils.isBlank(id)) {
            //id is blank for new document; whereas for re-check id is always present
            //in such a scenario use method --> hasRecordWithSimilarChecksum
            query.addCriteria(where("id").ne(id));
        }

        return query;
    }
}
