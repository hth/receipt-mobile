package com.receiptofi.utils;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;

import org.springframework.web.util.HtmlUtils;

/**
 * User: hitender
 * Date: 11/24/13 11:28 AM
 */
public class TextInputScrubber {
    private static Logger log = LoggerFactory.getLogger(TextInputScrubber.class);

    public static String scrub(String text) {
        if(StringUtils.isBlank(text)) {
            return text;
        }

        String decoded;
        try {
            decoded = URLDecoder.decode(text, "UTF-8");
        } catch(Exception exce) {
            log.error("Text decode failed: " + text);
            return StringUtils.EMPTY;
        }

        HtmlPolicyBuilder htmlPolicyBuilder = new HtmlPolicyBuilder();
        PolicyFactory factory = htmlPolicyBuilder.toFactory();
        String scrubbedText = factory.sanitize(decoded);

        //Using Spring instead of StringEscapeUtils
        return HtmlUtils.htmlUnescape(scrubbedText);
    }
}
