package com.alvescosta.loja.assistant.vector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class InMemoryVectorStoreTest {

    private VectorStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryVectorStore();
    }

    @Test
    void deve_retornar_vazio_quando_store_vazio() {
        List<SearchResult> resultados = store.search(new float[]{1, 0}, 5);
        assertThat(resultados).isEmpty();
    }

    @Test
    void deve_salvar_e_recuperar_vetor_por_similaridade() {
        // Vetor A é mais próximo do [1, 0]
        store.upsert("A", new float[]{1, 0}, Map.of("name", "Produto A"));
        // Vetor B é ortogonal
        store.upsert("B", new float[]{0, 1}, Map.of("name", "Produto B"));

        List<SearchResult> resultados = store.search(new float[]{1, 0}, 1);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).id()).isEqualTo("A");
        assertThat(resultados.get(0).score()).isCloseTo(1.0, within(1e-6));
    }

    @Test
    void deve_ordenar_resultados_por_similaridade_decrescente() {
        store.upsert("A", new float[]{1, 0}, Map.of());
        store.upsert("B", new float[]{0.7f, 0.7f}, Map.of());
        store.upsert("C", new float[]{0, 1}, Map.of());

        List<SearchResult> resultados = store.search(new float[]{1, 0}, 3);

        assertThat(resultados).extracting(SearchResult::id)
            .containsExactly("A", "B", "C");
    }

    @Test
    void deve_atualizar_vetor_existente_no_upsert() {
        store.upsert("A", new float[]{1, 0}, Map.of("versao", "1"));
        store.upsert("A", new float[]{0, 1}, Map.of("versao", "2"));

        List<SearchResult> resultados = store.search(new float[]{0, 1}, 1);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).id()).isEqualTo("A");
        assertThat(resultados.get(0).metadata()).containsEntry("versao", "2");
    }

    @Test
    void deve_remover_vetor_por_id() {
        store.upsert("A", new float[]{1, 0}, Map.of());
        store.delete("A");

        List<SearchResult> resultados = store.search(new float[]{1, 0}, 1);
        assertThat(resultados).isEmpty();
    }

    @Test
    void deve_limpar_todos_os_vetores() {
        store.upsert("A", new float[]{1, 0}, Map.of());
        store.upsert("B", new float[]{0, 1}, Map.of());
        store.clear();

        assertThat(store.search(new float[]{1, 0}, 5)).isEmpty();
    }
}