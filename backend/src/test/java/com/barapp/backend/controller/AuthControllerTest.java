package com.barapp.backend.controller;

import com.barapp.backend.dto.request.LoginRequest;
import com.barapp.backend.dto.request.RegisterRequest;
import com.barapp.backend.entity.User;
import com.barapp.backend.enums.Role;
import com.barapp.backend.repository.UserRepository;
import com.barapp.backend.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired WebApplicationContext context;
    @MockitoBean UserRepository userRepository;
    @MockitoBean PasswordEncoder passwordEncoder;
    @MockitoBean JwtUtils jwtUtils;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    void register_cree_compte_et_retourne_token() throws Exception {
        User saved = User.builder().id(1L).email("bar@barapp.fr").password("encoded").role(Role.BARMAKER).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(jwtUtils.generateToken("bar@barapp.fr", "BARMAKER")).thenReturn("fake-token");

        RegisterRequest request = new RegisterRequest();
        request.setEmail("bar@barapp.fr");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-token"))
                .andExpect(jsonPath("$.email").value("bar@barapp.fr"));
    }

    @Test
    void login_retourne_token_si_credentials_valides() throws Exception {
        User user = User.builder().id(1L).email("bar@barapp.fr").password("encoded").role(Role.BARMAKER).build();
        when(userRepository.findByEmail("bar@barapp.fr")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtUtils.generateToken("bar@barapp.fr", "BARMAKER")).thenReturn("fake-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("bar@barapp.fr");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-token"));
    }

    @Test
    void login_retourne_401_si_mauvais_mot_de_passe() throws Exception {
        User user = User.builder().id(1L).email("bar@barapp.fr").password("encoded").role(Role.BARMAKER).build();
        when(userRepository.findByEmail("bar@barapp.fr")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mauvais", "encoded")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("bar@barapp.fr");
        request.setPassword("mauvais");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
