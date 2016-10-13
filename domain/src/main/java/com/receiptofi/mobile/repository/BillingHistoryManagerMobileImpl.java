package com.receiptofi.mobile.repository;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.BillingHistoryEntity;
import com.receiptofi.repository.BillingHistoryManager;

import org.apache.commons.lang3.StringUtils;

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

    private final MongoTemplate mongoTemplate;
    private final BillingHistoryManager billingHistoryManager;

    @Autowired
    public BillingHistoryManagerMobileImpl(MongoTemplate mongoTemplate, BillingHistoryManager billingHistoryManager) {
        this.mongoTemplate = mongoTemplate;
        this.billingHistoryManager = billingHistoryManager;
    }

    @Override
    public List<BillingHistoryEntity> getHistory(String rid, Date since) {
        return mongoTemplate.find(
                query(where("RID").is(rid).and("U").gte(since)).with(new Sort(DESC, "BM")),
                BillingHistoryEntity.class
        );
    }

    @Override
    public List<BillingHistoryEntity> getHistory(String rid) {
        return mongoTemplate.find(
                query(where("RID").is(rid)).with(new Sort(DESC, "BM")),
                BillingHistoryEntity.class
        );
    }

    @Override
    public BillingHistoryEntity getHistory(String rid, String yyyyMM) {
        return mongoTemplate.findOne(
                query(where("RID").is(rid).and("BM").is(yyyyMM)).with(new Sort(DESC, "U")),
                BillingHistoryEntity.class
        );
    }

    @Override
    public void save(BillingHistoryEntity billingHistory) {
        billingHistoryManager.save(billingHistory);
    }

    @Override
    public void deleteHard(BillingHistoryEntity billingHistory) {
        if (StringUtils.isBlank(billingHistory.getTransactionId())) {
            billingHistoryManager.deleteHard(billingHistory);
        } else {
            LOG.error("Should not be deleting billingHistory with rid={} transactionStatus={} transactionId={}",
                    billingHistory.getRid(), billingHistory.getTransactionStatus(), billingHistory.getTransactionId());
        }
    }
}
