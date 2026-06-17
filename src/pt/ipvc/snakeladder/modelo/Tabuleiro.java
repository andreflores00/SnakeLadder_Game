/**
 * Representa o tabuleiro do jogo.
 * Gere a coleção de obstáculos (cobras e escadas) e fornece métodos para
 * verificar se uma determinada casa tem uma ação especial associada,
 * aplicando assim a lógica do percurso.
 *
 * @author André e Eduardo
 * @version 1.0
 */
package pt.ipvc.snakeladder.modelo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Tabuleiro {
    private Quadricula[] casas;
    private List<Obstaculo> obstaculos;

    public Tabuleiro() {
        this.casas = new Quadricula[100];
        this.obstaculos = new ArrayList<>();
        gerarQuadriculas();
        gerarObstaculosPerfeitos();
    }

    private void gerarQuadriculas() {
        for (int i = 0; i < 100; i++) {
            casas[i] = new Quadricula(i + 1, 0, 0);
        }
    }

    private void gerarObstaculosPerfeitos() {
        Random random = new Random();
        Set<Integer> casasOcupadas = new HashSet<>();
        casasOcupadas.add(1);
        casasOcupadas.add(100);

        int tentativasMax = 2000; // Mais tentativas para ter a certeza que encontra lugar

        // Gerar 4 Escadas
        int escadasGeradas = 0;
        int tentativas = 0;
        while (escadasGeradas < 4 && tentativas < tentativasMax) {
            tentativas++;
            int inicio = 2 + random.nextInt(75);
            int fim = inicio + 12 + random.nextInt(35); // Podem ser um pouco mais compridas
            if (fim > 99) fim = 99;

            if (tentarAdicionarObstaculo(inicio, fim, casasOcupadas, true)) {
                escadasGeradas++;
            }
        }

        // Gerar 4 Cobras
        int cobrasGeradas = 0;
        tentativas = 0;
        while (cobrasGeradas < 4 && tentativas < tentativasMax) {
            tentativas++;
            int inicio = 25 + random.nextInt(74);
            int fim = inicio - 12 - random.nextInt(35); // Podem ser um pouco mais compridas
            if (fim < 2) fim = 2;

            if (tentarAdicionarObstaculo(inicio, fim, casasOcupadas, false)) {
                cobrasGeradas++;
            }
        }
    }

    private boolean tentarAdicionarObstaculo(int inicio, int fim, Set<Integer> casasOcupadas, boolean isEscada) {
        // Se a casa já tem algo, rejeita
        if (casasOcupadas.contains(inicio) || casasOcupadas.contains(fim)) return false;

        int[] A = getCoordenadas(inicio);
        int[] B = getCoordenadas(fim);

        // REGRA 1: Não permitir obstáculos demasiado deitados/horizontais
        if (Math.abs(A[0] - B[0]) > 6) return false;

        // REGRA 2: Não permitir obstáculos curtos demais (têm de subir/descer pelo menos 2 andares)
        if (Math.abs(A[1] - B[1]) < 2) return false;

        for (Obstaculo obs : obstaculos) {
            int[] C = getCoordenadas(obs.getInicio());
            int[] D = getCoordenadas(obs.getFim());

            // REGRA 3: O mais importante! Impede que se cruzem em "X" no ecrã
            if (seCruzam(A, B, C, D)) return false;

            // REGRA 4: Restrição suavizada - Só impede de começarem ou acabarem exatamente na mesma linha (andar)
            if (A[1] == C[1] || B[1] == D[1]) return false;

            // REGRA 5: Distância mínima para não asfixiar o tabuleiro
            if (Math.abs(inicio - obs.getInicio()) <= 2 || Math.abs(fim - obs.getFim()) <= 2) return false;
        }

        casasOcupadas.add(inicio);
        casasOcupadas.add(fim);
        if (isEscada) {
            obstaculos.add(new Escada(inicio, fim));
        } else {
            obstaculos.add(new Cobra(inicio, fim));
        }

        return true;
    }

    // --- ALGORITMOS MATEMÁTICOS ---

    private int[] getCoordenadas(int numero) {
        int zeroIndex = numero - 1;
        int linha = zeroIndex / 10;
        int coluna = zeroIndex % 10;
        if (linha % 2 != 0) coluna = 9 - coluna;
        return new int[]{coluna, linha};
    }

    private boolean ccw(int[] A, int[] B, int[] C) {
        return (C[1] - A[1]) * (B[0] - A[0]) > (B[1] - A[1]) * (C[0] - A[0]);
    }

    private boolean seCruzam(int[] A, int[] B, int[] C, int[] D) {
        return ccw(A, C, D) != ccw(B, C, D) && ccw(A, B, C) != ccw(A, B, D);
    }

    public void adicionarObstaculo(Obstaculo obs) {
        obstaculos.add(obs);
    }

    public Obstaculo verificarObstaculo(int posicaoAtual) {
        for (Obstaculo obs : obstaculos) {
            if (obs.getInicio() == posicaoAtual) {
                return obs;
            }
        }
        return null;
    }

    public Quadricula[] getCasas() { return casas; }
    public List<Obstaculo> getObstaculos() { return obstaculos; }
}