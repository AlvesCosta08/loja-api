package com.alvescosta.loja.controller;

import com.alvescosta.loja.domain.Role;
import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private MockMultipartFile fakeImage() {
        return new MockMultipartFile(
                "file",
                "teste.png",
                "image/png",
                "fake-png-bytes".getBytes()
        );
    }

    private User buildUser(Long id, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setName("Teste");
        u.setRole(role);
        return u;
    }

    @Test
    void deveFazerUploadDeImagemAutenticadoComoAdmin() throws Exception {
        String token = jwtService.generate(buildUser(1L, "admin@loja.com", Role.ADMIN));

        MvcResult result = mockMvc.perform(
                        multipart("/products/upload")
                                .file(fakeImage())
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.startsWith("/products/images/")))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String url = body.replaceAll(".*\"url\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"));
    }

    @Test
    void deveRejeitarUploadSemAutenticacao() throws Exception {
        mockMvc.perform(multipart("/products/upload").file(fakeImage()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarUploadComArquivoVazio() throws Exception {
        String token = jwtService.generate(buildUser(1L, "admin@loja.com", Role.ADMIN));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "vazio.png", "image/png", new byte[0]
        );

        mockMvc.perform(
                        multipart("/products/upload")
                                .file(emptyFile)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarUploadDeClienteComum() throws Exception {
        String token = jwtService.generate(buildUser(2L, "cliente@loja.com", Role.CUSTOMER));

        mockMvc.perform(
                        multipart("/products/upload")
                                .file(fakeImage())
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden());
    }
}