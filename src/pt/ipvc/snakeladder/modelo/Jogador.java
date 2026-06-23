package pt.ipvc.snakeladder.modelo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;

/**
 * Representa um participante no jogo Snake and Ladder.
 * Armazena a cor da peça e a posição atual, utilizando propriedades do JavaFX
 * para notificar a interface gráfica automaticamente após cada movimento.
 *
 * @author André e Eduardo
 * @version 1.0
 */
public class Jogador {

    /** A cor escolhida para a peça deste jogador. */
    private Color cor;

    /** A propriedade reativa que guarda a posição atual no tabuleiro. */
    private IntegerProperty posicao;

    /**
     * Construtor da classe Jogador.
     * Cria um novo Jogador com uma cor visual específica e coloca-o na casa inicial (1).
     *
     * @param cor A cor (Color do JavaFX) que representará a peça do jogador no ecrã.
     */
    public Jogador(Color cor) {
        this.cor = cor;
        this.posicao = new SimpleIntegerProperty(1);
    }

    /**
     * Devolve a cor escolhida para este jogador.
     *
     * @return A cor associada a este jogador.
     */
    public Color getCor() {
        return cor;
    }

    /**
     * Permite à interface gráfica adicionar "Listeners" para detetar quando o jogador se move.
     *
     * @return A propriedade reativa (IntegerProperty) que contém a posição atual.
     */
    public IntegerProperty posicaoProperty() {
        return posicao;
    }

    /**
     * Devolve a casa exata onde o jogador se encontra no momento.
     *
     * @return O número inteiro da casa (ex: 1 a 100).
     */
    public int getPosicao() {
        return posicao.get();
    }

    /**
     * Move o jogador um determinado número de casas no tabuleiro.
     * O limite máximo de paragem é a casa 100 (vitória).
     *
     * @param casas O valor retirado no dado (ou recebido via rede) para avançar.
     */
    public void mover(int casas) {
        int novaPosicao = posicao.get() + casas;

        // Garante que o jogador não ultrapassa a casa 100
        if (novaPosicao > 100) {
            novaPosicao = 100;
        }

        posicao.set(novaPosicao);
    }
}