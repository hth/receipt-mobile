package com.receiptofi.mobile.web.filter;

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

/**
 * User: hitender
 * Date: 6/9/14 11:28 PM
 */
@WebFilter(urlPatterns={"/api/*"})
public class ApiFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(ApiFilter.class);

    @Override
    public void init(FilterConfig config) throws ServletException {
        // If you have any <init-param> in web.xml, then you could get them
        // here by config.getInitParameter("name") and assign it as field.
        log.info("Api filter initialized");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (StringUtils.isBlank(((HttpServletRequest) req).getHeader("X-R-MAIL")) || StringUtils.isBlank(((HttpServletRequest) req).getHeader("X-R-AUTH"))) {
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // If you have assigned any expensive resources as field of
        // this Filter class, then you could clean/close them here.
        log.info("Api filter destroyed");
    }
}
