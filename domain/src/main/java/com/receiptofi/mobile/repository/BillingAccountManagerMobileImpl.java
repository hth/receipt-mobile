package com.receiptofi.mobile.repository;

import static com.receiptofi.repository.util.AppendAdditionalFields.isActive;
import static org.springframework.data.domain.Sort.Direction;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.BillingAccountEntity;
import com.receiptofi.repository.BillingAccountManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;

/**
 * User: hitender
 * Date: 4/19/15 3:59 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Repository
public class BillingAccountManagerMobileImpl implements BillingAccountManagerMobile {
    private static final Logger LOG = LoggerFactory.getLogger(BillingAccountManagerMobileImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(
            BillingAccountEntity.class,
            Document.class,
            "collection");

    private final MongoTemplate mongoTemplate;
    private final BillingAccountManager billingAccountManager;

    @Autowired
    public BillingAccountManagerMobileImpl(MongoTemplate mongoTemplate, BillingAccountManager billingAccountManager) {
        this.mongoTemplate = mongoTemplate;
        this.billingAccountManager = billingAccountManager;
    }

    @Override
    public BillingAccountEntity getLatestBillingAccount(String rid) {
        return mongoTemplate.findOne(
                query(where("RID").is(rid)
                                .andOperator(
                                        isActive()
                                )
                ).with(new Sort(Direction.DESC, "C")),
                BillingAccountEntity.class
        );
    }

    @Override
    public void save(BillingAccountEntity billingAccount) {
        billingAccountManager.save(billingAccount);
    }
}
