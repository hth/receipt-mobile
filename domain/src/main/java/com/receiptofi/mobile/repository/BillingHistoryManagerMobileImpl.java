package com.receiptofi.mobile.repository;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.BillingHistoryEntity;

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
 * Date: 4/19/15 4:05 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Repository
public class BillingHistoryManagerMobileImpl implements BillingHistoryManagerMobile {
    private static final Logger LOG = LoggerFactory.getLogger(BillingHistoryManagerMobileImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(
            BillingHistoryEntity.class,
            Document.class,
            "collection");

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<BillingHistoryEntity> getHistory(String rid, Date since) {
        return mongoTemplate.find(
                query(where("RID").is(rid).and("U").gte(since)).with(new Sort(DESC, "BM")),
                BillingHistoryEntity.class);
    }

    @Override
    public List<BillingHistoryEntity> getHistory(String rid) {
        return mongoTemplate.find(
                query(where("RID").is(rid)).with(new Sort(DESC, "BM")),
                BillingHistoryEntity.class
        );
    }
}
