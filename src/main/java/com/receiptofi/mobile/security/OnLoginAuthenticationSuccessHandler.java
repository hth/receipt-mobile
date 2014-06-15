package com.receiptofi.mobile.security;

import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.domain.types.RoleEnum;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

/**
 * User: hitender
 * Date: 5/28/14 12:42 AM
 */
public class OnLoginAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private RequestCache requestCache = new HttpSessionRequestCache();

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private AuthenticatedToken authenticatedToken;

    @Autowired
    public OnLoginAuthenticationSuccessHandler(AuthenticatedToken authenticatedToken) {
        this.authenticatedToken = authenticatedToken;
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws ServletException, IOException {
        if(request.getHeader("cookie") != null) {
            handle(request, response, authentication);
            clearAuthenticationAttributes(request);
        } else {
            response.addHeader("X-R-MAIL", ((ReceiptUser) authentication.getPrincipal()).getUsername());
            response.addHeader("X-R-AUTH", authenticatedToken.getUserAuthenticationKey(((ReceiptUser) authentication.getPrincipal()).getUsername()));
            //response.addHeader("X-R-ROLE", authentication.getAuthorities().toString());
            //response.addCookie(authenticatedToken.createAuthenticatedCookie(((ReceiptUser) authentication.getPrincipal()).getUsername()));
        }

        /**
         * Refer: http://www.baeldung.com/2011/10/31/securing-a-restful-web-service-with-spring-security-3-1-part-3/
         * To execute: curl -i -X POST -d emailId=some@mail.com -d password=realPassword http://localhost:8080/receipt/j_spring_security_check
         */
        final SavedRequest savedRequest = requestCache.getRequest(request, response);

        if(savedRequest == null) {
            clearAuthenticationAttributes(request);
            return;
        }
        final String targetUrlParameter = getTargetUrlParameter();
        if(isAlwaysUseDefaultTargetUrl() || (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
            requestCache.removeRequest(request, response);
            clearAuthenticationAttributes(request);
            return;
        }

        clearAuthenticationAttributes(request);
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(authentication);

        if(response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    /**
     * Builds the landing URL according to the user role when they log in
     * Refer: http://www.baeldung.com/spring_redirect_after_login
     */
    protected String determineTargetUrl(Authentication authentication) {
        boolean isUser = false, isSup = false, isEmp = false, isAdmin = false;

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for(GrantedAuthority grantedAuthority : authorities) {
            if(grantedAuthority.getAuthority().equals(RoleEnum.ROLE_USER.name())) {
                isUser = true;
                break;
            } else if(grantedAuthority.getAuthority().equals(RoleEnum.ROLE_SUPERVISOR.name())) {
                isSup = true;
                break;
            } else if(grantedAuthority.getAuthority().equals(RoleEnum.ROLE_TECHNICIAN.name())) {
                isEmp = true;
                break;
            } else if(grantedAuthority.getAuthority().equals(RoleEnum.ROLE_ADMIN.name())) {
                isAdmin = true;
                break;
            }
        }

        if(isAdmin) {
            return "/admin/landing.htm";
        } else if(isSup || isEmp) {
            return "/emp/landing.htm";
        } else if(isUser) {
            return "/access/landing.htm";
        } else {
            throw new IllegalStateException();
        }
    }
}
