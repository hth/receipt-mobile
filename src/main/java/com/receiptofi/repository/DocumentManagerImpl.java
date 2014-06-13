/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.types.DocumentStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author hitender
 * @since Jan 6, 2013 1:29:44 PM
 *
 */
@Repository
public final class DocumentManagerImpl implements DocumentManager {
	private static final Logger log = LoggerFactory.getLogger(ReceiptManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(DocumentEntity.class, Document.class, "collection");

	@Autowired private MongoTemplate mongoTemplate;

	@Override
	public List<DocumentEntity> getAllObjects() {
		return mongoTemplate.findAll(DocumentEntity.class, TABLE);
	}

	//TODO invoke transaction here
	@Override
	public void save(DocumentEntity object) {
		mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
		try {
			// Cannot use insert because insert does not perform update like save.
			// Save will always try to update or create new record.
			// mongoTemplate.insert(object, TABLE);

            if(object.getId() != null) {
                object.setUpdated();
            }
			mongoTemplate.save(object, TABLE);
		} catch (DataIntegrityViolationException e) {
			log.error("Duplicate record entry for DocumentEntity={}", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public DocumentEntity findOne(String id) {
		throw new UnsupportedOperationException("This method is not supported");
	}

    @Override
    public DocumentEntity findOne(String id, String userProfileId) {
        Query query = query(where("id").is(id).and("USER_PROFILE_ID").is(userProfileId));
        return mongoTemplate.findOne(query, DocumentEntity.class, TABLE);
    }

    @Override
    public DocumentEntity findActiveOne(String id) {
        Query query = query(where("id").is(id)).addCriteria(isActive());
        return mongoTemplate.findOne(query, DocumentEntity.class, TABLE);
    }

	@Override
	public void deleteHard(DocumentEntity object) {
		mongoTemplate.remove(object, TABLE);
	}

	@Override
	public long numberOfPendingReceipts(String userProfileId) {
        Criteria criteria1 = where("USER_PROFILE_ID").is(userProfileId);
        Query query = query(criteria1).addCriteria(isActive()).addCriteria(isNotDeleted());
		return mongoTemplate.count(query, TABLE);
	}

	@Override
	public List<DocumentEntity> getAllPending(String userProfileId) {
        Criteria criteria1 = where("USER_PROFILE_ID").is(userProfileId);
        Query query = query(criteria1).addCriteria(isActive()).addCriteria(isNotDeleted());

        Sort sort = new Sort(Direction.ASC, "C");
		return mongoTemplate.find(query.with(sort), DocumentEntity.class, TABLE);
	}

    @Override
    public List<DocumentEntity> getAllRejected(String userProfileId) {
        Query query = query(
                where("USER_PROFILE_ID").is(userProfileId)
                .and("DS_E").is(DocumentStatusEnum.TURK_RECEIPT_REJECT)
        ).addCriteria(isNotActive()).addCriteria(isDeleted());

        Sort sort = new Sort(Direction.ASC, "C");
        return mongoTemplate.find(query.with(sort), DocumentEntity.class, TABLE);
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }
}
