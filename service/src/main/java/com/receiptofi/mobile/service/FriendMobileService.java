package com.receiptofi.mobile.service;

import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.service.FriendService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 10/17/15 11:31 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Service
public class FriendMobileService {

    @Autowired private FriendService friendService;

    public void getActiveFriends(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.setActiveFriends(friendService.getFriends(rid).values());
    }

    public void getPendingFriends(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.setPendingFriends(friendService.getPendingConnections(rid));
    }

    public void getAwaitingFriends(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.setAwaitingFriends(friendService.getAwaitingConnections(rid));
    }
}
