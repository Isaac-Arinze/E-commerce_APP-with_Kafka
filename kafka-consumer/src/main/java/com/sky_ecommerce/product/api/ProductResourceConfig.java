package com.sky_ecommerce.product.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves files saved under kafka-consumer/uploads/** at /uploads/**
 */
@Configuration
public class ProductResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL path /uploads/** to the local folder kafka-consumer/uploads/**
        String location = "file:kafka-consumer/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
