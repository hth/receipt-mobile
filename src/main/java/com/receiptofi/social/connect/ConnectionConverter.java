package com.receiptofi.social.connect;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.types.ProviderEnum;
import com.receiptofi.social.annotation.Social;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.stereotype.Component;

@Component
@Social
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

    public UserAccountEntity convert(String userId, String receiptUserId, Connection<?> cnn) {
        ConnectionData data = cnn.createData();

        UserAccountEntity userAccount = UserAccountEntity.newInstance(
                receiptUserId,
                userId,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                UserAuthenticationEntity.blankInstance()
        );
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

    public UserAccountEntity convert(String userId, Connection<?> cnn) {
        return convert(userId, null, cnn);
    }

    private String decrypt(String encryptedText) {
        return encryptedText == null ? encryptedText : textEncryptor.decrypt(encryptedText);
    }

    private String encrypt(String text) {
        return text == null ? text : textEncryptor.encrypt(text);
    }
}