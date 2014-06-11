/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.UserProfileEntity;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.isActive;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author hitender
 * @since Dec 23, 2012 3:45:47 AM
 *
 */
@Repository
public final class UserProfileManagerImpl implements UserProfileManager {
	private static final Logger log = LoggerFactory.getLogger(UserProfileManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(UserProfileEntity.class, Document.class, "collection");

	private MongoTemplate mongoTemplate;

    @Autowired
    public UserProfileManagerImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

	@Override
	public List<UserProfileEntity> getAllObjects() {
		return mongoTemplate.findAll(UserProfileEntity.class, TABLE);
	}

	@Override
	public void save(UserProfileEntity object) {
		mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
		try {
//            if(findByEmail(object.getEmailId()) == null)
//			    mongoTemplate.save(object, TABLE);
//            else {
//                log.error("User seems to be already registered: " + object.getEmailId());
//                throw new Exception("User already registered with email: " + object.getEmailId());
//            }
            if(object.getId() != null) {
                object.setUpdated();
            }
            mongoTemplate.save(object, TABLE);
		} catch (DataIntegrityViolationException e) {
			log.error("Duplicate record entry for UserProfileEntity={}", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public UserProfileEntity getObjectUsingUserAuthentication(UserAuthenticationEntity object) {
		return mongoTemplate.findOne(query(where("USER_AUTHENTICATION.$id").is(new ObjectId(object.getId()))), UserProfileEntity.class, TABLE);
	}

	@Override
	public UserProfileEntity findByEmail(String emailId) {
		return mongoTemplate.findOne(query(where("EM").is(emailId).andOperator(isActive())), UserProfileEntity.class, TABLE);
	}

    @Override
    public UserProfileEntity findByReceiptUserId(String receiptUserId) {
        return mongoTemplate.findOne(byReceiptUserId(receiptUserId, true), UserProfileEntity.class, TABLE);
    }

    @Override
    public UserProfileEntity forProfilePreferenceFindByReceiptUserId(String receiptUserId) {
        return mongoTemplate.findOne(byReceiptUserId(receiptUserId, false), UserProfileEntity.class, TABLE);
    }

    private Query byReceiptUserId(String receiptUserId, boolean activeProfile) {
        if(activeProfile) {
            return query(where("RID").is(receiptUserId).andOperator(isActive()));
        } else {
            return query(where("RID").is(receiptUserId));
        }
    }

    @Override
    public UserProfileEntity findByUserId(String email) {
        return mongoTemplate.findOne(query(where("UID").is(email).andOperator(isActive())), UserProfileEntity.class, TABLE);
    }

	@Override
	public UserProfileEntity findOne(String id) {
		return mongoTemplate.findOne(query(where("id").is(id)), UserProfileEntity.class, TABLE);
	}

	@Override
    public void deleteHard(UserProfileEntity object) {
		mongoTemplate.remove(object, TABLE);
	}

	@Override
	public List<UserProfileEntity> searchAllByName(String name) {
		//TODO look into PageRequest for limit data
		//PageRequest request = new PageRequest(0, 1, new Sort("created", Directions.DESC));

        //Can add "^" + to force search only the names starting with
		Criteria a = where("FN").regex(name, "i");
		Criteria b = where("LN").regex(name, "i");
		return mongoTemplate.find(query(new Criteria().orOperator(a, b)), UserProfileEntity.class, TABLE);
	}

    @Override
    public UserProfileEntity findOneByMail(String mail) {
        return mongoTemplate.findOne(query(where("EM").is(mail)), UserProfileEntity.class, TABLE);
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }
}
