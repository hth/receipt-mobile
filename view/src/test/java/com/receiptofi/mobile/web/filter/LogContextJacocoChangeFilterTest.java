package com.receiptofi.mobile.web.filter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogContextJacocoChangeFilterTest {

    @Mock (extraInterfaces = {HttpServletRequest.class}) private ServletRequest servletRequest;
    @Mock (extraInterfaces = {HttpServletResponse.class}) private ServletResponse servletResponse;
    @Mock private FilterChain filterChain;

    private LogContextJacocoChangeFilter logContextJacocoChangeFilter;
    private List<String> headers;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        logContextJacocoChangeFilter = new LogContextJacocoChangeFilter();
        headers = new ArrayList<>();
        headers.add("host");
        headers.add("user-agent");
        headers.add("accept");
        headers.add("x-forwarded-for");
    }

    @Test
    public void testDoFilter() throws IOException, ServletException {
        when(((HttpServletRequest) servletRequest).getHeaderNames()).thenReturn(Collections.enumeration(headers));
        when(((HttpServletRequest) servletRequest).getRequestURL()).thenReturn(new StringBuffer("https://abc/api/g?h=on&d=1"));
        when(((HttpServletRequest) servletRequest).getQueryString()).thenReturn("h=on&d=1");
        when(((HttpServletRequest) servletRequest).getHeader("host")).thenReturn("localhost");
        when(((HttpServletRequest) servletRequest).getHeader("user-agent")).thenReturn("Mozilla");
        when(((HttpServletRequest) servletRequest).getHeader("accept")).thenReturn("test/html");
        when(((HttpServletRequest) servletRequest).getHeader("x-forwarded-for")).thenReturn("someURL");
        logContextJacocoChangeFilter.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain, times(1)).doFilter(servletRequest, servletResponse);
    }
}
