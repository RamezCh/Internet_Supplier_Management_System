package com.github.ramezch.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.url}")
    private String appUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/api/auth/**").permitAll()
                        .requestMatchers("/index.html", "/assets/index-*.js", "/assets/index-*.css", "/cms.png").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )
                .logout(l -> l.logoutSuccessUrl(appUrl))
                .oauth2Login(o -> o.defaultSuccessUrl(appUrl))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );
        return http.build();
    }

}