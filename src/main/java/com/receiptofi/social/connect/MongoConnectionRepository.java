package com.receiptofi.social.connect;

import com.receiptofi.domain.types.ProviderEnum;
import com.receiptofi.social.annotation.Social;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.NoSuchConnectionException;
import org.springframework.social.connect.NotConnectedException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Social
public class MongoConnectionRepository implements ConnectionRepository {

    private final String userId;
    private final ConnectionService connectionService;
    private final ConnectionFactoryLocator connectionFactoryLocator;
    private final TextEncryptor textEncryptor;

    public MongoConnectionRepository(String userId,
                                     ConnectionService connectionService,
                                     ConnectionFactoryLocator connectionFactoryLocator,
                                     TextEncryptor textEncryptor) {

        this.userId = userId;
        this.connectionService = connectionService;
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.textEncryptor = textEncryptor;
    }

    public MultiValueMap<String, Connection<?>> findAllConnections() {
        List<Connection<?>> resultList = connectionService.getConnections(userId);

        MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<>();
        Set<String> registeredProviderIds = this.connectionFactoryLocator.registeredProviderIds();
        for(String registeredProviderId : registeredProviderIds) {
            connections.put(registeredProviderId, Collections.<Connection<?>>emptyList());
        }

        for(Connection<?> connection : resultList) {
            String providerId = connection.getKey().getProviderId();
            if(connections.get(providerId).size() == 0) {
                connections.put(providerId, new LinkedList<Connection<?>>());
            }
            connections.add(providerId, connection);
        }
        return connections;
    }

    public List<Connection<?>> findConnections(String providerId) {
        return connectionService.getConnections(userId, ProviderEnum.valueOf(providerId.toUpperCase()));
    }

    @SuppressWarnings("unchecked")
    public <A> List<Connection<A>> findConnections(Class<A> apiType) {
        return (List<Connection<A>>) (List<?>) findConnections(getProviderId(apiType));
    }

    public MultiValueMap<String, Connection<?>> findConnectionsToUsers(MultiValueMap<String, String> providerUserIds) {
        if(providerUserIds == null || providerUserIds.isEmpty()) {
            throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
        }

        List<Connection<?>> resultList = connectionService.getConnections(userId, providerUserIds);

        MultiValueMap<String, Connection<?>> connectionsForUsers = new LinkedMultiValueMap<>();
        for(Connection<?> connection : resultList) {
            String providerId = connection.getKey().getProviderId();
            List<String> userIds = providerUserIds.get(providerId);
            List<Connection<?>> connections = connectionsForUsers.get(providerId);
            if(connections == null) {
                //TODO re-check this code
                connections = new ArrayList<>(userIds.size());
                for(String userId : userIds) {
                    connections.add(null);
                }
                connectionsForUsers.put(providerId, connections);
            }
            String providerUserId = connection.getKey().getProviderUserId();
            int connectionIndex = userIds.indexOf(providerUserId);
            connections.set(connectionIndex, connection);
        }
        return connectionsForUsers;
    }

    public Connection<?> getConnection(ConnectionKey connectionKey) {
        try {
            return connectionService.getConnection(
                    userId,
                    ProviderEnum.valueOf(connectionKey.getProviderId().toUpperCase()),
                    connectionKey.getProviderUserId()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchConnectionException(connectionKey);
        }
    }

    @SuppressWarnings("unchecked")
    public <A> Connection<A> getConnection(Class<A> apiType, String providerUserId) {
        String providerId = getProviderId(apiType);
        return (Connection<A>) getConnection(new ConnectionKey(providerId, providerUserId));
    }

    public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
        ProviderEnum providerId = ProviderEnum.valueOf(getProviderId(apiType));
        @SuppressWarnings("unchecked")
        Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
        if(connection == null) {
            throw new NotConnectedException(providerId.toString());
        }
        return connection;
    }

    @SuppressWarnings("unchecked")
    public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
        ProviderEnum providerId = ProviderEnum.valueOf(getProviderId(apiType).toUpperCase());
        return (Connection<A>) findPrimaryConnection(providerId);
    }

    public void addConnection(Connection<?> connection) {
        connectionService.create(userId, connection);
    }

    public void updateConnection(Connection<?> connection) {
        connectionService.update(userId, connection);
    }

    public void removeConnections(String providerId) {
        connectionService.remove(userId, ProviderEnum.valueOf(providerId.toUpperCase()));
    }

    public void removeConnection(ConnectionKey connectionKey) {
        connectionService.remove(userId, connectionKey);
    }

    private <A> String getProviderId(Class<A> apiType) {
        return connectionFactoryLocator.getConnectionFactory(apiType).getProviderId();
    }

    private Connection<?> findPrimaryConnection(ProviderEnum providerId) {
        return connectionService.getPrimaryConnection(userId, providerId);
    }
}