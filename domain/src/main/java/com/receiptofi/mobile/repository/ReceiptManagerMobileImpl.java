package com.receiptofi.mobile.repository;

import static com.receiptofi.repository.util.AppendAdditionalFields.isActive;
import static com.receiptofi.repository.util.AppendAdditionalFields.isNotDeleted;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.ReceiptEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 4/7/15 7:38 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Repository
public class ReceiptManagerMobileImpl implements ReceiptManagerMobile {
    private static final Logger LOG = LoggerFactory.getLogger(ReceiptManagerMobileImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(
            ReceiptEntity.class,
            Document.class,
            "collection");

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ReceiptManagerMobileImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<ReceiptEntity> getAllReceipts(String receiptUserId) {
        return mongoTemplate.find(
                query(where("RID").is(receiptUserId)
                        .andOperator(
                                isActive(),
                                isNotDeleted()
                        )
                ).with(new Sort(DESC, "RTXD").and(new Sort(DESC, "C"))),
                ReceiptEntity.class,
                TABLE);
    }

    @Override
    public List<ReceiptEntity> getAllUpdatedReceiptSince(String receiptUserId, Date since) {
        return mongoTemplate.find(
                query(where("RID").is(receiptUserId).and("U").gte(since))
                        .with(new Sort(DESC, "RTXD").and(new Sort(DESC, "C"))),
                ReceiptEntity.class,
                TABLE
        );
    }

    @Override
    public List<ReceiptEntity> getRecentReceipts(int limit) {
        return mongoTemplate.find(
                query(new Criteria()
                        .andOperator(
                                isActive(),
                                isNotDeleted()
                        )
                ).with(new Sort(DESC, "U")).limit(limit),
                ReceiptEntity.class,
                TABLE);
    }

    @Override
    public ReceiptEntity findReceipt(String id) {
        Assert.hasText(id, "Id is empty");
        return mongoTemplate.findOne(
                query(where("id").is(id)),
                ReceiptEntity.class,
                TABLE);
    }
}
