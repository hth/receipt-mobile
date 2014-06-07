package com.receiptofi.social.user;

import com.receiptofi.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

@Component
@Profile(value = "DEV")
public final class SignInAdapterImpl implements SignInAdapter {
    private static final Logger log = LoggerFactory.getLogger(SignInAdapterImpl.class);

    private final RequestCache myRequestCache;

    private CustomUserDetailsService myUserAccountService;

    @Value("${completeProfileController:/access/completeprofile.htm}")
    private String completeProfileController;

    @Inject
    public SignInAdapterImpl(RequestCache requestCache, CustomUserDetailsService service) {
        myRequestCache = requestCache;
        myUserAccountService = service;
    }

    public String signIn(String localUserId, Connection<?> connection, NativeWebRequest request) {
        UserDetails user;
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        if(servletWebRequest.getRequest().getRequestURI().contains("facebook")) {
            user = myUserAccountService.loadUserByUserId(localUserId);
        } else {
            user = myUserAccountService.loadUserByUsername(StringUtils.lowerCase(localUserId));
        }
        Assert.notNull(user);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return extractOriginalUrl(request, user);
    }

    private String extractOriginalUrl(NativeWebRequest request, UserDetails user) {
        HttpServletRequest nativeReq = request.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse nativeRes = request.getNativeResponse(HttpServletResponse.class);
        SavedRequest saved = myRequestCache.getRequest(nativeReq, nativeRes);

        if(isProfileNotComplete(user)) {
            log.debug("profile not complete, user={}", user.getUsername());
            return completeProfileController;
        }

        if(saved == null) {
            return null;
        }

        myRequestCache.removeRequest(nativeReq, nativeRes);
        removeAuthenticationAttributes(nativeReq.getSession(false));
        return saved.getRedirectUrl();
    }

    private void removeAuthenticationAttributes(HttpSession session) {
        if(session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    private boolean isProfileNotComplete(UserDetails user) {
        return !user.getUsername().contains("@");
    }
}