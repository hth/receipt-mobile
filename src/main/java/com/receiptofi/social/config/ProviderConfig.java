package com.receiptofi.social.config;

import com.receiptofi.social.annotation.Social;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * User: hitender
 * Date: 7/9/14 12:27 AM
 */
@Configuration
@Social
public class ProviderConfig {

    @Value("${populate.social.friend.on:false}")
    private boolean populateSocialFriendOn;

    public boolean isPopulateSocialFriendOn() {
        return populateSocialFriendOn;
    }
}
