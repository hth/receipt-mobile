package com.receiptofi.mobile.repository;

import static com.receiptofi.repository.util.AppendAdditionalFields.isActive;
import static com.receiptofi.repository.util.AppendAdditionalFields.isNotDeleted;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.ExpenseTagEntity;

import org.bson.types.ObjectId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;

/**
 * User: hitender
 * Date: 4/25/15 3:30 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Repository
public class ExpenseTagManagerMobileImpl implements ExpenseTagManagerMobile {
    private static final Logger LOG = LoggerFactory.getLogger(ExpenseTagManagerMobileImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(
            ExpenseTagEntity.class,
            Document.class,
            "collection");

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public ExpenseTagEntity getExpenseTag(String rid, String tagId, String tagName) {
        return mongoTemplate.findOne(
                query(where("id").is(new ObjectId(tagId))
                                .and("TAG").is(tagName)
                                .and("RID").is(rid)
                                .andOperator(
                                        isActive(),
                                        isNotDeleted()
                                )
                ),
                ExpenseTagEntity.class,
                TABLE
        );
    }

    @Override
    public boolean doesExits(String rid, String tagName) {
        return mongoTemplate.count(
                query(where("RID").is(rid)
                        .and("TAG").is(tagName)
                        .andOperator(
                                isActive(),
                                isNotDeleted()
                        )),
                ExpenseTagEntity.class
        ) > 0;
    }
}
