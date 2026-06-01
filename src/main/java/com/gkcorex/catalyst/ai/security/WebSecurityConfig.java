package com.gkcorex.catalyst.ai.security;

import jakarta.servlet.DispatcherType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

// @EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WebSecurityConfig {

  JwtAuthFilter jwtAuthFilter;

  HandlerExceptionResolver handlerExceptionResolver;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
    httpSecurity
        .csrf(csrfConfig -> csrfConfig.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                //                auth.requestMatchers("/api/auth/**", "/webhooks/**")
                auth.dispatcherTypeMatchers(DispatcherType.ASYNC)
                    .permitAll()
                    .dispatcherTypeMatchers(DispatcherType.ERROR)
                    .permitAll()
                    .requestMatchers("/api/v1/account/auth/**", "/webhooks/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exceptionHandlingConfigurer ->
                exceptionHandlingConfigurer.accessDeniedHandler(
                    (request, response, accessDeniedException) -> {
                      handlerExceptionResolver.resolveException(
                          request, response, null, accessDeniedException);
                    }));
    return httpSecurity.build();
  }
}
