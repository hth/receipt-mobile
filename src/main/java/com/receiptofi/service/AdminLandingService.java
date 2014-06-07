package com.receiptofi.service;

import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.repository.UserProfileManager;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.form.UserSearchForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 4/28/13
 * Time: 8:34 PM
 */
@Service
public final class AdminLandingService {
    private static final Logger log = LoggerFactory.getLogger(AdminLandingService.class);

    @Autowired private UserProfileManager userProfileManager;

    /**
     * This method is called from AJAX to get the matching list of users in the system
     *
     * @param name
     * @return
     */
    public List<String> findMatchingUsers(String name) {
        DateTime time = DateUtil.now();
        List<String> users = new ArrayList<>();
        for(UserSearchForm userSearchForm : findAllUsers(name)) {
            users.add(userSearchForm.getUserName());
        }
        log.debug("List of users: ", users);
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return users;
    }

    /**
     * This method returns well populated users with 'id' and other relevant data for showing user profile.
     *
     * @param name
     * @return
     */
    public List<UserSearchForm> findAllUsers(String name) {
        DateTime time = DateUtil.now();
        log.info("Search string for user name: " + name);
        List<UserSearchForm> userList = new ArrayList<>();
        for(UserProfileEntity user : userProfileManager.searchAllByName(name)) {
            UserSearchForm userForm = UserSearchForm.newInstance(user);
            userList.add(userForm);
        }
        log.info("found users.. total size " + userList.size());
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return userList;
    }
}
