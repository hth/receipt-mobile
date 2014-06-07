package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.MileageEntity;

import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.isActive;
import static com.receiptofi.repository.util.AppendAdditionalFields.isNotDeleted;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import org.joda.time.DateTime;

import com.mongodb.WriteResult;

/**
 * User: hitender
 * Date: 12/25/13 4:16 AM
 */
public class MileageManagerImpl implements MileageManager {
    private static final String TABLE = BaseEntity.getClassAnnotationValue(MileageEntity.class, Document.class, "collection");

    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<MileageEntity> getAllObjects() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void save(MileageEntity object) {
        if(object.getId() != null) {
            object.setUpdated();
        }
        mongoTemplate.save(object, TABLE);
    }

    @Override
    public MileageEntity findOne(String id) {
        return mongoTemplate.findOne(query(where("id").is(id)), MileageEntity.class, TABLE);
    }

    @Override
    public MileageEntity findOne(String id, String userProfileId) {
        return mongoTemplate.findOne(query(where("id").is(id)).addCriteria(where("USER_PROFILE_ID").is(userProfileId)), MileageEntity.class, TABLE);
    }

    @Override
    public WriteResult updateObject(String id, String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteHard(MileageEntity object) {
        mongoTemplate.remove(object);
    }

    @Override
    public long collectionSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<MileageEntity> getMileageForThisMonth(String userProfileId, DateTime startMonth, DateTime endMonth) {
        Criteria criteria = where("USER_PROFILE_ID").is(userProfileId).and("C").gte(startMonth.toDate()).lt(endMonth.toDate());

        Sort sort = new Sort(Sort.Direction.DESC, "S");
        Query query = query(criteria).addCriteria(isActive()).addCriteria(isNotDeleted());
        return mongoTemplate.find(query.with(sort), MileageEntity.class, TABLE);
    }

    @Override
    public boolean updateStartDate(String mileageId, DateTime startDate, String userProfileId) {
        return updateDateInRecord(mileageId, "SD", startDate, userProfileId).getLastError().ok();
    }

    @Override
    public boolean updateEndDate(String mileageId, DateTime endDate, String userProfileId) {
        return updateDateInRecord(mileageId, "ED", endDate, userProfileId).getLastError().ok();
    }

    private WriteResult updateDateInRecord(String mileageId, String fieldName, DateTime dateTime, String userProfileId) {
        return mongoTemplate.updateFirst(
                query(where("id").is(mileageId).and("USER_PROFILE_ID").is(userProfileId)),
                update(fieldName, dateTime.toDate()),
                MileageEntity.class
        );
    }
}
