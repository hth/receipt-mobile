package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;
import org.bson.types.ObjectId;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * User: hitender
 * Date: 4/22/13
 * Time: 11:21 PM
 */
@Repository
public final class BizStoreManagerImpl implements BizStoreManager {
    private static final String TABLE = BaseEntity.getClassAnnotationValue(BizStoreEntity.class, Document.class, "collection");

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<BizStoreEntity> getAllObjects() {
        return mongoTemplate.findAll(BizStoreEntity.class, TABLE);
    }

    @Override
    public void save(BizStoreEntity object) {
        mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
        if(object.getId() != null) {
            object.setUpdated();
        }
        mongoTemplate.save(object, TABLE);
    }

    @Override
    public BizStoreEntity findOne(String id) {
        return mongoTemplate.findOne(query(where("id").is(id)), BizStoreEntity.class, TABLE);
    }

    @Override
    public void deleteHard(BizStoreEntity object) {
        mongoTemplate.remove(object);
    }

    public BizStoreEntity noStore() {
        return mongoTemplate.findOne(query(where("ADDRESS").is(StringUtils.EMPTY)), BizStoreEntity.class, TABLE);
    }

    public BizStoreEntity findOne(BizStoreEntity bizStoreEntity) {
        Query query = query(where("ADDRESS").is(bizStoreEntity.getAddress()));

        if(StringUtils.isNotEmpty(bizStoreEntity.getPhone())) {
            query.addCriteria(where("PHONE").is(bizStoreEntity.getPhone()));
        }

        return mongoTemplate.findOne(query, BizStoreEntity.class, TABLE);
    }

    @Override
    public List<BizStoreEntity> findAllWithAnyAddressAnyPhone(String bizAddress, String bizPhone, BizNameEntity bizNameEntity) {
        Criteria criteriaA = new Criteria();
        if(StringUtils.isNotEmpty(bizAddress)) {
            criteriaA.and("ADDRESS").regex(bizAddress, "i");
        }
        if(StringUtils.isNotEmpty(bizPhone)) {
            criteriaA.and("PHONE").regex(bizPhone, "i");
        }

        if(bizNameEntity != null && StringUtils.isNotEmpty(bizNameEntity.getId())) {
            Criteria criteriaB = where("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()));
            return mongoTemplate.find(query(criteriaB).addCriteria(criteriaA).limit(STORE_LIMIT), BizStoreEntity.class, TABLE);
        } else {
            return mongoTemplate.find(query(criteriaA).limit(STORE_LIMIT), BizStoreEntity.class, TABLE);
        }
    }

    @Override
    public List<BizStoreEntity> findAllWithStartingAddressStartingPhone(String bizAddress, String bizPhone, BizNameEntity bizNameEntity) {
        Query query = null;
        if(StringUtils.isNotEmpty(bizAddress)) {
            query = query(where("ADDRESS").regex("^" + bizAddress, "i"));
        }
        if(StringUtils.isNotEmpty(bizPhone)) {
            Criteria criteria = where("PHONE").regex("^" + bizPhone, "i");
            if(query == null) {
                query = query(criteria);
            } else {
                query.addCriteria(criteria);
            }
        }

        if(bizNameEntity != null && StringUtils.isNotEmpty(bizNameEntity.getId())) {
            Criteria criteriaA = where("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()));
            if(query != null) {
                query.addCriteria(criteriaA);
            } else {
                query = query(criteriaA);
            }
        }
        return mongoTemplate.find(query.limit(STORE_LIMIT), BizStoreEntity.class, TABLE);
    }

    @Override
    public List<BizStoreEntity> getAllWithJustSpecificField(String bizAddress, BizNameEntity bizNameEntity, String fieldName) {
        Query query;
        if(StringUtils.isBlank(bizAddress)) {
            Criteria criteriaB = where("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()));

            query = query(criteriaB);
        } else {
            Criteria criteriaA = where("ADDRESS").regex("^" + bizAddress, "i");
            Criteria criteriaB = where("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()));

            query = query(criteriaB).addCriteria(criteriaA);
        }
        query.fields().include(fieldName);
        return mongoTemplate.find(query, BizStoreEntity.class, TABLE);
    }

    @Override
    public List<BizStoreEntity> getAllWithJustSpecificField(String bizPhone, String bizAddress, BizNameEntity bizNameEntity, String fieldName) {
        Query query;
        if(StringUtils.isBlank(bizPhone)) {
            Criteria criteriaB = where("ADDRESS").is(bizAddress);
            Criteria criteriaC = where("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()));

            query = query(criteriaC).addCriteria(criteriaB);
        } else {
            Criteria criteriaA = where("PHONE").regex("^" + bizPhone, "i");
            Criteria criteriaB = where("ADDRESS").is(bizAddress);
            Criteria criteriaC = where("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()));

            query = query(criteriaC).addCriteria(criteriaB).addCriteria(criteriaA);
        }
        query.fields().include(fieldName);
        return mongoTemplate.find(query, BizStoreEntity.class, TABLE);
    }

    @Override
    public List<BizStoreEntity> findAllAddress(BizNameEntity bizNameEntity, int limit) {
        Sort sort = new Sort(Sort.Direction.DESC, "C");
        return mongoTemplate.find(query(where("BIZ_NAME.$id").is(new ObjectId(bizNameEntity.getId()))).with(sort).limit(limit), BizStoreEntity.class, TABLE);
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }
}
