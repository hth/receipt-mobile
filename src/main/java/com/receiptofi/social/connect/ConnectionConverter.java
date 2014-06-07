package com.receiptofi.social.connect;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.types.ProviderEnum;
import com.receiptofi.domain.types.RoleEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.stereotype.Component;

@Component
public class ConnectionConverter {
    private final ConnectionFactoryLocator connectionFactoryLocator;
    private final TextEncryptor textEncryptor;

    @Autowired
    public ConnectionConverter(ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.textEncryptor = textEncryptor;
    }

    public Connection<?> convert(UserAccountEntity userAccount) {
        if(userAccount == null) return null;

        ConnectionData connectionData = fillConnectionData(userAccount);
        ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId());
        return connectionFactory.createConnection(connectionData);
    }

    private ConnectionData fillConnectionData(UserAccountEntity userAccount) {
        return new ConnectionData(
                userAccount.getProviderId().toString().toLowerCase(),
                userAccount.getProviderUserId(),
                userAccount.getDisplayName(),
                userAccount.getProfileUrl(),
                userAccount.getImageUrl(),
                decrypt(userAccount.getAccessToken()),
                decrypt(userAccount.getSecret()),
                decrypt(userAccount.getRefreshToken()),
                userAccount.getExpireTime()
        );
    }

    public UserAccountEntity convert(Connection<?> cnn) {
        ConnectionData data = cnn.createData();

        UserAccountEntity userAccount = new UserAccountEntity();
        userAccount.addRole(RoleEnum.ROLE_USER);
        userAccount.setProviderId(ProviderEnum.valueOf(data.getProviderId().toUpperCase()));
        userAccount.setProviderUserId(data.getProviderUserId());
        userAccount.setDisplayName(data.getDisplayName());
        userAccount.setProfileUrl(data.getProfileUrl());
        userAccount.setImageUrl(data.getImageUrl());
        userAccount.setAccessToken(encrypt(data.getAccessToken()));
        userAccount.setSecret(encrypt(data.getSecret()));
        userAccount.setRefreshToken(encrypt(data.getRefreshToken()));
        userAccount.setExpireTime(data.getExpireTime());
        return userAccount;
    }

    private String decrypt(String encryptedText) {
        return encryptedText != null ? textEncryptor.decrypt(encryptedText) : encryptedText;
    }

    private String encrypt(String text) {
        return text != null ? textEncryptor.encrypt(text) : text;
    }
}