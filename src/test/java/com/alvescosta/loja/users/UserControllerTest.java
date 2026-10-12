package com.alvescosta.loja.users;

import com.alvescosta.loja.domain.Role;
import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.repository.UserRepository;
import com.alvescosta.loja.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController — Usuário logado")
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private User user;
    private String token;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("alves@test.com")
                .name("Alves Costa")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER)
                .build());

        token = jwtService.generate(user);
    }

    @Test
    @DisplayName("401 sem token")
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401 com token inválido")
    void deveRetornar401ComTokenInvalido() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("200 com dados do usuário logado")
    void deveRetornarDadosDoUsuarioLogado() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("alves@test.com"))
                .andExpect(jsonPath("$.name").value("Alves Costa"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.hashedPassword").doesNotExist());
    }
}