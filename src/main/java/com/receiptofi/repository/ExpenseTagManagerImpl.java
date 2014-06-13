package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.ExpenseTagEntity;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

/**
 * User: hitender
 * Date: 5/13/13
 * Time: 11:59 PM
 */
@Repository
public final class ExpenseTagManagerImpl implements ExpenseTagManager {
    private static final Logger log = LoggerFactory.getLogger(ExpenseTagManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(ExpenseTagEntity.class, Document.class, "collection");

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<ExpenseTagEntity> getAllObjects() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void save(ExpenseTagEntity object) {
        try {
            if(object.getId() != null) {
                object.setUpdated();
            }
            mongoTemplate.save(object, TABLE);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate record entry for ExpenseType={}", e);
            throw new RuntimeException("Expense Name: " + object.getTagName() + ", already exists");
        }
    }

    @Override
    public ExpenseTagEntity findOne(String id) {
        return mongoTemplate.findOne(query(where("id").is(id)), ExpenseTagEntity.class, TABLE);
    }

    @Override
    public void deleteHard(ExpenseTagEntity object) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<ExpenseTagEntity> allExpenseTypes(String userProfileId) {
        return mongoTemplate.find(
                query(where("RID").is(userProfileId)).with(new Sort(ASC, "TAG")),
                ExpenseTagEntity.class,
                TABLE
        );
    }

    @Override
    public List<ExpenseTagEntity> activeExpenseTypes(String receiptUserId) {
        return mongoTemplate.find(
                query(where("RID").is(receiptUserId)
                        .andOperator(
                                isActive(),
                                isNotDeleted()
                        )
                ).with(new Sort(ASC, "TAG")),
                ExpenseTagEntity.class,
                TABLE
        );
    }

    @Override
    public void changeVisibility(String expenseTypeId, boolean changeTo, String receiptUserId) {
        Query query = query(where("id").is(new ObjectId(expenseTypeId)).and("RID").is(receiptUserId));
        Update update = update("A", changeTo);

        //TODO try using writeResult to check for condition
        WriteResult writeResult = mongoTemplate.updateFirst(query, entityUpdate(update), ExpenseTagEntity.class);
        log.info("changeVisibility WriteResult: ", writeResult);
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }
}