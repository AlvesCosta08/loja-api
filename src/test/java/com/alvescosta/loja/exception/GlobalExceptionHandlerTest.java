package com.alvescosta.loja.exception;

import com.alvescosta.loja.domain.Role;
import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.dto.UserCreateRequest;
import com.alvescosta.loja.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("GlobalExceptionHandler: deve padronizar todas as respostas de erro")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    // ==================== 400 — Bad Request ====================

    @Test
    @DisplayName("400: JSON malformado deve retornar Bad Request")
    void deveRetornar400ParaJsonMalformado() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"cli@exemplo.com\"")) // JSON incompleto proposital
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("400: tipo errado em parâmetro deve retornar Bad Request")
    void deveRetornar400ParaTipoErrado() throws Exception {
        mockMvc.perform(get("/products/abc")) // ID deve ser Long, não String
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("400: parâmetro obrigatório ausente deve retornar Bad Request")
    void deveRetornar400ParaParametroAusente() throws Exception {
        mockMvc.perform(post("/payments")
                        .header("Authorization", "Bearer " + gerarTokenCliente())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ==================== 401 — Unauthorized ====================

    @Test
    @DisplayName("401: token ausente em rota protegida deve retornar Unauthorized")
    void deveRetornar401ParaTokenAusente() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("401: token inválido deve retornar Unauthorized")
    void deveRetornar401ParaTokenInvalido() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // ==================== 403 — Forbidden ====================

    @Test
    @DisplayName("403: cliente tentando acessar rota ADMIN deve retornar Forbidden")
    void deveRetornar403ParaClienteEmRotaAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                        .header("Authorization", "Bearer " + gerarTokenCliente()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // ==================== 404 — Not Found ====================

    @Test
    @DisplayName("404: produto inexistente deve retornar Not Found")
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ==================== 405 — Method Not Allowed ====================

    @Test
    @DisplayName("405: GET em rota que só aceita POST deve retornar Method Not Allowed")
    void deveRetornar405ParaMetodoNaoPermitido() throws Exception {
        // /auth/login só aceita POST. Um GET deve cair no handler de 405.
        mockMvc.perform(get("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    // ==================== 409 — Conflict ====================

    @Test
    @DisplayName("409: cadastro com e-mail duplicado deve retornar Conflict")
    void deveRetornar409ParaEmailDuplicado() throws Exception {
        var user = new UserCreateRequest("duplicado@teste.com", "Teste", "senha12345");
        
        // Primeiro cadastro (sucesso)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isCreated());

        // Segundo cadastro com mesmo e-mail (deve retornar 409)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado: duplicado@teste.com"));
    }

    // ==================== Helpers ====================

    private String gerarTokenCliente() {
        User user = User.builder()
                .id(1L)
                .email("cliente@teste.com")
                .name("Cliente Teste")
                .role(Role.CUSTOMER)
                .build();
        return jwtService.generate(user);
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar JSON para teste", e);
        }
    }
}