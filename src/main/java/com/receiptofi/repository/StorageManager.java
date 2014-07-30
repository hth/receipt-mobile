/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.shared.UploadReceiptImage;

import java.io.IOException;
import java.util.Collection;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * @author hitender
 * @since Jan 3, 2013 3:08:12 AM
 *
 * For GridFsTemplate. Because of the GridFsTemplate the mongo content has been moved to receipt-servlet.xml
 * @see http://www.rainydayinn.com/dev/distributed-storage-with-mongo-gridfs-with-spring-data-mongodb/
 *
 * Stores Receipt Image in GridFs
 */
public interface StorageManager extends RepositoryManager<UploadReceiptImage> {

	/**
	 * Saves the image and return the bolb id
	 * @param object - File
	 * @return String - bolbId
	 * @throws IOException
	 */
	String saveFile(UploadReceiptImage object) throws IOException;

	GridFSDBFile get(String id);

	GridFSDBFile getByFilename(String filename);

    /**
     * Removes the file from db
     *
     * @param id
     */
	void deleteHard(String id);
    void deleteHard(Collection<FileSystemEntity> fileSystemEntities);

    /**
     * Add a field delete and set the value to true
     *
     * @param fileSystemEntities
     */
    void deleteSoft(Collection<FileSystemEntity> fileSystemEntities);

	/**
	 * Gets size of the GridFs
	 * @return
	 */
	int getSize();
}
