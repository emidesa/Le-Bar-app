package com.barapp.backend.security;

import com.barapp.backend.entity.User;
import com.barapp.backend.enums.Role;
import com.barapp.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_retourne_userdetails_si_trouve() {
        User user = User.builder().id(1L).email("bar@barapp.fr").password("encoded").role(Role.BARMAKER).build();
        when(userRepository.findByEmail("bar@barapp.fr")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("bar@barapp.fr");

        assertThat(result.getUsername()).isEqualTo("bar@barapp.fr");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_BARMAKER");
    }

    @Test
    void loadUserByUsername_lance_exception_si_non_trouve() {
        when(userRepository.findByEmail("inconnu@barapp.fr")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("inconnu@barapp.fr"));
    }
}
