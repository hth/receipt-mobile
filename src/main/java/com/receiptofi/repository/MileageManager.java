package com.receiptofi.repository;

import com.receiptofi.domain.MileageEntity;

import java.util.List;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 12/25/13 4:16 AM
 */
public interface MileageManager extends RepositoryManager<MileageEntity> {

    MileageEntity findOne(String id, String userProfileId);
    List<MileageEntity> getMileageForThisMonth(String userProfileId, DateTime startMonth, DateTime endMonth);
    boolean updateStartDate(String mileageId, DateTime startDate, String userProfileId);
    boolean updateEndDate(String mileageId, DateTime endDate, String userProfileId);
}
