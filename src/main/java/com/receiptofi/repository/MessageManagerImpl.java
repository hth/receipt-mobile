package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.MessageDocumentEntity;
import com.receiptofi.domain.types.DocumentStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.Order;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

/**
 * User: hitender
 * Date: 4/6/13
 * Time: 7:28 PM
 */
@Repository
public final class MessageManagerImpl implements MessageManager {
    private static final Logger log = LoggerFactory.getLogger(MessageManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(MessageDocumentEntity.class, Document.class, "collection");

    @Value("${messageQueryLimit:10}")
    private int messageQueryLimit;

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<MessageDocumentEntity> getAllObjects() {
        return mongoTemplate.findAll(MessageDocumentEntity.class, TABLE);
    }

    @Override
    public List<MessageDocumentEntity> findWithLimit(DocumentStatusEnum status) {
        return findWithLimit(status, messageQueryLimit);
    }

    @Override
    public List<MessageDocumentEntity> findWithLimit(DocumentStatusEnum status, int limit) {
        Query query = query(where("LOCKED").is(false).and("DS_E").is(status));

        List<Order> orders = new ArrayList<Order>() {{
            add(new Order(DESC, "USER_LEVEL_ENUM"));
            add(new Order(ASC, "C"));
        }};
        Sort sort = new Sort(orders);

        query.with(sort).limit(limit);
        return mongoTemplate.find(query, MessageDocumentEntity.class, TABLE);
    }

    @Override
    public List<MessageDocumentEntity> findUpdateWithLimit(String emailId, String userProfileId, DocumentStatusEnum status) {
        return findUpdateWithLimit(emailId, userProfileId, status, messageQueryLimit);
    }

    @Override
    public List<MessageDocumentEntity> findUpdateWithLimit(String emailId, String userProfileId, DocumentStatusEnum status, int limit) {
//        String updateQuery = "{ " +
//                "set : " +
//                    "{" +
//                    "'emailId' : '" + emailId + "', " +
//                    "'profileId' : '" + profileId + "', " +
//                    "'recordLocked' : " + true +
//                    "} " +
//                "}";
//
//        String sortQuery  = "{ sort : { 'level' : " + -1 + ", 'created' : " + 1 + "} }";
//        String limitQuery = "{ limit : " + messageQueryLimit + "}";

//        BasicDBObject basicDBObject = new BasicDBObject()
//                .append("recordLocked", false)
//                .append("DS_E", "OCR_PROCESSED");

        List<MessageDocumentEntity> list = findWithLimit(status);
        for(MessageDocumentEntity object : list) {
            object.setEmailId(emailId);
            object.setUserProfileId(userProfileId);
            object.setRecordLocked(true);
            try {
                save(object);
            } catch (Exception e) {
                object.setRecordLocked(false);
                object.setUserProfileId(StringUtils.EMPTY);
                object.setEmailId(StringUtils.EMPTY);
                try {
                    save(object);
                } catch (Exception e1) {
                    log.error("Update failed: " + object.toString());
                }
            }
        }

        return list;
    }

    @Override
    public List<MessageDocumentEntity> findPending(String emailId, String userProfileId, DocumentStatusEnum status) {
        Query query = query(where("LOCKED").is(true).and("DS_E").is(status).and("EM").is(emailId).and("USER_PROFILE_ID").is(userProfileId));

        List<Order> orders = new ArrayList<Order>() {{
            add(new Order(DESC, "USER_LEVEL_ENUM"));
            add(new Order(ASC, "C"));
        }};
        Sort sort = new Sort(orders);

        query.with(sort);
        return mongoTemplate.find(query, MessageDocumentEntity.class, TABLE);
    }

    @Override
    public List<MessageDocumentEntity> findAllPending() {
        Query query = query(where("LOCKED").is(true).and("DS_E").is(DocumentStatusEnum.OCR_PROCESSED));

        List<Order> orders = new ArrayList<Order>() {{
            add(new Order(DESC, "USER_LEVEL_ENUM"));
            add(new Order(ASC, "C"));
        }};
        Sort sort = new Sort(orders);

        query.with(sort);
        return mongoTemplate.find(query, MessageDocumentEntity.class, TABLE);
    }

    @Override
    public void save(MessageDocumentEntity object) {
        mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
        if(object.getId() != null) {
            object.setUpdated(); //TODO why force the update date. Should it not be handled by the system just like versioning.
        }
        mongoTemplate.save(object, TABLE);
    }

    @Override
    public MessageDocumentEntity findOne(String id) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public WriteResult updateObject(String receiptOCRId, DocumentStatusEnum statusFind, DocumentStatusEnum statusSet) {
        mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
        Query query = query(where("LOCKED").is(true).and("DS_E").is(statusFind).and("RECEIPT_OCR_ID").is(receiptOCRId));
        Update update = update("DS_E", statusSet).set("A", false);
        return mongoTemplate.updateFirst(query, entityUpdate(update), MessageDocumentEntity.class);
    }

    @Override
    public WriteResult undoUpdateObject(String receiptOCRId, boolean value, DocumentStatusEnum statusFind, DocumentStatusEnum statusSet) {
        mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
        Query query = query(where("LOCKED").is(true).and("DS_E").is(statusFind).and("A").is(false).and("RECEIPT_OCR_ID").is(receiptOCRId));

        Update update = update("recordLocked", false)
                .set("A", true)
                .set("DS_E", statusSet);

        return mongoTemplate.updateFirst(query, entityUpdate(update), MessageDocumentEntity.class);
    }

    @Override
    public void deleteHard(MessageDocumentEntity object) {
        mongoTemplate.remove(object, TABLE);
    }

    @Override
    public void deleteAllForReceiptOCR(String receiptOCRId) {
        Query query = query(where("RECEIPT_OCR_ID").is(receiptOCRId));
        mongoTemplate.remove(query, MessageDocumentEntity.class);
    }

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }
}
