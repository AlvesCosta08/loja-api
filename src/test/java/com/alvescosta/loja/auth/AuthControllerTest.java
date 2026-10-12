package com.alvescosta.loja.auth;

import com.alvescosta.loja.dto.LoginRequest;
import com.alvescosta.loja.dto.UserCreateRequest;
import com.alvescosta.loja.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController — Cadastro e Login")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    // ============ FASE 1 — CADASTRO ============

    @Test
    @DisplayName("201 ao registrar usuário com dados válidos")
    void deveRegistrarUsuarioComSucesso() throws Exception {
        UserCreateRequest req =
                new UserCreateRequest("alves@test.com", "Alves Costa", "senha12345");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("alves@test.com"))
                .andExpect(jsonPath("$.name").value("Alves Costa"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.hashedPassword").doesNotExist());
    }

    @Test
    @DisplayName("400 ao tentar registrar e-mail duplicado")
    void naoDeveRegistrarEmailDuplicado() throws Exception {
        UserCreateRequest req =
                new UserCreateRequest("alves@test.com", "Alves Costa", "senha12345");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsStringIgnoringCase("email")));
    }

    @Test
    @DisplayName("400 ao registrar com senha curta")
    void naoDeveAceitarSenhaCurta() throws Exception {
        UserCreateRequest req =
                new UserCreateRequest("alves@test.com", "Alves Costa", "123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ============ FASE 2 — LOGIN ============

    @Test
    @DisplayName("200 ao logar com credenciais válidas e retornar access_token")
    void deveLogarERetornarToken() throws Exception {
        registrarUsuario();

        LoginRequest req = new LoginRequest("alves@test.com", "senha12345");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"));
    }

    @Test
    @DisplayName("401 ao logar com senha errada")
    void deveRejeitarSenhaErrada() throws Exception {
        registrarUsuario();

        LoginRequest req = new LoginRequest("alves@test.com", "senhaErrada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401 ao logar com usuário inexistente")
    void naoDeveLogarUsuarioInexistente() throws Exception {
        LoginRequest req = new LoginRequest("naoexiste@test.com", "qualquercoisa");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- helpers ----------

    private void registrarUsuario() throws Exception {
        UserCreateRequest req =
                new UserCreateRequest("alves@test.com", "Alves Costa", "senha12345");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }
}