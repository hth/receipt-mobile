package com.receiptofi.mobile.web.filter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class LogContextFilterTest {

    @Mock(extraInterfaces = {HttpServletRequest.class}) private ServletRequest servletRequest;
    @Mock(extraInterfaces = {HttpServletRequest.class}) private ServletResponse servletResponse;
    @Mock private FilterChain filterChain;
    @Mock private FilterConfig filterConfig;

    private LogContextFilter logContextFilter;
    private List<String> headers;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        logContextFilter = new LogContextFilter();
        headers = new ArrayList<>();
        headers.add("host");
        headers.add("user-agent");
        headers.add("accept");
        headers.add("x-forwarded-for");
    }

    @Test
    public void testDoFilter() throws Exception {
        when(((HttpServletRequest)servletRequest).getHeaderNames()).thenReturn(Collections.enumeration(headers));
        when(((HttpServletRequest)servletRequest).getRequestURL()).thenReturn(new StringBuffer("https://abc/api/g?h=on&d=1"));
        when(((HttpServletRequest)servletRequest).getQueryString()).thenReturn("h=on&d=1");
        when(((HttpServletRequest)servletRequest).getHeader("host")).thenReturn("localhost");
        when(((HttpServletRequest)servletRequest).getHeader("user-agent")).thenReturn("http");
        when(((HttpServletRequest)servletRequest).getHeader("accept")).thenReturn("json");
        when(((HttpServletRequest)servletRequest).getHeader("x-forwarded-for")).thenReturn("someURL");
        logContextFilter.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain, times(1)).doFilter(servletRequest, servletResponse);
    }
}
