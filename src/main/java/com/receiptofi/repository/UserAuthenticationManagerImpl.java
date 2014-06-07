/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.entityUpdate;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

/**
 * @author hitender
 * @since Dec 16, 2012 1:20:53 PM
 */
@Repository
public final class UserAuthenticationManagerImpl implements UserAuthenticationManager {
	private static final Logger log = LoggerFactory.getLogger(UserAuthenticationManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(UserAuthenticationEntity.class, Document.class, "collection");

	private MongoTemplate mongoTemplate;

    @Autowired
    public UserAuthenticationManagerImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

	@Override
	public List<UserAuthenticationEntity> getAllObjects() {
		return mongoTemplate.findAll(UserAuthenticationEntity.class, TABLE);
	}

	@Override
	public void save(UserAuthenticationEntity object) {
		mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
		try {
            if(object.getId() != null) {
                object.setUpdated();
            }
            mongoTemplate.save(object, TABLE);
		} catch (DataIntegrityViolationException e) {
			log.error("Duplicate record entry for UserAuthenticationEntity:{} {}", e.getLocalizedMessage(), e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public UserAuthenticationEntity findOne(String id) {
		return mongoTemplate.findOne(query(Criteria.where("id").is(id)), UserAuthenticationEntity.class, TABLE);
	}

	@Override
	public WriteResult updateObject(String id, String name) {
		return mongoTemplate.updateFirst(query(Criteria.where("id").is(id)), entityUpdate(update("NAME", name)), TABLE);
	}

	@Override
	public void deleteHard(UserAuthenticationEntity object) {
		mongoTemplate.remove(object, TABLE);
	}

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }
}
