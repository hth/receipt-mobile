package com.receiptofi.domain;

import com.receiptofi.utils.DateUtil;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * User: hitender
 * Date: 12/13/13 12:38 AM
 */
@Document(collection = "MILEAGE")
@CompoundIndexes({
        @CompoundIndex(name = "mileage_se_idx", def = "{'S': -1, 'E': -1, 'USER_PROFILE_ID': -1}", unique = true),
        @CompoundIndex(name = "mileage_s_idx", def = "{'S': -1, 'USER_PROFILE_ID': -1}", unique = true)
})
public final class MileageEntity extends BaseEntity {

    @NotNull
    @Field("S")
    private int start;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Field("SD")
    private Date startDate;

    @Field("E")
    private int end;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Field("ED")
    private Date endDate;

    @NotNull
    @Field("USER_PROFILE_ID")
    private String userProfileId;

    @NotNull
    @Field("DOC_ID")
    private String documentId;

    @DBRef
    @Field("FILES")
    private Collection<FileSystemEntity> fileSystemEntities;

    @DBRef
    @Field("N")
    private CommentEntity mileageNotes;

    /**
     * To keep bean happy
     */
    public MileageEntity() {
    }

    public MileageEntity(FileSystemEntity fileSystemEntity, String userProfileId) {
        if(fileSystemEntities == null) {
            fileSystemEntities = new LinkedList<>();
        }
        fileSystemEntities.add(fileSystemEntity);
        this.userProfileId = userProfileId;
        this.startDate = getCreated();
    }

    /**
     * To be used during merge operation
     */
    public void mergeEndingMileage(MileageEntity mileageEntity) {
        if(!mileageEntity.isComplete()) {
            this.end = mileageEntity.getStart();
            this.endDate = mileageEntity.getStartDate();
            if(mileageNotes != null) {
                mileageNotes.setText(mergeComments(mileageEntity).toString());
            }
            fileSystemEntities.add(mileageEntity.getFileSystemEntities().iterator().next());
        }
    }

    private StringBuilder mergeComments(MileageEntity mileageEntity) {
        StringBuilder mergedText = new StringBuilder();
        mergedText.append(mileageNotes != null ? mileageNotes.getText() : "");
        if(mergedText.toString().length() > 0) {
            mergedText.append("\n\n");
        }
        mergedText.append(mileageEntity.getMileageNotes() != null ? mileageEntity.getMileageNotes().getText() : "");
        return mergedText;
    }

    public MileageEntity splitMileage() {
        FileSystemEntity fileSystemEntity = null;
        for(FileSystemEntity fse : getFileSystemEntities()) {
            fileSystemEntity = fse;
        }
        MileageEntity m2 = new MileageEntity(fileSystemEntity, getUserProfileId());
        m2.setStart(getEnd());
        m2.setStartDate(getEndDate());

        setEnd(0);
        setEndDate(null);
        List<FileSystemEntity> fileSystemEntities = new LinkedList<>();
        fileSystemEntities.add(getFileSystemEntities().iterator().next());
        setFileSystemEntities(fileSystemEntities);
        return m2;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getUserProfileId() {
        return userProfileId;
    }

    public void setUserProfileId(String userProfileId) {
        this.userProfileId = userProfileId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Collection<FileSystemEntity> getFileSystemEntities() {
        return fileSystemEntities;
    }

    public void setFileSystemEntities(Collection<FileSystemEntity> fileSystemEntities) {
        this.fileSystemEntities = fileSystemEntities;
    }

    public void addFileSystemEntities(Collection<FileSystemEntity> fileSystemEntities) {
        if(this.fileSystemEntities == null) {
            this.fileSystemEntities = Collections.emptyList();
        }
        this.fileSystemEntities.addAll(fileSystemEntities);
    }

    public CommentEntity getMileageNotes() {
        return mileageNotes;
    }

    public void setMileageNotes(CommentEntity mileageNotes) {
        this.mileageNotes = mileageNotes;
    }

    @Transient
    public int getTotal() {
        return isComplete() ? end - start : start;
    }

    /**
     * Complete represent when start and end exist in the record
     *
     * @return
     */
    @Transient
    public boolean isComplete() {
        return start != 0 && end != 0 && fileSystemEntities.size() > 0;
    }

    @Transient
    public String tripDays() {
        if(startDate == null) {
            return "";
        }

        DateTime dayStart = DateUtil.toDateTime(startDate);
        DateTime dayEnd = DateUtil.toDateTime(endDate);

        int days = Days.daysBetween(dayStart.withTimeAtStartOfDay(), dayEnd.withTimeAtStartOfDay()).getDays();
        switch (days) {
            case 0:
                return "Same Day Trip";
            case 1:
                return "1 day trip";
            default:
                if(days > 0) {
                    return days + " days trip";
                } else if(days < 0) {
                    return "Trip end day greater";
                } else {
                    return "";
                }
        }
    }
}
