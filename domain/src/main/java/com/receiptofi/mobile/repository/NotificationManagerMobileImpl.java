package com.receiptofi.mobile.repository;

import static com.receiptofi.repository.util.AppendAdditionalFields.isNotDeleted;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.NotificationEntity;
import com.receiptofi.domain.types.NotificationMarkerEnum;

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
 * Date: 4/7/15 8:39 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Repository
public class NotificationManagerMobileImpl implements NotificationManagerMobile {
    private static final Logger LOG = LoggerFactory.getLogger(ReceiptManagerMobileImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(
            NotificationEntity.class,
            Document.class,
            "collection");

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<NotificationEntity> getNotifications(String rid, Date since) {
        return mongoTemplate.find(
                query(where("RID").is(rid).and("NM").ne(NotificationMarkerEnum.I).and("U").gte(since))
                        .addCriteria(isNotDeleted())
                        .with(new Sort(Sort.Direction.DESC, "C")),
                NotificationEntity.class
        );
    }
}
