package pt.ipvc.snakeladder;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;

public class Jogador {
    private SimpleIntegerProperty posicao;
    private Color cor;

    public Jogador(Color cor) {
        this.cor = cor;
        // O jogo dita que a peça começa na casa 1
        this.posicao = new SimpleIntegerProperty(1);
    }

    public void mover(int casas) {
        // Atualiza a posição somando o valor do dado
        posicao.set(posicao.get() + casas);
    }

    // Método essencial para o Listener do JavaFX
    public SimpleIntegerProperty posicaoProperty() {
        return posicao;
    }

    public int getPosicao() {
        return posicao.get();
    }

    public Color getCor() {
        return cor;
    }
}