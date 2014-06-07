package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.CommentEntity;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

/**
 * User: hitender
 * Date: 6/11/13
 * Time: 7:13 PM
 */
@Repository
public final class CommentManagerImpl implements CommentManager {
    private static final String TABLE = BaseEntity.getClassAnnotationValue(CommentEntity.class, Document.class, "collection");

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<CommentEntity> getAllObjects() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * Note: comment should not be marked updated with new time as its already updated by Ajax
     *
     * @param object
     * @throws Exception
     */
    @Override
    public void save(CommentEntity object) {
        //Note: comment should not be marked updated with new time as its already updated by Ajax
//        if(object.getId() != null) {
//            object.setUpdated();
//        }
        mongoTemplate.save(object, TABLE);
    }

    @Override
    public CommentEntity findOne(String id) {
        return mongoTemplate.findOne(query(where("id").is(id)), CommentEntity.class);
    }

    @Override
    public WriteResult updateObject(String id, String name) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void deleteHard(CommentEntity object) {
        mongoTemplate.remove(object);
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }
}
