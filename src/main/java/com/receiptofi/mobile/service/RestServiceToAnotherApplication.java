package com.receiptofi.mobile.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Used to GET or POST data from/to another application
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
 * User: hitender
 * Date: 6/12/14 11:53 PM
 */
public abstract class RestServiceToAnotherApplication {

    //Create a list for the message converters
    private final List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>() {{
        add(new MappingJacksonHttpMessageConverter());
    }};

    //Create a Rest template
    public final RestTemplate restTemplate = new RestTemplate() {{
        setMessageConverters(messageConverters);
    }};
}
