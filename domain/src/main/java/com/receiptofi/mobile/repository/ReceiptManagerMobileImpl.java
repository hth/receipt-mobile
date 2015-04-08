package com.receiptofi.mobile.repository;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.repository.ReceiptManagerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;

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

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<ReceiptEntity> getAllReceipts(String receiptUserId) {
        return mongoTemplate.find(
                query(where("RID").is(receiptUserId))
                        .with(new Sort(DESC, "RTXD").and(new Sort(DESC, "C"))),
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
}
