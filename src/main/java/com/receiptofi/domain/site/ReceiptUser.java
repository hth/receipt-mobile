package com.receiptofi.domain.site;

import com.receiptofi.domain.types.ProviderEnum;
import com.receiptofi.domain.types.UserLevelEnum;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * User: hitender
 * Date: 4/27/14 2:48 AM
 */
public final class ReceiptUser extends User {

    private String rid;
    private ProviderEnum pid;
    private UserLevelEnum userLevel;

    public ReceiptUser(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, password, authorities);
    }

    public ReceiptUser(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            String rid,
            ProviderEnum pid,
            UserLevelEnum userLevel,
            boolean active
    ) {
        super(username, password, active, true, true, true, authorities);
        this.rid = rid;
        this.pid = pid;
        this.userLevel = userLevel;
    }

    public ReceiptUser(
            String username,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    public ReceiptUser(
            String username,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities,
            String rid,
            ProviderEnum pid,
            UserLevelEnum userLevel
    ) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.rid = rid;
        this.pid = pid;
        this.userLevel = userLevel;
    }

    public String getRid() {
        return rid;
    }

    public UserLevelEnum getUserLevel() {
        return userLevel;
    }

    public ProviderEnum getPid() {
        return pid;
    }
}
