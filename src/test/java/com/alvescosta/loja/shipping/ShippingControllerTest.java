package com.alvescosta.loja.shipping;

import com.alvescosta.loja.dto.ShippingQuoteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ShippingController — Cálculo de frete")
class ShippingControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("200 para SP (prefixo 0)")
    void deveCalcularFreteSP() throws Exception {
        ShippingQuoteRequest req = new ShippingQuoteRequest("01310100",
                new BigDecimal("100.00"));

        mockMvc.perform(post("/shipping/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01310100"))
                .andExpect(jsonPath("$.options", hasSize(2)))
                .andExpect(jsonPath("$.options[0].type").value("STANDARD"))
                .andExpect(jsonPath("$.options[0].price").value(10.00))
                .andExpect(jsonPath("$.options[1].type").value("EXPRESS"))
                .andExpect(jsonPath("$.options[1].price").value(20.00));
    }

    @Test
    @DisplayName("200 para RJ (prefixo 2)")
    void deveCalcularFreteRJ() throws Exception {
        ShippingQuoteRequest req = new ShippingQuoteRequest("20000000",
                new BigDecimal("100.00"));

        mockMvc.perform(post("/shipping/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options[0].price").value(15.00))
                .andExpect(jsonPath("$.options[1].price").value(30.00));
    }

    @Test
    @DisplayName("200 para outra região (prefixo 6)")
    void deveCalcularFreteOutraRegiao() throws Exception {
        ShippingQuoteRequest req = new ShippingQuoteRequest("60000000",
                new BigDecimal("100.00"));

        mockMvc.perform(post("/shipping/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options[0].price").value(20.00))
                .andExpect(jsonPath("$.options[1].price").value(40.00));
    }

    @Test
    @DisplayName("Frete STANDARD grátis acima de R$ 200")
    void deveDarFreteGratisStandardAcimaDe200() throws Exception {
        ShippingQuoteRequest req = new ShippingQuoteRequest("01310100",
                new BigDecimal("250.00"));

        mockMvc.perform(post("/shipping/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options[0].price").value(0))
                .andExpect(jsonPath("$.options[1].price").value(20.00));
    }

    @Test
    @DisplayName("200 aceita CEP com hífen")
    void deveAceitarCepComHifen() throws Exception {
        ShippingQuoteRequest req = new ShippingQuoteRequest("01310-100",
                new BigDecimal("100.00"));

        mockMvc.perform(post("/shipping/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("400 quando CEP inválido")
    void deveRejeitarCepInvalido() throws Exception {
        ShippingQuoteRequest req = new ShippingQuoteRequest("123",
                new BigDecimal("100.00"));

        mockMvc.perform(post("/shipping/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}