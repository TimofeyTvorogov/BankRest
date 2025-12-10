package com.example.bankcards.security;


import com.example.bankcards.exception.GlobalExceptionHandler;
import jakarta.servlet.FilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.AccessDeniedException;

@Configuration
public class FilterChainConfig {
    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    AuthenticationProvider provider;

    @Autowired
    CorsConfigurationSource corsConfigurationSource;

    @Autowired
    GlobalExceptionHandler globalHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) {
        security
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authenticationProvider(provider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint((request, response, authException) -> {
                        var responseEntity = globalHandler.handleAuthException(authException);
                        response.setStatus(responseEntity.getStatusCode().value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write(
                                new ObjectMapper().writeValueAsString(responseEntity.getBody())
                        );
                    });
                    exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                        var responseEntity = globalHandler.handleAuthException(accessDeniedException);
                        response.setStatus(responseEntity.getStatusCode().value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write(
                                new ObjectMapper().writeValueAsString(responseEntity.getBody())
                        );
                    });
                })
                .sessionManagement(sessionConfig ->
                        sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers("/auth/**", "/swagger-ui/**","/v3/api-docs/**","/swagger/**").permitAll()
                        .anyRequest().authenticated());
        return security.build();
    }
}
