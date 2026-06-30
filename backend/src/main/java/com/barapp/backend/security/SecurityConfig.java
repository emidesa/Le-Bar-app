package com.barapp.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

// Configuration principale de Spring Security
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // Je n'utilise pas de session, le JWT suffit
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Les routes publiques accessibles sans token
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/cocktails/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                // Les clients commandent et suivent leurs commandes sans compte
                .requestMatchers("/api/orders").permitAll()
                .requestMatchers("/api/orders/table/**").permitAll()
                // Lecture d'une commande en détail (barmaker, mais aussi accessible sans compte)
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/**").permitAll()
                // Avancement des items par le barmaker — ID connu uniquement côté barmaker
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/orders/items/**").permitAll()
                // Seul le barmaker connecté peut accéder aux autres routes
                .anyRequest().authenticated()
            )
            // Retourne 401 (et non 403) pour les requêtes sans token valide
            .exceptionHandling(e -> e.authenticationEntryPoint(
                (req, res, ex) -> res.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Non authentifié")
            ))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Encodeur de mot de passe BCrypt, utilisé à l'inscription
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}