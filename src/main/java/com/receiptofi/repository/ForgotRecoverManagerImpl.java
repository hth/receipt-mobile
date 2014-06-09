package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.ForgotRecoverEntity;

import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.entityUpdate;
import static com.receiptofi.repository.util.AppendAdditionalFields.isActive;
import static com.receiptofi.repository.util.AppendAdditionalFields.isNotDeleted;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

/**
 * User: hitender
 * Date: 6/4/13
 * Time: 12:10 AM
 */
@Repository
public final class ForgotRecoverManagerImpl implements ForgotRecoverManager {
    private static final String TABLE = BaseEntity.getClassAnnotationValue(ForgotRecoverEntity.class, Document.class, "collection");

    private MongoTemplate mongoTemplate;

    @Autowired
    public ForgotRecoverManagerImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<ForgotRecoverEntity> getAllObjects() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void save(ForgotRecoverEntity object) {
        if(object.getId() != null) {
            object.setUpdated();
        }
        mongoTemplate.save(object, TABLE);
    }

    @Override
    public void invalidateAllEntries(String receiptUserId) {
        Criteria criteria = where("RID").is(receiptUserId);
        mongoTemplate.updateMulti(query(criteria), entityUpdate(update("A", false)), ForgotRecoverEntity.class);
    }

    @Override
    public ForgotRecoverEntity findOne(String id) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public ForgotRecoverEntity findByAuthenticationKey(String key) {
        Criteria criteria = where("AUTH").is(key);
        Query query = query(criteria).addCriteria(isActive()).addCriteria(isNotDeleted());
        return mongoTemplate.findOne(query, ForgotRecoverEntity.class, TABLE);
    }

    @Override
    public void deleteHard(ForgotRecoverEntity object) {
        mongoTemplate.remove(object);
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }
}
