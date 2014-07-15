package com.receiptofi.web.util;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.site.ReceiptUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

/**
 * Maintains if registration is allowed.
 *
 * User: hitender
 * Date: 6/22/14 7:30 PM
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class Registration {
    private static final Logger log = LoggerFactory.getLogger(Registration.class);

    @Value("${registration.turned.on}")
    private boolean registrationTurnedOn;

    @Value("${indexController:/open/index.htm}")
    private String indexController;

    public void changeUserAccountActiveState(UserAccountEntity userAccount) {
        if(!registrationTurnedOn) {
            userAccount.inActive();
        }
    }

    public boolean validateIfRegistrationIsAllowed(ModelMap map, Authentication authentication) {
        if(!((UserDetails) authentication.getPrincipal()).isEnabled()) {
            ReceiptUser receiptUser = (ReceiptUser) authentication.getPrincipal();

            SecurityContextHolder.getContext().setAuthentication(
                    new AnonymousAuthenticationToken(
                            String.valueOf(System.currentTimeMillis()),
                            "anonymousUser",
                            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
                    )
            );
            map.addAttribute("deniedSignup", true);
            map.addAttribute("user", receiptUser.getUsername());
            map.addAttribute("pid", receiptUser.getPid());
            return true;
        }
        return false;
    }

    /**
     * To check is app is currently accepting registration
     *
     * @param userAccount
     */
    public void isRegistrationAllowed(UserAccountEntity userAccount) {
        if(registrationTurnedOn) {
            log.info("registration is allowed, marking user={} active", userAccount.getReceiptUserId());
            userAccount.active();
        } else {
            log.info("registration is NOT allowed, marking user={} inactive", userAccount.getReceiptUserId());
            userAccount.inActive();
        }
    }

    /**
     * Last line of defense when registration is turned off and user logs in through one of the provider
     *
     * @param user
     * @return
     */
    public boolean checkRegistrationIsTurnedOn(UserDetails user) {
        log.info("profile active={} user={} redirect to {}", user.isEnabled(), user.getUsername(), indexController);

        if(user.isEnabled() || registrationTurnedOn) {
            return false;
        }

        return true;
    }

    public String getIndexController() {
        return indexController;
    }

    public boolean isRegistrationTurnedOn() {
        return registrationTurnedOn;
    }
}