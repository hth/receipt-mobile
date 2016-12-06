package com.receiptofi.mobile.web.listener;

import com.receiptofi.service.cache.RedisCacheConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

/**
 * User: hitender
 * Date: 8/14/16 3:16 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Component
public class ReceiptofiInitializationCheckBean {
    private static final Logger LOG = LoggerFactory.getLogger(ReceiptofiInitializationCheckBean.class);

    private RedisCacheConfig redisCacheConfig;

    @Autowired
    public ReceiptofiInitializationCheckBean(RedisCacheConfig redisCacheConfig) {
        this.redisCacheConfig = redisCacheConfig;
    }

    @PostConstruct
    public void checkRedisConnection() {
        RedisConnection redisConnection = redisCacheConfig.redisTemplate().getConnectionFactory().getConnection();
        if (redisConnection.isClosed()) {
            LOG.error("Redis Server could not be connected");
            throw new RuntimeException("Redis Server could not be connected");
        }
        LOG.info("Redis Server connected");
    }
}
