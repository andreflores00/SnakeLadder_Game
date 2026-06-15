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
        if (jogadores.size() < 2) return;
        jogadorAtualIndex = 0;
        jogoTerminado = false;
    }

    // Método antigo (usado para jogo local)
    public void jogarTurno() {
        jogarTurno(dado.rolar());
    }

    // NOVO MÉTODO (Essencial para a Rede Sincronizar)
    public void jogarTurno(int valorDadoForcado) {
        if (jogoTerminado) return;

        Jogador jogadorAtual = jogadores.get(jogadorAtualIndex);
        jogadorAtual.mover(valorDadoForcado);

        Obstaculo obs = tabuleiro.verificarObstaculo(jogadorAtual.getPosicao());
        if (obs != null) {
            obs.aplicar(jogadorAtual);
        }

        if (jogadorAtual.getPosicao() >= 100) {
            jogoTerminado = true;
        } else {
            jogadorAtualIndex = (jogadorAtualIndex + 1) % jogadores.size();
        }
    }

    public Tabuleiro getTabuleiro() { return tabuleiro; }
    public List<Jogador> getJogadores() { return jogadores; }
    public Dado getDado() { return dado; }
    public int getJogadorAtualIndex() { return jogadorAtualIndex; }
    public boolean isJogoTerminado() { return jogoTerminado; }
}