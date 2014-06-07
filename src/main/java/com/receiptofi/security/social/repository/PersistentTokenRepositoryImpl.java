package com.receiptofi.security.social.repository;

import com.receiptofi.domain.social.RememberMeTokenEntity;
import com.receiptofi.repository.social.RememberMeTokenManager;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Repository;

/**
 * User: hitender
 * Date: 3/30/14 4:23 PM
 */
@Repository
public class PersistentTokenRepositoryImpl implements PersistentTokenRepository {

    @Autowired private RememberMeTokenManager rememberMeTokenManager;

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        RememberMeTokenEntity newToken = new RememberMeTokenEntity(token);
        rememberMeTokenManager.save(newToken);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        RememberMeTokenEntity token = rememberMeTokenManager.findBySeries(series);
        if (token != null){
            token.setTokenValue(tokenValue);
            token.setUpdated();
            rememberMeTokenManager.save(token);
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        RememberMeTokenEntity token = rememberMeTokenManager.findBySeries(seriesId);
        return new PersistentRememberMeToken(token.getUsername(), token.getSeries(), token.getTokenValue(), token.getUpdated());
    }

    @Override
    public void removeUserTokens(String username) {
        rememberMeTokenManager.deleteTokensWithUsername(username);
    }
}