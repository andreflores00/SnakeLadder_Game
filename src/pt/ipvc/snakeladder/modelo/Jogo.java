package pt.ipvc.snakeladder.modelo;

import java.util.ArrayList;
import java.util.List;

public class Jogo {
    private Tabuleiro tabuleiro;
    private List<Jogador> jogadores;
    private Dado dado;

    public Jogo() {
        this.tabuleiro = new Tabuleiro();
        this.jogadores = new ArrayList<>();
        this.dado = new Dado(); // Associação com o Dado
    }

    public void adicionarJogador(Jogador jogador) {
        jogadores.add(jogador);
    }

    public void iniciar() {
        // Lógica de arranque do jogo
        System.out.println("Jogo iniciado com " + jogadores.size() + " jogadores.");
    }

    // Getters
    public Tabuleiro getTabuleiro() { return tabuleiro; }
    public List<Jogador> getJogadores() { return jogadores; }
    public Dado getDado() { return dado; }
}