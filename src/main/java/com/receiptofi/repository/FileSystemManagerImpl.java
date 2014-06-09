package com.receiptofi.repository;

import com.receiptofi.domain.BaseEntity;
import com.receiptofi.domain.FileSystemEntity;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static com.receiptofi.repository.util.AppendAdditionalFields.entityUpdate;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

/**
 * User: hitender
 * Date: 12/23/13 9:21 PM
 */
@Repository
public final class FileSystemManagerImpl implements FileSystemManager {
    private static final Logger log = LoggerFactory.getLogger(FileSystemManagerImpl.class);
    private static final String TABLE = BaseEntity.getClassAnnotationValue(FileSystemEntity.class, Document.class, "collection");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<FileSystemEntity> getAllObjects() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void save(FileSystemEntity object) {
        if(object.getId() != null) {
            object.setUpdated();
        }
        mongoTemplate.save(object, TABLE);
    }

    @Override
    public FileSystemEntity findOne(String id) {
        return mongoTemplate.findOne(query(where("id").is(id)), FileSystemEntity.class, TABLE);
    }

    @Override
    public void deleteHard(FileSystemEntity object) {
        mongoTemplate.remove(object, TABLE);
    }

    public void deleteHard(Collection<FileSystemEntity> fileSystemEntities) {
        for(FileSystemEntity fileSystemEntity : fileSystemEntities) {
            deleteHard(fileSystemEntity);
        }
    }

    private void deleteSoft(FileSystemEntity fileSystemEntity) {
        mongoTemplate.updateMulti(
                query(where("id").is(new ObjectId(fileSystemEntity.getId()))),
                entityUpdate(update("D", true)),
                FileSystemEntity.class
        );
    }

    public void deleteSoft(Collection<FileSystemEntity> fileSystemEntities) {
        for(FileSystemEntity fileSystemEntity : fileSystemEntities) {
            deleteSoft(fileSystemEntity);
        }
    }

    @Override
    public long collectionSize() {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
