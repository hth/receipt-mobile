package com.receiptofi.mobile.repository;



import static com.receiptofi.repository.util.AppendAdditionalFields.isActive;
import static com.receiptofi.repository.util.AppendAdditionalFields.isNotDeleted;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.CouponEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 5/9/16 10:38 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Repository
public class CouponManagerMobileImpl implements CouponManagerMobile {
    private static final Logger LOG = LoggerFactory.getLogger(ExpenseTagManagerMobileImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(
            CouponEntity.class,
            Document.class,
            "collection");

    private MongoTemplate mongoTemplate;

    @Autowired
    public CouponManagerMobileImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<CouponEntity> findAll(String rid) {
        return mongoTemplate.find(
                Query.query(
                        where("RID").is(rid)
                                .andOperator(
                                        isNotDeleted()
                                )
                ),
                CouponEntity.class,
                TABLE);
    }

    @Override
    public List<CouponEntity> getCouponUpdateSince(String rid, Date since) {
        return mongoTemplate.find(
                Query.query(
                        where("RID").is(rid).and("U").gte(since)
                                .andOperator(
                                        isNotDeleted()
                                )
                ),
                CouponEntity.class,
                TABLE);
    }

    @Override
    public CouponEntity findOne(String couponId, String rid) {
        return mongoTemplate.findOne(
                Query.query(
                        where("id").is(couponId).and("RID").is(rid)
                                .andOperator(
                                        isNotDeleted()
                                )
                ),
                CouponEntity.class,
                TABLE);
    }
}
