package com.danil.library.config;

import com.danil.library.security.JwtAuthFilter;
import com.danil.library.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * AuthenticationManager нужен AuthService/контроллеру для логина (если ты через него логинишься).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) throws Exception {

        // CSRF токен будем хранить в cookie (норм для браузера)
        CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();

        http
                // 1) CSRF включён, НО auth ручки исключаем, чтобы не ловить 403 на /login и /refresh
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepo)
                        .ignoringRequestMatchers("/api/auth/**")   // <- must-fix
                )

                // 2) JWT проект = stateless (никаких HttpSession)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // <- must-fix

                // 3) для чистого API обычно базовую auth выключают
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())

                // 4) CORS (если фронт отдельно) — можно оставить дефолт, либо настроить явно
                .cors(Customizer.withDefaults())

                // 5) Доступы
                .authorizeHttpRequests(auth -> auth
                        // swagger / docs
                        .requestMatchers("/swagger/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // csrf endpoint (если у тебя есть CsrfController)
                        .requestMatchers("/api/csrf").permitAll()

                        // auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // главная/домашняя
                        .requestMatchers("/", "/home", "/index.html").permitAll()

                        // всё остальное — только с access токеном
                        .anyRequest().authenticated()
                )

                // 6) JWT фильтр ДО UsernamePasswordAuthenticationFilter
                .addFilterBefore(new JwtAuthFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
