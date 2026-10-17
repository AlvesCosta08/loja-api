package com.alvescosta.loja.assistant.embedding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Fake determinístico para testes.
 * Mesmo texto → mesmo vetor. Textos diferentes → vetores diferentes.
 * NÃO usar em produção.
 */
public class FakeEmbeddingProvider implements EmbeddingProvider {

    private final int dimension;

    public FakeEmbeddingProvider(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text não pode ser nulo ou vazio");
        }

        long seed = hashToSeed(text);
        Random random = new Random(seed);

        float[] vetor = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vetor[i] = random.nextFloat() * 2 - 1; // [-1, 1]
        }

        // Reutiliza o utilitário testado
        return Vectors.normalizarL2(vetor);
    }

    private long hashToSeed(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            long seed = 0;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (digest[i] & 0xFF);
            }
            return seed;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}