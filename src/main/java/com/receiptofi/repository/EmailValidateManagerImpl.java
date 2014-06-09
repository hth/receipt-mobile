package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.EmailValidateEntity;
import com.receiptofi.domain.UserProfileEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * User: hitender
 * Date: 5/17/14 6:29 PM
 */
@Repository
public final class EmailValidateManagerImpl implements EmailValidateManager {
    private static final Logger log = LoggerFactory.getLogger(EmailValidateManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(EmailValidateEntity.class, Document.class, "collection");

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<EmailValidateEntity> getAllObjects() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void save(EmailValidateEntity object) {
        mongoTemplate.save(object);
    }

    @Override
    public EmailValidateEntity findOne(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteHard(EmailValidateEntity object) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long collectionSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EmailValidateEntity findByAuthenticationKey(String auth) {
        Query query = query(where("AUTH").is(auth)).addCriteria(isActive()).addCriteria(isNotDeleted());
        return mongoTemplate.findOne(query, EmailValidateEntity.class, TABLE);
    }

    @Override
    public void invalidateAllEntries(EmailValidateEntity object) {
        mongoTemplate.updateMulti(
                query(where("RID").is(object.getReceiptUserId())),
                entityUpdate(update("A", false)),
                EmailValidateEntity.class
        );
    }

    @Override
    public EmailValidateEntity reInviteActiveInvite(String email, UserProfileEntity invitedBy) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EmailValidateEntity find(String email) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
