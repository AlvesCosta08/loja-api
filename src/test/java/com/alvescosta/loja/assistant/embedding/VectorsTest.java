
package com.alvescosta.loja.assistant.embedding;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class VectorsTest {

    @Test
    void normalizar_deve_produzir_norma_1() {
        float[] v = Vectors.normalizarL2(new float[]{3, 4});

        assertThat(Math.hypot(v[0], v[1])).isCloseTo(1.0, within(1e-6));
        assertThat(v[0]).isCloseTo(0.6f, within(1e-6f));
        assertThat(v[1]).isCloseTo(0.8f, within(1e-6f));
    }

    @Test
    void normalizar_deve_ser_idempotente() {
        float[] a = Vectors.normalizarL2(new float[]{3, 4});
        float[] b = Vectors.normalizarL2(a);

        assertThat(b).isEqualTo(a);
    }

    @Test
    void normalizar_deve_lidar_com_vetor_zero_sem_dividir_por_zero() {
        float[] v = Vectors.normalizarL2(new float[]{0, 0, 0});

        assertThat(v).containsExactly(0f, 0f, 0f);
    }

    @Test
    void similaridade_cosseno_de_vetores_iguais_e_1() {
        float[] a = Vectors.normalizarL2(new float[]{1, 2, 3});
        float[] b = Vectors.normalizarL2(new float[]{1, 2, 3});

        assertThat(Vectors.similaridadeCosseno(a, b)).isCloseTo(1.0, within(1e-6));
    }

    @Test
    void similaridade_cosseno_de_vetores_ortogonais_e_0() {
        float[] a = new float[]{1, 0};
        float[] b = new float[]{0, 1};

        assertThat(Vectors.similaridadeCosseno(a, b)).isCloseTo(0.0, within(1e-6));
    }

    @Test
    void similaridade_cosseno_deve_rejeitar_dimensoes_diferentes() {
        assertThatThrownBy(() ->
            Vectors.similaridadeCosseno(new float[]{1, 2}, new float[]{1, 2, 3}))
            .isInstanceOf(IllegalArgumentException.class);
    }
}