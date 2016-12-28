package com.receiptofi.mobile.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.PaymentCardEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 9/23/16 5:24 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Repository
public class PaymentCardManageMobileImpl implements PaymentCardManagerMobile {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentCardManageMobileImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(
            PaymentCardEntity.class,
            Document.class,
            "collection");

    private final MongoTemplate mongoTemplate;

    @Autowired
    public PaymentCardManageMobileImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public PaymentCardEntity findOne(String id, String rid) {
        return mongoTemplate.findOne(
                query(where("id").is(id).and("RID").is(rid)),
                PaymentCardEntity.class,
                TABLE
        );
    }

    @Override
    public List<PaymentCardEntity> getAllPaymentCards(String rid) {
        return mongoTemplate.find(
                query(where("RID").is(rid)),
                PaymentCardEntity.class,
                TABLE
        );
    }

    public List<PaymentCardEntity> getUpdatedSince(String rid, Date since) {
        return mongoTemplate.find(
                query(where("RID").is(rid).and("U").gte(since)),
                PaymentCardEntity.class,
                TABLE
        );
    }
}
