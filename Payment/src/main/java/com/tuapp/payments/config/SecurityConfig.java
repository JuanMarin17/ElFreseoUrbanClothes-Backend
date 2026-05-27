package com.tuapp.payments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // El webhook de MP debe ser público (MP no manda JWT)
                .requestMatchers("/api/payments/webhook").permitAll()
                // El callback OAuth debe ser público (redirect de MP)
                .requestMatchers("/api/oauth/callback").permitAll()
                // El resto requiere autenticación (ajusta según tu sistema de auth)
                .anyRequest().authenticated()
            );

        // Si usas JWT, agrega tu filtro aquí:
        // http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
