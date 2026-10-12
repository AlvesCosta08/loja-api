package com.alvescosta.loja.controller;

import com.alvescosta.loja.dto.PaymentResponse;
import com.alvescosta.loja.exception.BusinessException;
import com.alvescosta.loja.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    /**
     * Endpoint público que o Stripe chama quando o pagamento é concluído.
     * Não exige JWT, mas exige o header Stripe-Signature.
     *
     * Em produção, o header deve ser validado contra o webhook secret do Stripe.
     * Aqui apenas verificamos a presença para simular o comportamento.
     */
    @PostMapping("/stripe")
    public Map<String, String> stripe(
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestBody String rawBody) throws Exception {

        if (signature == null || signature.isBlank()) {
            throw new BusinessException("Assinatura ausente");
        }

        JsonNode node = objectMapper.readTree(rawBody);
        String type = node.path("type").asText();
        String gatewayRef = node.path("data").path("object").path("id").asText();

        if (!"payment_intent.succeeded".equals(type)) {
            return Map.of("status", "ignored");
        }

        PaymentResponse response = paymentService.handleGatewayWebhook(gatewayRef);

        return Map.of(
                "status", "processed",
                "paymentId", response.id().toString()
        );
    }
}
