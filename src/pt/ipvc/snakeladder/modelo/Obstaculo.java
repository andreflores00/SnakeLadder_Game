package pt.ipvc.snakeladder.modelo;

/**
 * Classe abstrata que serve de base para todos os elementos especiais do tabuleiro.
 * Define a estrutura comum para obstáculos que alteram a posição do jogador,
 * garantindo a aplicação correta do conceito de polimorfismo.
 *
 * @author André e Eduardo
 * @version 1.0
 */
public abstract class Obstaculo {
    private final int inicio;
    private final int fim;

    /**
     * Construtor da classe base Obstaculo.
     *
     * @param inicio A casa onde o obstáculo começa (onde o jogador aterra).
     * @param fim A casa para onde o jogador será movido.
     */
    public Obstaculo(int inicio, int fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    /**
     * Obtém a casa de início do obstáculo.
     *
     * @return O número da casa inicial.
     */
    public int getInicio() { return inicio; }

    /**
     * Obtém a casa de destino do obstáculo.
     *
     * @return O número da casa final.
     */
    public int getFim() { return fim; }

    /**
     * Aplica o efeito do obstáculo a um determinado jogador.
     * Este método deve ser implementado pelas classes filhas (Cobra e Escada).
     *
     * @param j O jogador que aterrou no obstáculo e sofrerá o seu efeito.
     */
    public abstract void aplicar(Jogador j);
}