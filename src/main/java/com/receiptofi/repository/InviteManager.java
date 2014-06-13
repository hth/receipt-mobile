package com.receiptofi.repository;

import com.receiptofi.domain.InviteEntity;
import com.receiptofi.domain.UserAccountEntity;

/**
 * User: hitender
 * Date: 6/9/13
 * Time: 2:15 PM
 */
public interface InviteManager extends RepositoryManager<InviteEntity> {

    /**
     * Find InviteEntity by authentication key
     *
     * @param auth
     * @return
     */
    InviteEntity findByAuthenticationKey(String auth);

    /**
     * Make all the existing request invalid
     *
     * @param object
     */
    void invalidateAllEntries(InviteEntity object);

    /**
     * Find the user who has been invited and the invite is active
     *
     * @param emailId
     */
    InviteEntity reInviteActiveInvite(String emailId, UserAccountEntity invitedBy);

    /**
     * Search existing invite by email id that is active and not deleted
     *
     * @param emailId
     * @return
     */
    InviteEntity find(String emailId);
}
