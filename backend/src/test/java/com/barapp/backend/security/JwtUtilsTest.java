package com.barapp.backend.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private final JwtUtils jwtUtils = new JwtUtils(
            "test-secret-key-pour-les-tests-unitaires-suffisamment-longue"
    );

    @Test
    void generateToken_retourne_token_non_nul() {
        String token = jwtUtils.generateToken("bar@barapp.fr", "BARMAKER");

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void getEmail_retourne_email_correct() {
        String token = jwtUtils.generateToken("bar@barapp.fr", "BARMAKER");

        assertThat(jwtUtils.getEmail(token)).isEqualTo("bar@barapp.fr");
    }

    @Test
    void isValid_retourne_true_pour_token_valide() {
        String token = jwtUtils.generateToken("bar@barapp.fr", "BARMAKER");

        assertThat(jwtUtils.isValid(token)).isTrue();
    }

    @Test
    void isValid_retourne_false_pour_token_invalide() {
        assertThat(jwtUtils.isValid("token.invalide.xxx")).isFalse();
    }
}
