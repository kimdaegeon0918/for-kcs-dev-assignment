package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {
    @Value("${allowed.origins}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(parseAllowedOrigins(allowedOrigins))
                        .allowedMethods("GET") // 읽기만 허용
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    // 허용도메인 파싱해서 리스트로 반환
    private String[] parseAllowedOrigins(String origins) {
        return origins.split(",");
    }
}