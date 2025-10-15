package com.sciome.bmdexpressweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect /docs to /docs/index.html
        registry.addRedirectViewController("/docs", "/docs/index.html");
        registry.addRedirectViewController("/docs/", "/docs/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve all /docs/** paths as static resources before Vaadin routing
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/static/docs/")
                .resourceChain(false);
    }
}
