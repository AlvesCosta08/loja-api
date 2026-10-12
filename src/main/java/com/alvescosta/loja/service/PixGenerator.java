package com.alvescosta.loja.service;

/**
 * Gerador "mock" de payload Pix. Em producao, isso chamaria o PSP
 * (gateway de pagamento) para gerar o EMV real e o QR Code.
 */
public final class PixGenerator {

    private PixGenerator() {}

    public static Pix generate(Long orderId, String amount) {
        String copyPaste = "00020126580014BR.GOV.BCB.PIX0136"
                + "loja-api-" + orderId + "-" + amount
                + "5204000053039865802BR5925ALVES COSTA LOJA LTDA6009SAO PAULO62070503***6304ABCD";

        String qrPng = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
        String qrBase64 = "data:image/png;base64," + qrPng;

        return new Pix(copyPaste, qrBase64);
    }

    public record Pix(String copyPaste, String qrCodeBase64) {}
}
