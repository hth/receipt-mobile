package com.receiptofi.social.connect;

import com.receiptofi.domain.types.ProviderEnum;

import java.util.List;
import java.util.Set;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.util.MultiValueMap;

public interface ConnectionService {

    void create(String userId, Connection<?> userConn);
    void update(String userId, Connection<?> userConn);
    void remove(String userId, ConnectionKey connectionKey);
    void remove(String userId, ProviderEnum providerId);
    Connection<?> getPrimaryConnection(String userId, ProviderEnum providerId);
    Connection<?> getConnection(String userId, ProviderEnum providerId, String providerUserId);
    List<Connection<?>> getConnections(String userId);
    List<Connection<?>> getConnections(String userId, ProviderEnum providerId);
    List<Connection<?>> getConnections(String userId, MultiValueMap<String, String> providerUsers);
    Set<String> getUserIds(ProviderEnum providerId, Set<String> providerUserIds);
    List<String> getUserIds(ProviderEnum providerId, String providerUserId);

}
