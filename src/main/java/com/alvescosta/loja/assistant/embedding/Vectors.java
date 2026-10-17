package com.alvescosta.loja.assistant.embedding;

public final class Vectors {

    private Vectors() {
        // Classe utilitária, não deve ser instanciada
    }

    /**
     * Normaliza o vetor usando a norma L2 (distância euclidiana).
     * Retorna um novo vetor com norma igual a 1.0.
     * Se o vetor for zero, retorna uma cópia (mantém zeros) para não dividir por zero.
     */
    public static float[] normalizarL2(float[] v) {
        double somaQuadrados = 0;
        for (float x : v) {
            somaQuadrados += x * x;
        }
        double norma = Math.sqrt(somaQuadrados);

        if (norma == 0) {
            return v.clone(); // Retorna cópia para não expor o array original
        }

        float[] resultado = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            resultado[i] = (float) (v[i] / norma);
        }
        return resultado;
    }

    /**
     * Calcula a similaridade de cosseno entre dois vetores.
     * Retorna 1.0 para vetores idênticos, 0.0 para ortogonais e -1.0 para opostos.
     * Lança IllegalArgumentException se as dimensões forem diferentes.
     */
    public static double similaridadeCosseno(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Dimensões diferentes: " + a.length + " vs " + b.length);
        }

        double produtoEscalar = 0;
        double normaA = 0;
        double normaB = 0;

        for (int i = 0; i < a.length; i++) {
            produtoEscalar += a[i] * b[i];
            normaA += a[i] * a[i];
            normaB += b[i] * b[i];
        }

        if (normaA == 0 || normaB == 0) {
            return 0.0;
        }

        return produtoEscalar / (Math.sqrt(normaA) * Math.sqrt(normaB));
    }
}