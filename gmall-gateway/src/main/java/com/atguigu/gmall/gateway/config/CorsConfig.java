package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter(){

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:1000");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");

        configurationSource.registerCorsConfiguration("/**",config);
        return new CorsWebFilter(configurationSource);
    }
}
