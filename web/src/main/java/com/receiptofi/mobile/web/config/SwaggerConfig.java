package com.receiptofi.mobile.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

/**
 * User: hitender
 * Date: 12/9/14 1:45 AM
 */
@Configuration
@EnableWebMvc
@ComponentScan("com.receiptofi.mobile.web.controller")
@EnableSwagger /* Loads the spring beans required by the framework. */
public class SwaggerConfig extends WebMvcConfigurerAdapter {
    private SpringSwaggerConfig springSwaggerConfig;

    /**
     * Required to autowire SpringSwaggerConfig.
     */
    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
        this.springSwaggerConfig = springSwaggerConfig;
    }

    /**
     * Every SwaggerSpringMvcPlugin bean is picked up by the swagger-mvc framework - allowing for multiple
     * swagger groups i.e. same code base multiple swagger resource listings.
     * Include patterns from root - .*
     * Include specific pattern /api/* - .api.*
     */
    @Bean
    public SwaggerSpringMvcPlugin customImplementation() {
        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
                .includePatterns(".*");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "My Apps API Title",
                "My Apps API Description",
                "My Apps API terms of service",
                "My Apps API Contact Email",
                "My Apps API Licence Type",
                "My Apps API License URL"
        );
        return apiInfo;
    }
}
