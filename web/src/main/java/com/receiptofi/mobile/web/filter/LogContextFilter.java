package com.receiptofi.mobile.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * User: hitender
 * Date: 6/1/14 3:38 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class LogContextFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(LogContextFilter.class);

    /* https://stackoverflow.com/questions/24894093/ruby-regular-expression-extracting-part-of-url */
    private static final Pattern EXTRACT_ENDPOINT_PATTERN =
            Pattern.compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
    private static final String REQUEST_ID_MDC_KEY = "X-REQUEST-ID";

    @Override
    public void doFilter(
            ServletRequest req,
            ServletResponse res,
            FilterChain chain
    ) throws IOException, ServletException {
        String uuid = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID_MDC_KEY, uuid);

        Map<String, String> headerMap = getHeadersInfo((HttpServletRequest) req);
        String url = ((HttpServletRequest) req).getRequestURL().toString();
        String query = ((HttpServletRequest) req).getQueryString();

        LOG.info("Request received:"
                        + " Host=\"" + getHeader(headerMap, "host") + "\""
                        + " UserAgent=\"" + getHeader(headerMap, "user-agent") + "\""
                        + " Accept=\"" + getHeader(headerMap, "accept") + "\""
                        + " ForwardedFor=\"" + getHeader(headerMap, "x-forwarded-for") + "\""
                        + " Endpoint=\"" + extractDataFromURL(url, "$5") + "\""
                        + " Query=\"" + query + "\""
                        + " URL=\"" + url + "\""
        );
        chain.doFilter(req, res);
    }

    private String getHeader(Map<String, String> headers, String header) {
        return CollectionUtils.isEmpty(headers) && !headers.containsKey(header) ? "" : headers.get(header);
    }

    private String extractDataFromURL(String uri, String group) {
        return EXTRACT_ENDPOINT_PATTERN.matcher(uri).replaceFirst(group);
    }

    private Map<String, String> getHeadersInfo(HttpServletRequest request) {

        Map<String, String> map = new HashMap<>();

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.info("initialized");
    }

    @Override
    public void destroy() {
        LOG.info("deleted");
    }
}
