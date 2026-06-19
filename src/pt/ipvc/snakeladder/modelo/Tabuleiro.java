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

    /**
     * Construtor da classe Tabuleiro.
     * Inicializa a matriz de casas e a lista de obstáculos, procedendo à sua geração automática.
     */
    public Tabuleiro() {
        this.casas = new Quadricula[100];
        this.obstaculos = new ArrayList<>();
        gerarQuadriculas();
        gerarObstaculosPerfeitos();
    }

    /**
     * Preenche o tabuleiro com as 100 quadrículas sequenciais.
     */
    private void gerarQuadriculas() {
        for (int i = 0; i < 100; i++) {
            casas[i] = new Quadricula(i + 1, 0, 0);
        }
    }

    /**
     * Gera e posiciona aleatoriamente as cobras e escadas no tabuleiro.
     * Assegura que são criadas exatamente 4 escadas e 4 cobras, respeitando regras
     * matemáticas para evitar que os obstáculos se cruzem visualmente ou se sobreponham.
     */
    private void gerarObstaculosPerfeitos() {
        Random random = new Random(12345);
        Set<Integer> casasOcupadas = new HashSet<>();
        casasOcupadas.add(1);
        casasOcupadas.add(100);

        int tentativasMax = 2000;

        // Gerar 4 Escadas
        int escadasGeradas = 0;
        int tentativas = 0;
        while (escadasGeradas < 4 && tentativas < tentativasMax) {
            tentativas++;
            int inicio = 2 + random.nextInt(75);
            int fim = inicio + 12 + random.nextInt(35);
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
            int fim = inicio - 12 - random.nextInt(35);
            if (fim < 2) fim = 2;

            if (tentarAdicionarObstaculo(inicio, fim, casasOcupadas, false)) {
                cobrasGeradas++;
            }
        }
    }

    /**
     * Tenta inserir um novo obstáculo no tabuleiro, validando todas as regras de colisão e estética.
     *
     * @param inicio A casa de partida do obstáculo.
     * @param fim A casa de destino do obstáculo.
     * @param casasOcupadas Conjunto de casas que já contêm um obstáculo.
     * @param isEscada Verdadeiro se o obstáculo for uma Escada, falso se for uma Cobra.
     * @return true se o obstáculo foi posicionado com sucesso, false caso contrário.
     */
    private boolean tentarAdicionarObstaculo(int inicio, int fim, Set<Integer> casasOcupadas, boolean isEscada) {
        if (casasOcupadas.contains(inicio) || casasOcupadas.contains(fim)) return false;

        int[] A = getCoordenadas(inicio);
        int[] B = getCoordenadas(fim);

        if (Math.abs(A[0] - B[0]) > 6) return false;
        if (Math.abs(A[1] - B[1]) < 2) return false;

        for (Obstaculo obs : obstaculos) {
            int[] C = getCoordenadas(obs.getInicio());
            int[] D = getCoordenadas(obs.getFim());

            if (seCruzam(A, B, C, D)) return false;
            if (A[1] == C[1] || B[1] == D[1]) return false;
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

    /**
     * Verifica se existe algum obstáculo configurado para a posição especificada.
     *
     * @param posicaoAtual O número da casa onde o jogador se encontra.
     * @return O objeto {@link Obstaculo} presente na casa, ou null se for uma casa normal.
     */
    public Obstaculo verificarObstaculo(int posicaoAtual) {
        for (Obstaculo obs : obstaculos) {
            if (obs.getInicio() == posicaoAtual) {
                return obs;
            }
        }
        return null;
    }

    /**
     * Devolve o array com todas as quadrículas (casas) que compõem o tabuleiro.
     * * @return Um array contendo os objetos {@link Quadricula} do jogo.
     */
    public Quadricula[] getCasas() {
        return casas;
    }

    /**
     * Devolve a lista de todos os obstáculos (cobras e escadas) gerados no tabuleiro.
     * * @return Uma lista com os objetos {@link Obstaculo} posicionados.
     */
    public List<Obstaculo> getObstaculos() {
        return obstaculos;
    }
}