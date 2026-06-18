package pt.ipvc.snakeladder.modelo;

/**
 * Representa uma casa (quadrícula) individual no tabuleiro do jogo.
 * Armazena o número lógico da casa e as suas coordenadas físicas no ecrã (x, y).
 *
 * @author André e Eduardo
 * @version 1.0
 */
public class Quadricula {
    private final int numero;
    private final int x;
    private final int y;

    /**
     * Construtor da classe Quadricula.
     *
     * @param numero O número da casa no tabuleiro (ex: 1 a 100).
     * @param x A coordenada X da quadrícula.
     * @param y A coordenada Y da quadrícula.
     */
    public Quadricula(int numero, int x, int y) {
        this.numero = numero;
        this.x = x;
        this.y = y;
    }

    /**
     * Obtém o número correspondente a esta quadrícula.
     *
     * @return O número da casa (1 a 100).
     */
    public int getNumero() {
        return numero;
    }

    /**
     * Obtém a coordenada X da quadrícula para efeitos de desenho na interface.
     *
     * @return O valor da posição X.
     */
    public int getX() {
        return x;
    }

    /**
     * Obtém a coordenada Y da quadrícula para efeitos de desenho na interface.
     *
     * @return O valor da posição Y.
     */
    public int getY() {
        return y;
    }
}