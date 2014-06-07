package com.receiptofi.social.connect;

import com.receiptofi.domain.types.ProviderEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UsersConnectionRepository;

//sadly there is no default implementation for Mongo :(
public class MongoUsersConnectionRepository implements UsersConnectionRepository {

    private String userId;

    @Autowired
    private ConnectionService connectionService;
    private ConnectionFactoryLocator connectionFactoryLocator;
    private TextEncryptor textEncryptor;
    private ConnectionSignUp connectionSignUp;

    @Autowired
    public MongoUsersConnectionRepository(String userId,
                                          ConnectionService connectionService,
                                          ConnectionFactoryLocator connectionFactoryLocator,
                                          TextEncryptor textEncryptor) {

        this.userId = userId;
        this.connectionService = connectionService;
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.textEncryptor = textEncryptor;
    }

    public MongoUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator,
                                          TextEncryptor noOpText) {

        this.connectionFactoryLocator = connectionFactoryLocator;
        textEncryptor = noOpText;
    }

    public void setConnectionSignUp(ConnectionSignUp connectionSignUp) {
        this.connectionSignUp = connectionSignUp;
    }

    public void removeConnections(ProviderEnum providerId) {
        connectionService.remove(userId, providerId);
    }

    public void removeConnection(ConnectionKey connectionKey) {
        connectionService.remove(userId, connectionKey);
    }

    public List<String> findUserIdsWithConnection(final Connection<?> connection) {
        ProviderEnum providerId = ProviderEnum.valueOf(connection.getKey().getProviderId().toUpperCase());
        List<String> result = connectionService.getUserIds(providerId, connection.getKey().getProviderUserId());

        if(result == null || result.size() == 0) {
            connectionService.create(connection.getKey().getProviderUserId(), connection);
            result = new ArrayList<String>() {{
                add(connection.getKey().getProviderUserId());
            }};
        }

        return result;
    }

    public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
        return connectionService.getUserIds(ProviderEnum.valueOf(providerId), providerUserIds);
    }

    public ConnectionRepository createConnectionRepository(String userId) {
        if(userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        return new MongoConnectionRepository(userId, connectionService, connectionFactoryLocator, textEncryptor);
    }

}
