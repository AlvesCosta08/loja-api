package com.alvescosta.loja.coupons;

import com.alvescosta.loja.domain.*;
import com.alvescosta.loja.dto.AddToCartRequest;
import com.alvescosta.loja.dto.CouponRequest;
import com.alvescosta.loja.repository.*;
import com.alvescosta.loja.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CouponController — Cupons de desconto")
class CouponControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired CouponRepository couponRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private User customer;
    private User admin;
    private Product product;
    private String token;
    private String adminToken;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        couponRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        customer = userRepository.save(User.builder()
                .email("cli@test.com").name("Cli")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER).build());

        admin = userRepository.save(User.builder()
                .email("adm@test.com").name("Adm")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.ADMIN).build());

        product = productRepository.save(Product.builder()
                .name("Notebook").price(new BigDecimal("1000.00"))
                .stock(50).category("Eletronicos").build());

        token = jwtService.generate(customer);
        adminToken = jwtService.generate(admin);
    }

    // ---------- CRIAÇÃO (ADMIN) ----------

    @Test
    @DisplayName("401 sem token ao criar cupom")
    void naoDeveCriarCupomSemToken() throws Exception {
        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("403 quando CUSTOMER tenta criar cupom")
    void clienteNaoPodeCriarCupom() throws Exception {
        CouponRequest req = new CouponRequest(
                "PROMO10", DiscountType.PERCENTAGE, new BigDecimal("10"),
                null, null, null);

        mockMvc.perform(post("/coupons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("201 quando ADMIN cria cupom percentual")
    void adminDeveCriarCupomPercentual() throws Exception {
        CouponRequest req = new CouponRequest(
                "PROMO10", DiscountType.PERCENTAGE, new BigDecimal("10"),
                null, null, null);

        mockMvc.perform(post("/coupons")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("PROMO10"))
                .andExpect(jsonPath("$.type").value("PERCENTAGE"))
                .andExpect(jsonPath("$.value").value(10));
    }

    @Test
    @DisplayName("400 quando código é duplicado")
    void naoDeveCriarCupomDuplicado() throws Exception {
        CouponRequest req = new CouponRequest(
                "PROMO10", DiscountType.PERCENTAGE, new BigDecimal("10"),
                null, null, null);

        mockMvc.perform(post("/coupons")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(post("/coupons")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("codigo")));
    }

    @Test
    @DisplayName("400 quando percentual > 100")
    void naoDeveAceitarPercentualAcimaDe100() throws Exception {
        CouponRequest req = new CouponRequest(
                "PROMO", DiscountType.PERCENTAGE, new BigDecimal("150"),
                null, null, null);

        mockMvc.perform(post("/coupons")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------- APLICAR NO CHECKOUT ----------

    @Test
    @DisplayName("Aplica cupom percentual no checkout")
    void deveAplicarCupomPercentual() throws Exception {
        couponRepository.save(Coupon.builder()
                .code("PROMO10").type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .active(true).usageCount(0).build());

        adicionarAoCarrinho(product.getId(), 1); // 1000.00

        mockMvc.perform(post("/orders").param("couponCode", "PROMO10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(900.00))
                .andExpect(jsonPath("$.discount").value(100.00))
                .andExpect(jsonPath("$.couponCode").value("PROMO10"));
    }

    @Test
    @DisplayName("Aplica cupom fixo no checkout")
    void deveAplicarCupomFixo() throws Exception {
        couponRepository.save(Coupon.builder()
                .code("OFF50").type(DiscountType.FIXED)
                .value(new BigDecimal("50.00"))
                .active(true).usageCount(0).build());

        adicionarAoCarrinho(product.getId(), 1);

        mockMvc.perform(post("/orders").param("couponCode", "OFF50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(950.00))
                .andExpect(jsonPath("$.discount").value(50.00));
    }

    @Test
    @DisplayName("400 cupom inexistente no checkout")
    void naoDeveAplicarCupomInexistente() throws Exception {
        adicionarAoCarrinho(product.getId(), 1);

        mockMvc.perform(post("/orders").param("couponCode", "NAOEXISTE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 cupom expirado")
    void naoDeveAplicarCupomExpirado() throws Exception {
        couponRepository.save(Coupon.builder()
                .code("VELHO").type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .active(true).usageCount(0).build());

        adicionarAoCarrinho(product.getId(), 1);

        mockMvc.perform(post("/orders").param("couponCode", "VELHO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 cupom abaixo do valor mínimo")
    void naoDeveAplicarCupomAbaixoDoMinimo() throws Exception {
        couponRepository.save(Coupon.builder()
                .code("MINIMO").type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .minOrderValue(new BigDecimal("5000.00"))
                .active(true).usageCount(0).build());

        adicionarAoCarrinho(product.getId(), 1); // 1000.00, mínimo 5000

        mockMvc.perform(post("/orders").param("couponCode", "MINIMO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 cupom com usos esgotados")
    void naoDeveAplicarCupomEsgotado() throws Exception {
        couponRepository.save(Coupon.builder()
                .code("ESGOTADO").type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .maxUses(5).usageCount(5)
                .active(true).build());

        adicionarAoCarrinho(product.getId(), 1);

        mockMvc.perform(post("/orders").param("couponCode", "ESGOTADO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Incrementa usageCount após uso")
    void deveIncrementarUso() throws Exception {
        Coupon c = couponRepository.save(Coupon.builder()
                .code("USO").type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .active(true).usageCount(0).build());

        adicionarAoCarrinho(product.getId(), 1);

        mockMvc.perform(post("/orders").param("couponCode", "USO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        Coupon atualizado = couponRepository.findById(c.getId()).orElseThrow();
        assert atualizado.getUsageCount() == 1;
    }

    // ---------- helpers ----------

    private void adicionarAoCarrinho(Long productId, int qty) throws Exception {
        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AddToCartRequest(productId, qty))));
    }
}