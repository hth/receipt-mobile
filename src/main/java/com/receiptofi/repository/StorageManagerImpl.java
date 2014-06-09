/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.web.form.UploadReceiptImage;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * @author hitender
 * @since Jan 3, 2013 3:09:08 AM
 *
 */
public final class StorageManagerImpl implements StorageManager {
	private static final Logger log = LoggerFactory.getLogger(StorageManagerImpl.class);

	private final GridFS gridFs;

	public StorageManagerImpl(DB gridfsDb) {
        try {
		    gridFs = new GridFS(gridfsDb);
        } catch(com.mongodb.MongoException exception) {
            log.error("Error in initializing MongoDB: Issue with getting the connection during server startup. " + exception.getLocalizedMessage());
            log.error("Receiptofi Mongo DB: " + exception.getMessage());
            throw exception;
        }
	}

	@Override
	public List<UploadReceiptImage> getAllObjects() {
		DBCursor dbCursor = gridFs.getFileList();
		while(dbCursor.hasNext()) {
			DBObject dbObject = dbCursor.next();
			gridFs.find(dbObject.toString());
		}
		throw new UnsupportedOperationException("Method not implemented");
	}

    @Override
	public int getSize() {
		return gridFs.getFileList().size();
	}

	@Override
	public void save(UploadReceiptImage object) {
		persist(object);
	}

	@Override
	public String saveFile(UploadReceiptImage object) throws IOException {
		return persist(object);
	}

	@Override
	public UploadReceiptImage findOne(String id) {
		throw new UnsupportedOperationException("Method not implemented");
	}

    private void deleteSoft(String id) {
        GridFSDBFile receiptBlob = get(id);
        receiptBlob.put("D", true);
        receiptBlob.save();
    }

    @Override
    public void deleteSoft(Collection<FileSystemEntity> fileSystemEntities) {
        for(FileSystemEntity fileSystemEntity : fileSystemEntities) {
            deleteSoft(fileSystemEntity.getBlobId());
        }
    }

	@Override
	public void deleteHard(UploadReceiptImage object) {
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public void deleteHard(String id) {
		log.debug("deleted GridFs object - " + id);
		gridFs.remove(new ObjectId(id));
	}

    @Override
    public void deleteHard(Collection<FileSystemEntity> fileSystemEntities) {
        for(FileSystemEntity fileSystemEntity : fileSystemEntities) {
            log.debug("deleted GridFs object - " + fileSystemEntity.getBlobId());
            gridFs.remove(new ObjectId(fileSystemEntity.getBlobId()));
        }
    }

	private String persist(UploadReceiptImage uploadReceiptImage) {
		boolean closeStreamOnPersist = true;
		GridFSInputFile receiptBlob;
        try {
            if(!uploadReceiptImage.containsFile()) {
                receiptBlob = gridFs.createFile(
                        uploadReceiptImage.getFileData().getInputStream(),
                        uploadReceiptImage.getFileName(),
                        closeStreamOnPersist);
            } else {
                InputStream is = new FileInputStream(uploadReceiptImage.getFile());
                receiptBlob = gridFs.createFile(is, uploadReceiptImage.getFileName(), closeStreamOnPersist);
            }
        } catch (IOException ioe) {
            log.error("Image persist error:{}", ioe);
            throw new RuntimeException(ioe.getCause());
        }

        if(receiptBlob != null) {
            receiptBlob.put("D", false);
            receiptBlob.put("FILE_TYPE", uploadReceiptImage.getFileType().getName());
            receiptBlob.setContentType(uploadReceiptImage.getFileData().getContentType());
            receiptBlob.setMetaData(uploadReceiptImage.getMetaData());

            receiptBlob.save();
            return receiptBlob.getId().toString();
        } else {
            return null;
        }
	}

	@Override
	public GridFSDBFile get(String id) {
		try {
			return gridFs.findOne(new ObjectId(id));
		} catch(IllegalArgumentException iae) {
			log.error("Submitted image id " + id + ", error message - " + iae.getLocalizedMessage());
			return null;
		}
	}

	@Override
	public GridFSDBFile getByFilename(String filename) {
		return gridFs.findOne(filename);
	}

    @Override
    public long collectionSize() {
        return getSize();
    }
}
