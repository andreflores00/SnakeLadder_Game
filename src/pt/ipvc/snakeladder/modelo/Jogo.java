package pt.ipvc.snakeladder.modelo;

import java.util.ArrayList;
import java.util.List;

public class Jogo {
    private Tabuleiro tabuleiro;
    private List<Jogador> jogadores;
    private Dado dado;
    private int jogadorAtualIndex;
    private boolean jogoTerminado;

    public Jogo() {
        this.tabuleiro = new Tabuleiro();
        this.jogadores = new ArrayList<>();
        this.dado = new Dado();
        this.jogadorAtualIndex = 0;
        this.jogoTerminado = false;
    }

    public void adicionarJogador(Jogador jogador) {
        jogadores.add(jogador);
    }

    public void iniciar() {
        if (jogadores.size() < 2) {
            System.out.println("Aviso: São necessários pelo menos 2 jogadores.");
            return;
        }
        jogadorAtualIndex = 0;
        jogoTerminado = false;
        System.out.println("O jogo vai começar!");
    }

    /**
     * Processa um turno completo para o jogador atual.
     */
    public void jogarTurno() {
        if (jogoTerminado) {
            return;
        }

        Jogador jogadorAtual = jogadores.get(jogadorAtualIndex);
        int valorDado = dado.rolar();

        System.out.println("Jogador lançou um " + valorDado);
        jogadorAtual.mover(valorDado);

        // 1. Verificar se aterrou numa Cobra ou Escada
        Obstaculo obs = tabuleiro.verificarObstaculo(jogadorAtual.getPosicao());
        if (obs != null) {
            obs.aplicar(jogadorAtual); // O Polimorfismo entra em ação aqui!
        }

        // 2. Verificar a condição de Vitória (Chegar à casa 100)
        if (jogadorAtual.getPosicao() >= 100) {
            jogoTerminado = true;
            System.out.println("VITÓRIA! O jogo terminou.");
        } else {
            // 3. Passar a vez ao próximo jogador
            jogadorAtualIndex = (jogadorAtualIndex + 1) % jogadores.size();
        }
    }

    // Getters
    public Tabuleiro getTabuleiro() { return tabuleiro; }
    public List<Jogador> getJogadores() { return jogadores; }
    public Dado getDado() { return dado; }
    public int getJogadorAtualIndex() { return jogadorAtualIndex; }
    public boolean isJogoTerminado() { return jogoTerminado; }
}