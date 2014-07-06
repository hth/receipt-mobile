package com.receiptofi.web.controller.api.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.security.web.csrf.CsrfToken;

/**
 * User: hitender
 * Date: 6/30/14 1:26 AM
 */
@WebFilter(urlPatterns={"/api/mobile/*"})
public class MobileApiFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(MobileApiFilter.class);

    @Override
    public void init(FilterConfig config) throws ServletException {
        // If you have any <init-param> in web.xml, then you could get them
        // here by config.getInitParameter("name") and assign it as field.
        log.info("Api filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (StringUtils.isBlank(((HttpServletRequest) request).getHeader("X-R-API-MOBILE"))) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        // Populate X-CSRF-TOKEN in response when not found in request
        if (StringUtils.isBlank(((HttpServletRequest) request).getHeader("X-CSRF-TOKEN"))) {
            CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
            // Spring Security will allow the Token to be included in this header name
            ((HttpServletResponse) response).setHeader("X-CSRF-HEADER", token.getHeaderName());
            // Spring Security will allow the token to be included in this parameter name
            ((HttpServletResponse) response).setHeader("X-CSRF-PARAM", token.getParameterName());
            // this is the value of the token to be included as either a header or an HTTP parameter
            ((HttpServletResponse) response).setHeader("X-CSRF-TOKEN", token.getToken());
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // If you have assigned any expensive resources as field of
        // this Filter class, then you could clean/close them here.
        log.info("Api filter destroyed");
    }
}

