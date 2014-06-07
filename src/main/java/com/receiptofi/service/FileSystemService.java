package com.receiptofi.service;

import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.repository.FileSystemManager;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 12/23/13 9:19 PM
 */
@Service
public final class FileSystemService {

    @Autowired private FileSystemManager fileSystemManager;

    public void save(FileSystemEntity fileSystemEntity) throws Exception {
        fileSystemManager.save(fileSystemEntity);
    }

    public FileSystemEntity findById(String id) {
        return fileSystemManager.findOne(id);
    }

    public void deleteSoft(Collection<FileSystemEntity> fileSystemEntities) {
        fileSystemManager.deleteSoft(fileSystemEntities);
    }

    public void deleteHard(FileSystemEntity fileSystemEntity) {
        fileSystemManager.deleteHard(fileSystemEntity);
    }

    public void deleteHard(Collection<FileSystemEntity> fileSystemEntities) {
        fileSystemManager.deleteHard(fileSystemEntities);
    }
}
