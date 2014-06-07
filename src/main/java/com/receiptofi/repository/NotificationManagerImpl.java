package com.receiptofi.repository;


import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.NotificationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.isNotDeleted;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

/**
 * User: hitender
 * Date: 6/30/13
 * Time: 1:38 PM
 */
@Repository
public final class NotificationManagerImpl implements NotificationManager {
    private static final Logger log = LoggerFactory.getLogger(NotificationManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(NotificationEntity.class, Document.class, "collection");

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<NotificationEntity> getAllObjects() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void save(NotificationEntity object) {
        if(object.getId() != null) {
            object.setUpdated();
        }
        mongoTemplate.save(object, TABLE);
    }

    @Override
    public NotificationEntity findOne(String id) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public WriteResult updateObject(String id, String name) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void deleteHard(NotificationEntity object) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }

    @Override
    public List<NotificationEntity> getAllNotification(String userProfileId, int limit) {
        Criteria criteria1 = where("USER_PROFILE_ID").is(userProfileId);
        Criteria criteria2 = where("NOTIFIED").is(true);
        Sort sort = new Sort(Sort.Direction.DESC, "C");
        Query query = query(criteria1).addCriteria(criteria2).addCriteria(isNotDeleted()).with(sort);
        if(limit != NotificationManager.ALL) {
            query.limit(limit);
        }
        return mongoTemplate.find(query, NotificationEntity.class, TABLE);
    }
}
