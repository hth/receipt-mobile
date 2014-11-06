package com.receiptofi.mobile.service;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to GET or POST data from/to another application.
 *
 * //A simple GET request, the response will be mapped to Example.class
 * Example result = restTemplate.getForObject(url, Example.class);
 *
 * //Create an object which can be sent with the POST request
 * PostExample postRequest = new PostExample("value1", "value2");
 *
 * //The POST request.
 * Example result2 = restTemplate.postForObject(url, postRequest, Example.class);
 *
 * http://blog.jdriven.com/2012/11/consume-rest-json-webservices-easily-using-spring-web/
 *
 * Add the code in receipt-servlet-mobile.xml
 * <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
 *      <property name="messageConverters">
 *          <list>
 *              <ref bean="jsonMessageConverter"/>
 *          </list>
 *      </property>
 * </bean>
 *
 * <!-- Configure bean to convert JSON to POJO and vice versa -->
 * <bean id="jsonMessageConverter"
 * class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter" />
 *
 * In Spring 4.0.x MappingJacksonHttpMessageConverter is deprecated.
 * You should use MappingJackson2HttpMessageConverter instead
 *
 * User: hitender
 * Date: 6/12/14 11:53 PM
 */
@SuppressWarnings ("unused")
public class RestServiceToAnotherApplication {
    private List<HttpMessageConverter<?>> messageConverters;

    /** Create a Rest template */
    public RestTemplate restTemplate;

    public RestServiceToAnotherApplication() {
        /** Create a list for the message converters */
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new MappingJackson2HttpMessageConverter());

        restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(messageConverters);
    }
}
