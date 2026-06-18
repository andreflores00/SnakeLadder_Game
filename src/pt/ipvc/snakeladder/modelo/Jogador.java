package pt.ipvc.snakeladder.modelo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;

/**
 * Representa um participante no jogo Snake and Ladder.
 * Armazena a cor da peça e a posição atual, utilizando propriedades do JavaFX
 * para notificar a interface gráfica automaticamente após cada movimento.
 *
 * @author Dudz e Eduardo
 * @version 1.0
 */
public class Jogador {
    private Color cor;
    private IntegerProperty posicao;

    /**
     * Cria um novo Jogador com uma cor visual específica e coloca-o na casa inicial (1).
     *
     * @param cor A {@link Color} que representará a peça do jogador no Canvas.
     */
    public Jogador(Color cor) {
        this.cor = cor;
        this.posicao = new SimpleIntegerProperty(1);
    }

    /**
     * @return A cor associada a este jogador.
     */
    public Color getCor() {
        return cor;
    }

    /**
     * Permite à interface gráfica adicionar "Listeners" para detetar quando o jogador se move.
     *
     * @return A propriedade reativa que contém a posição atual.
     */
    public IntegerProperty posicaoProperty() {
        return posicao;
    }

    /**
     * @return O número inteiro da casa onde o jogador se encontra no momento.
     */
    public int getPosicao() {
        return posicao.get();
    }

    /**
     * Move o jogador um determinado número de casas no tabuleiro.
     * O limite máximo é a casa 100.
     *
     * @param casas O valor retirado no dado (ou recebido via Socket) para avançar.
     */
    public void mover(int casas) {
        int novaPosicao = posicao.get() + casas;
        if (novaPosicao > 100) {
            novaPosicao = 100;
        }
        posicao.set(novaPosicao);
    }
}