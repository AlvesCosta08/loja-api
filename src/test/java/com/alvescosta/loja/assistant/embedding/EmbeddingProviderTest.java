package com.alvescosta.loja.assistant.embedding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EmbeddingProviderTest {

    private final EmbeddingProvider provider = new FakeEmbeddingProvider(1536);

    @Test
    void deve_retornar_vetor_com_dimensao_configurada() {
        float[] vetor = provider.embed("camiseta preta");
        assertThat(vetor).hasSize(1536);
    }

    @Test
    void deve_ser_deterministico_para_mesma_entrada() {
        float[] a = provider.embed("camiseta");
        float[] b = provider.embed("camiseta");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void deve_gerar_vetores_diferentes_para_textos_diferentes() {
        float[] a = provider.embed("camiseta");
        float[] b = provider.embed("calça");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void nao_deve_aceitar_texto_nulo_ou_vazio() {
        assertThatThrownBy(() -> provider.embed(null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> provider.embed("   "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_retornar_vetor_normalizado_com_norma_l2_igual_a_1() {
        float[] v = provider.embed("camiseta");
        double norma = 0;
        for (float x : v) norma += x * x;
        norma = Math.sqrt(norma);

        assertThat(norma).isCloseTo(1.0, within(1e-5));
    }

    @Test
    void deve_suportar_embedding_em_lote() {
        List<float[]> vetores = provider.embedAll(List.of("camiseta", "calça"));
        assertThat(vetores).hasSize(2);
        assertThat(vetores.get(0)).hasSize(1536);
        assertThat(vetores.get(1)).hasSize(1536);
    }
}