/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.utils.DateUtil;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import static com.receiptofi.repository.util.AppendAdditionalFields.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import org.joda.time.DateTime;

import com.mongodb.WriteResult;

/**
 * @author hitender
 * @since Dec 26, 2012 9:16:44 PM
 *
 */
@Repository
public final class ItemManagerImpl implements ItemManager {
	private static final Logger log = LoggerFactory.getLogger(ItemManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(ItemEntity.class, Document.class, "collection");

	@Autowired private MongoTemplate mongoTemplate;
    @Autowired private BizNameManager bizNameManager;

	@Override
	public List<ItemEntity> getAllObjects() {
		return mongoTemplate.findAll(ItemEntity.class, TABLE);
	}

	@Override
	public void save(ItemEntity object) {
		mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
		try {
            if(object.getId() != null) {
                object.setUpdated();
            }
            mongoTemplate.save(object, TABLE);
		} catch (DataIntegrityViolationException e) {
			log.error("Duplicate record entry for ItemEntity={}", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void saveObjects(List<ItemEntity> objects) throws Exception {
		mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
		try {
			//TODO reflection error saving the list
			//mongoTemplate.insert(objects, TABLE);
            int sequence = 1;
			for(ItemEntity object : objects) {
                object.setSequence(sequence);
				save(object);
                sequence ++;
			}
		} catch (DataIntegrityViolationException e) {
			log.error("Duplicate record entry for ItemEntity: " + e.getLocalizedMessage());
			throw new Exception(e.getMessage());
		}
	}

    /**
     * User findItem instead of findOne as this is not a secure call without user profile id
     *
     * @param id
     * @return
     */
    @Deprecated
	@Override
	public ItemEntity findOne(String id) {
		Sort sort = new Sort(Direction.ASC, "SEQUENCE");
		return mongoTemplate.findOne(query(where("id").is(id)).with(sort), ItemEntity.class, TABLE);
	}

    /**
     * Use this method instead of findOne
     *
     * @param itemId
     * @param userProfileId
     * @return
     */
    @Override
    public ItemEntity findItem(String itemId, String userProfileId) {
        Query query = query(where("id").is(itemId).and("USER_PROFILE_ID").is(userProfileId));
        return mongoTemplate.findOne(query, ItemEntity.class, TABLE);
    }

	@Override
	public List<ItemEntity> getWhereReceipt(ReceiptEntity receipt) {
		Sort sort = new Sort(Direction.ASC, "SEQUENCE");
		return mongoTemplate.find(query(where("RECEIPT.$id").is(new ObjectId(receipt.getId()))).with(sort), ItemEntity.class, TABLE);
	}

    /**
     * This method in future could be very memory extensive when there would be tons of similar items. To fix it, add
     * receipt date to items
     *
     * db.ITEM.find( {"name" : "509906212284 Podium Bottle 24 oz" , "created" : ISODate("2013-06-03T03:38:44.818Z")} )
     *
     * @param name - Name of the item
     * @param untilThisDay - Show result from this day onwards
     * @return
     */
	@Override
	public List<ItemEntity> findAllByNameLimitByDays(String name, DateTime untilThisDay) {
        return findAllByNameLimitByDays(name, null, untilThisDay);
	}

    /**
     * This method in future could be very memory extensive when there would be tons of similar items. To fix it, add
     * receipt date to items
     *
     * db.ITEM.find( {"name" : "509906212284 Podium Bottle 24 oz" , "created" : ISODate("2013-06-03T03:38:44.818Z")} )
     *
     * @param name - Name of the item
     * @param untilThisDay - Show result from this day onwards
     * @return
     */
    @Override
    public List<ItemEntity> findAllByNameLimitByDays(String name, String userProfileId, DateTime untilThisDay) {
        // Can choose Item create date but if needs accuracy then find receipts for these items and filter receipts by date provided.
        // Not sure how much beneficial it would be other than more data crunching.
        Criteria criteriaA = where("NAME").is(name);
        Query query = query(criteriaA);

        Criteria criteriaB;
        if(userProfileId != null) {
            criteriaB = where("USER_PROFILE_ID").is(userProfileId);
            query = query(criteriaA.andOperator(criteriaB.andOperator(isNotDeleted())));
        }

        return mongoTemplate.find(query, ItemEntity.class, TABLE);
    }

    /**
     * This method in future could be very memory extensive when there would be tons of similar items. To fix it, add
     * receipt date to items
     *
     * @param itemEntity
     * @param userProfileId
     * @return
     */
    @Override
    public List<ItemEntity> findAllByName(ItemEntity itemEntity, String userProfileId) {
        if(itemEntity.getReceipt().getUserProfileId().equals(userProfileId)) {
            Criteria criteria = where("NAME").is(itemEntity.getName())
                    .and("USER_PROFILE_ID").is(userProfileId)
                    .andOperator(
                            isNotDeleted()
                    );

            return mongoTemplate.find(query(criteria), ItemEntity.class, TABLE);
        } else {
            log.error("One of the query is trying to get items for different User Profile Id: " + userProfileId + ", Item Id: " + itemEntity.getId());
            return new LinkedList<>();
        }
    }

	@Override
	public void deleteHard(ItemEntity object) {
		mongoTemplate.remove(object, TABLE);
	}

	@Override
	public WriteResult updateObject(ItemEntity object) {
		Query query = query(where("id").is(object.getId()));
		Update update = Update.update("NAME", object.getName());
		return mongoTemplate.updateFirst(query, entityUpdate(update), TABLE);
	}

	@Override
	public void deleteWhereReceipt(ReceiptEntity receipt) {
		mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
		mongoTemplate.remove(query(where("RECEIPT.$id").is(new ObjectId(receipt.getId()))), ItemEntity.class);
	}

    @Override
    public void deleteSoft(ReceiptEntity receipt) {
        mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
        Query query = query(where("RECEIPT.$id").is(new ObjectId(receipt.getId())));
        Update update = Update.update("D", true);
        mongoTemplate.updateMulti(query, entityUpdate(update), ItemEntity.class);
    }

    @Override
    public List<ItemEntity> findItems(String name, String bizName) {
        Criteria criteriaI = where("NAME").regex(new StringTokenizer("^" + name).nextToken(), "i");
        Query query;

        BizNameEntity bizNameEntity = bizNameManager.findOneByName(bizName);
        if(bizNameEntity == null) {
            //query = Query.query(criteriaI);
            return new ArrayList<>();
        } else {
            Criteria criteriaB = where("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()));
            query = query(criteriaI).addCriteria(criteriaB);
        }

        //This makes just one of the field populated
        query.fields().include("NAME");
        return mongoTemplate.find(query, ItemEntity.class, TABLE);
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }

    @Override
    public void updateItemWithExpenseType(ItemEntity item) throws Exception {
        ItemEntity foundItem = findOne(item.getId());
        if(foundItem == null) {
            log.error("Could not update ExpenseType as no ItemEntity with Id was found: " + item.getId());
            throw new Exception("Could not update ExpenseType as no ItemEntity with Id was found: " + item.getId());
        } else {
            foundItem.setExpenseTag(item.getExpenseTag());
            save(foundItem);
        }
    }

    @Override
    public long countItemsUsingExpenseType(String expenseTypeId, String userProfileId) {
        Criteria criteria = where("ET_R.$id").is(new ObjectId(expenseTypeId))
                .and("USER_PROFILE_ID").is(userProfileId);

        Query query = query(criteria).addCriteria(isActive()).addCriteria(isNotDeleted());
        return mongoTemplate.count(query, ItemEntity.class);
    }

    /**
     * Example to fetch Entity based on DBRef
     *      db.ITEM.find( {'expenseType.$id':  ObjectId('51a6d366036487b899cc31fc')} )
     *
     * @param expenseType
     * @return
     */
    @Override
    public List<ItemEntity> getItemEntitiesForSpecificExpenseTypeForTheYear(ExpenseTagEntity expenseType) {
        Criteria criteria = where("ET_R.$id").is(new ObjectId(expenseType.getId()))
                .and("R_D").gte(DateUtil.startOfYear());

        Query query = query(criteria).addCriteria(isActive()).addCriteria(isNotDeleted());
        return mongoTemplate.find(query, ItemEntity.class);
    }

    @Override
    public List<ItemEntity> getItemEntitiesForUnAssignedExpenseTypeForTheYear(String userProfileId) {
        Criteria criteria = where("ET_R").is(StringUtils.trimToNull(null))
                .and("USER_PROFILE_ID").is(userProfileId)
                .and("R_D").gte(DateUtil.startOfYear());

        return mongoTemplate.find(query(criteria).addCriteria(isActive()).addCriteria(isNotDeleted()), ItemEntity.class);
    }
}
