package com.example.bankcards.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "security.cors")
@Getter
@Setter
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>();
    private Long maxAge;
    private Boolean allowCredentials;
}