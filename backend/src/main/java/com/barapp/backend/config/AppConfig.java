package com.barapp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Configuration générale : déclare les beans utilitaires de l'application
@Configuration
public class AppConfig {

    // RestTemplate permet de faire des appels HTTP vers des APIs externes comme TheCocktailDB
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}