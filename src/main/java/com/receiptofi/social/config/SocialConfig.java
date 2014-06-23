package com.receiptofi.social.config;

import com.receiptofi.repository.GenerateUserIdManager;
import com.receiptofi.service.AccountService;
import com.receiptofi.service.CustomUserDetailsService;
import com.receiptofi.social.annotation.Social;
import com.receiptofi.social.connect.ConnectionConverter;
import com.receiptofi.social.connect.ConnectionServiceImpl;
import com.receiptofi.social.connect.MongoUsersConnectionRepository;
import com.receiptofi.social.user.SignInAdapterImpl;
import com.receiptofi.social.user.SimpleConnectionSignUp;
import com.receiptofi.web.util.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.connect.GoogleConnectionFactory;

/**
 * User: hitender
 * Date: 5/10/14 12:35 AM
 */
@Configuration
@Social
public class SocialConfig {
    private static final Logger logger = LoggerFactory.getLogger(SocialConfig.class);

    @Value("${facebookClientId}")
    private String facebookClientId;

    @Value("${facebookClientSecret}")
    private String facebookClientSecret;

    @Value("${googleClientId}")
    private String googleClientId;

    @Value("${googleClientSecret}")
    private String googleClientSecret;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GenerateUserIdManager generateUserIdManager;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private Registration registration;

    /**
     * When a new provider is added to the app, register its {@link org.springframework.social.connect.ConnectionFactory} here.
     * @see org.springframework.social.google.connect.GoogleConnectionFactory
     */
    @Bean
    @Scope(value = "singleton", proxyMode = ScopedProxyMode.INTERFACES)
    public ConnectionFactoryLocator connectionFactoryLocator() {
        logger.info("Initializing connectionFactoryLocator");
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
        registry.addConnectionFactory(new FacebookConnectionFactory(facebookClientId, facebookClientSecret));
        registry.addConnectionFactory(new GoogleConnectionFactory(googleClientId, googleClientSecret));
        return registry;
    }

    /**
     * Singleton data access object providing access to connections across all users.
     */
    @Bean
    public UsersConnectionRepository usersConnectionRepository() {
        logger.info("Initializing usersConnectionRepository");
        MongoUsersConnectionRepository repository = new MongoUsersConnectionRepository(connectionFactoryLocator(), Encryptors.noOpText());
        repository.setConnectionSignUp(new SimpleConnectionSignUp());
        return repository;
    }

    /**
     * The Spring MVC Controller that allows users to sign-in with their provider accounts.
     */
    @Bean
    public ProviderSignInController providerSignInController(RequestCache requestCache) {
        logger.info("Initializing ProviderSignInController");
        ConnectionFactoryLocator connFactLocator = connectionFactoryLocator();
        UsersConnectionRepository usrConnRepo = usersConnectionRepository();
        SignInAdapterImpl signInAdapter = new SignInAdapterImpl(requestCache, customUserDetailsService, registration);
        ProviderSignInController controller = new ProviderSignInController(connFactLocator, usrConnRepo, signInAdapter);

        controller.setSignUpUrl("/signup");
        controller.setPostSignInUrl("/access/landing.htm");

        return controller;
    }

    @Bean
    public TextEncryptor textEncryptor() {
        logger.info("Initializing textEncryptor");
        return Encryptors.noOpText();
    }

    @Bean
    public ConnectionConverter connectionConverter() {
        logger.info("Initializing connectionConverter");
        return new ConnectionConverter(connectionFactoryLocator(), textEncryptor());
    }

    @Bean
    public ConnectionServiceImpl mongoConnectionService() {
        logger.info("Initializing mongoConnectionService");
        return new ConnectionServiceImpl(
                mongoTemplate,
                connectionConverter(),
                generateUserIdManager,
                accountService
        );
    }
}