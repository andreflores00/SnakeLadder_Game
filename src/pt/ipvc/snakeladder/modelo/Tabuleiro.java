package pt.ipvc.snakeladder.modelo;

import java.util.ArrayList;
import java.util.List;

public class Tabuleiro {
    private final int tamanho = 100;
    private final List<Obstaculo> obstaculos;

    public Tabuleiro() {
        this.obstaculos = new ArrayList<>();
    }

    public void adicionarObstaculo(Obstaculo obs) {
        obstaculos.add(obs);
    }

    /**
     * Verifica se a casa tem uma cobra ou escada.
     * @param posicaoAtual a casa onde o jogador aterrou.
     * @return a casa de destino (se houver obstáculo) ou a própria casa se estiver vazia.
     */
    public int verificarCasa(int posicaoAtual) {
        for (Obstaculo obs : obstaculos) {
            if (obs.getInicio() == posicaoAtual) {
                return obs.getFim(); // O jogador "viaja" pelo obstáculo
            }
        }
        return posicaoAtual; // Fica no mesmo sítio
    }

    public int getTamanho() {
        return tamanho;
    }
}