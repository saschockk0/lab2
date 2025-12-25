package com.rus.bank.config;

import com.rus.bank.security.JwtAuthFilter;
import com.rus.bank.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) throws Exception {
        http
                // CSRF отключен для REST API
                .csrf(AbstractHttpConfigurer::disable)

                // JWT проект = stateless
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // для чистого API выключаем базовую auth
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // CORS
                .cors(Customizer.withDefaults())

                // Доступы
                .authorizeHttpRequests(auth -> auth
                        // swagger / docs
                        .requestMatchers("/swagger/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // главная
                        .requestMatchers("/", "/home", "/index.html").permitAll()

                        // Админские операции доступны только ADMIN
                        .requestMatchers("/api/accounts/*/block", "/api/accounts/*/unblock").hasAuthority("ADMIN")
                        .requestMatchers("/api/cards/*/block", "/api/cards/*/unblock").hasAuthority("ADMIN")

                        // всё остальное — только с access токеном (CLIENT или ADMIN)
                        .anyRequest().authenticated()
                )

                // JWT фильтр ДО UsernamePasswordAuthenticationFilter
                .addFilterBefore(new JwtAuthFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

