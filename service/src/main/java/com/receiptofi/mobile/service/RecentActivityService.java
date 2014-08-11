package com.receiptofi.mobile.service;

import java.util.Date;
import java.util.List;

import com.receiptofi.domain.RecentActivityEntity;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.domain.AccountUpdate;
import com.receiptofi.repository.RecentActivityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 8/10/14 1:32 PM
 */
@Service
public final class RecentActivityService {

    private RecentActivityManager recentActivityManager;

    @Autowired
    public RecentActivityService(RecentActivityManager recentActivityManager) {
        this.recentActivityManager = recentActivityManager;
    }

    public AvailableAccountUpdates hasRecentActivities(String rid, Date since) {
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        List<RecentActivityEntity> recentActivities = recentActivityManager.findAll(rid, since);
        for(RecentActivityEntity recentActivity : recentActivities) {
            availableAccountUpdates.addRecentActivity(AccountUpdate.newInstance(recentActivity));
        }
        return availableAccountUpdates;
    }
}
