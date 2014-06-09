/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.ItemFeatureEntity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;

/**
 * @author hitender
 * @since Dec 26, 2012 9:21:35 PM
 *
 */
@Repository
public final class ItemFeatureManagerImpl implements ItemFeatureManager {
    private static final String TABLE = BaseEntity.getClassAnnotationValue(ItemFeatureEntity.class, Document.class, "collection");

	@Autowired private MongoTemplate mongoTemplate;

	@Override
	public List<ItemFeatureEntity> getAllObjects() {
		return mongoTemplate.findAll(ItemFeatureEntity.class, TABLE);
	}

	@Override
	public void save(ItemFeatureEntity object) {
        mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
        if(object.getId() != null) {
            object.setUpdated();
        }
		mongoTemplate.save(object, TABLE);
	}

	@Override
	public ItemFeatureEntity findOne(String id) {
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public void deleteHard(ItemFeatureEntity object) {
		mongoTemplate.remove(object, TABLE);
	}

    @Override
    public long collectionSize() {
        return mongoTemplate.getCollection(TABLE).count();
    }
}
