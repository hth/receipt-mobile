package com.receiptofi.mobile.web.filter;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApiFilterTest {

    @Mock (extraInterfaces = {HttpServletRequest.class}) private ServletRequest servletRequest;
    @Mock (extraInterfaces = {HttpServletResponse.class}) private ServletResponse servletResponse;
    @Mock private FilterChain filterChain;

    private ApiFilter apiFilter;
    private List<String> headers;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        apiFilter = new ApiFilter();
        headers = new ArrayList<>();
        headers.add("X-R-MAIL");
        headers.add("X-R-AUTH");
    }

    @Test
    public void testDoFilterFailure() throws IOException, ServletException {
        when(((HttpServletRequest) servletRequest).getHeader("X-R-MAIL")).thenReturn("");
        when(((HttpServletRequest) servletRequest).getHeader("X-R-AUTH")).thenReturn("");

        apiFilter.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain, never()).doFilter(servletRequest, servletResponse);
    }

    @Test
    public void testDoFilter() throws IOException, ServletException {
        when(((HttpServletRequest) servletRequest).getHeader("X-R-MAIL")).thenReturn("test@mail.com");
        when(((HttpServletRequest) servletRequest).getHeader("X-R-AUTH")).thenReturn("7326482734");

        apiFilter.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain, times(1)).doFilter(servletRequest, servletResponse);
    }
}
