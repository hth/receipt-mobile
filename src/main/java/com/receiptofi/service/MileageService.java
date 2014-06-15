package com.receiptofi.service;

import com.receiptofi.domain.CommentEntity;
import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.MileageEntity;
import com.receiptofi.domain.types.CommentTypeEnum;
import com.receiptofi.repository.CommentManager;
import com.receiptofi.repository.DocumentManager;
import com.receiptofi.repository.MileageManager;
import com.receiptofi.repository.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * User: hitender
 * Date: 12/25/13 4:16 AM
 */
@Service
public final class MileageService {
    private static Logger log = LoggerFactory.getLogger(MileageService.class);

    @Autowired private MileageManager mileageManager;
    @Autowired private CommentManager commentManager;
    @Autowired private StorageManager storageManager;
    @Autowired private DocumentManager documentManager;
    @Autowired private FileSystemService fileSystemService;

    public void save(MileageEntity mileageEntity) throws Exception {
        mileageManager.save(mileageEntity);
    }

    public List<MileageEntity> getMileageForThisMonth(String profileId, DateTime monthYear) {
        DateTime startTime = monthYear.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        return mileageManager.getMileageForThisMonth(
                profileId,
                startTime,
                startTime.plusMonths(1).withTimeAtStartOfDay().minusMillis(1)
        );
    }

    public int monthlyTotal(String userProfileId, DateTime monthYear) {
        return mileageTotal(getMileageForThisMonth(userProfileId, monthYear));
    }

    public int mileageTotal(List<MileageEntity> mileageEntities) {
        int total = 0;
        for(MileageEntity mileageEntity : mileageEntities) {
            if(mileageEntity.isComplete()) {
                total += mileageEntity.getTotal();
            }
        }
        return total;
    }

    public MileageEntity merge(String id1, String id2, String userProfileId) {
        MileageEntity m1 = mileageManager.findOne(id1, userProfileId);
        MileageEntity m2 = mileageManager.findOne(id2, userProfileId);

        try {
            if(m1 != null && m2 != null && !m1.isComplete() && !m2.isComplete()) {
                if(Integer.compare(m1.getStart(), m2.getStart()) == 1) {
                    m2.mergeEndingMileage(m1);
                    if(m2.getMileageNotes() != null) {
                        commentManager.save(m2.getMileageNotes());
                    }
                    mileageManager.save(m2);
                    mileageManager.deleteHard(m1);
                    return m2;
                } else if(Integer.compare(m1.getStart(), m2.getStart()) == -1) {
                    m1.mergeEndingMileage(m2);
                    if(m1.getMileageNotes() != null) {
                        commentManager.save(m1.getMileageNotes());
                    }
                    mileageManager.save(m1);
                    mileageManager.deleteHard(m2);
                    return m1;
                } else if(Integer.compare(m1.getStart(), m2.getStart()) == 0) {
                    //There should not be a duplicate data; it should have been rejected
                    throw new RuntimeException("as starting mileage are equal");
                }
            }
        } catch (RuntimeException re) {
            log.error("Merge failed to save id1:id2 {}:{}, reason={}", id1, id2, re.getLocalizedMessage(), re);
            throw new RuntimeException("Merge failed to save " + re.getLocalizedMessage());
        } catch(Exception exception) {
            log.error("Merge failed to save id1:id2 {}:{}, reason={}", id1, id2, exception.getLocalizedMessage(), exception);
            throw new RuntimeException("Merge failed to save");
        }
        throw new RuntimeException("Merge failed as one or both could not be merged");
    }

    public List<MileageEntity> split(String id, String userProfileId) {
        MileageEntity m1 = mileageManager.findOne(id, userProfileId);
        try {
            if(m1 != null && m1.isComplete()) {
                MileageEntity m2 = m1.splitMileage();
                mileageManager.save(m1);
                mileageManager.save(m2);

                List<MileageEntity> list = new LinkedList<>();
                list.add(m2);
                list.add(m1);
                return list;
            }
        } catch(Exception exception) {
            log.error("Split failed to save, id={}, reason={}", id, exception.getLocalizedMessage(), exception);
            throw new RuntimeException("Split failed to save");
        }
        throw new RuntimeException("Could not process split");
    }

    public MileageEntity getMileage(String mileageId, String userProfileId) {
        return mileageManager.findOne(mileageId, userProfileId);
    }

    public boolean updateStartDate(String mileageId, String date, String userProfileId) {
        return mileageManager.updateStartDate(mileageId, DateTime.parse(date, DateTimeFormat.forPattern("MM/dd/yyyy")), userProfileId);
    }

    public boolean updateEndDate(String mileageId, String date, String userProfileId) {
        return mileageManager.updateEndDate(mileageId, DateTime.parse(date, DateTimeFormat.forPattern("MM/dd/yyyy")), userProfileId);
    }

    /**
     * Saves notes to mileage
     *
     * @param notes
     * @param mileageId
     * @param userProfileId
     * @return
     */
    public boolean updateMileageNotes(String notes, String mileageId, String userProfileId) {
        MileageEntity mileageEntity = mileageManager.findOne(mileageId, userProfileId);
        CommentEntity commentEntity = mileageEntity.getMileageNotes();
        boolean commentEntityBoolean = false;
        if(commentEntity == null) {
            commentEntityBoolean = true;
            commentEntity = CommentEntity.newInstance(CommentTypeEnum.NOTES);
            commentEntity.setText(notes);
        } else {
            commentEntity.setText(notes);
        }
        try {
            commentEntity.setUpdated();
            commentManager.save(commentEntity);
            if(commentEntityBoolean) {
                mileageEntity.setMileageNotes(commentEntity);
                mileageManager.save(mileageEntity);
            }
            return true;
        } catch (Exception exce) {
            log.error("Failed updating notes for mileage={}, reason={}", mileageId, exce.getLocalizedMessage(), exce);
            return false;
        }
    }

    /**
     * Delete mileage and its associated data
     * @param mileageId - Mileage id to delete
     */
    public boolean deleteHardMileage(String mileageId, String userProfileId) throws Exception {
        MileageEntity mileage = mileageManager.findOne(mileageId, userProfileId);
        if(mileage != null) {
            mileageManager.deleteHard(mileage);
            fileSystemService.deleteHard(mileage.getFileSystemEntities());
            for(FileSystemEntity fileSystemEntity : mileage.getFileSystemEntities()) {
                storageManager.deleteHard(fileSystemEntity.getBlobId());
            }
            DocumentEntity documentEntity = documentManager.findOne(mileage.getDocumentId(), userProfileId);
            if(documentEntity != null) {
                documentManager.deleteHard(documentEntity);
            }
            return true;
        }
        return false;
    }
}