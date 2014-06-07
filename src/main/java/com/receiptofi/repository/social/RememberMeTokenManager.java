package com.receiptofi.repository.social;

import com.receiptofi.domain.social.RememberMeTokenEntity;
import com.receiptofi.repository.RepositoryManager;

/**
 * User: hitender
 * Date: 3/30/14 7:38 PM
 */
public interface RememberMeTokenManager extends RepositoryManager<RememberMeTokenEntity> {
    RememberMeTokenEntity findBySeries(String series);
    void deleteTokensWithUsername(String username);
}