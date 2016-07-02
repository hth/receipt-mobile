package com.receiptofi.mobile.service;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.types.FriendConnectionTypeEnum;
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
    @Autowired private AccountMobileService accountMobileService;

    public void getActiveFriends(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.setActiveFriends(friendService.getFriends(rid).values());
    }

    public void getPendingFriends(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.setPendingFriends(friendService.getPendingConnections(rid));
    }

    public void getAwaitingFriends(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.setAwaitingFriends(friendService.getAwaitingConnections(rid));
    }

    public boolean updateFriendConnection(String id, String auth, FriendConnectionTypeEnum friendConnectionType, String rid) {
        return friendService.updateFriendConnection(id, auth, friendConnectionType, rid);
    }

    public boolean unfriend(String receiptUserId, String fid) {
        UserAccountEntity userAccount = accountMobileService.findByRid(fid);
        return null != userAccount && friendService.unfriend(receiptUserId, userAccount.getUserId());
    }

    public boolean isConnected(String rid, String fid) {
        return friendService.isConnected(rid, fid);
    }
}
