package pt.ipvc.snakeladder.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe principal que atua como o motor lógico do jogo Snake and Ladder.
 * É responsável por gerir o estado da partida, os jogadores envolvidos,
 * o tabuleiro e a transição de turnos.
 *
 * @author Dudz e Eduardo
 * @version 1.0
 */
public class Jogo {
    private Tabuleiro tabuleiro;
    private List<Jogador> jogadores;
    private Dado dado;
    private int jogadorAtualIndex;
    private boolean jogoTerminado;

    /**
     * Construtor padrão da classe Jogo.
     * Inicializa o tabuleiro, a lista de jogadores, o dado e define o estado inicial da partida.
     */
    public Jogo() {
        this.tabuleiro = new Tabuleiro();
        this.jogadores = new ArrayList<>();
        this.dado = new Dado();
        this.jogadorAtualIndex = 0;
        this.jogoTerminado = false;
    }

    /**
     * Adiciona um novo jogador à lista de participantes da partida.
     *
     * @param jogador O objeto do tipo {@link Jogador} a ser inserido no jogo.
     */
    public void adicionarJogador(Jogador jogador) {
        jogadores.add(jogador);
    }

    /**
     * Prepara e inicia uma nova partida, colocando o índice no primeiro jogador.
     * Requer pelo menos dois jogadores registados para arrancar.
     */
    public void iniciar() {
        if (jogadores.size() < 2) return;
        jogadorAtualIndex = 0;
        jogoTerminado = false;
    }

    /**
     * Executa o turno local utilizando um valor aleatório gerado pelo dado do motor.
     */
    public void jogarTurno() {
        jogarTurno(dado.rolar());
    }

    /**
     * Executa o turno do jogador atual utilizando um valor de dado específico.
     * Essencial para o modo Multiplayer, permitindo sincronizar jogadas recebidas pela rede.
     * Move o peão, verifica colisões com obstáculos e avalia a condição de vitória.
     *
     * @param valorDadoForcado O número de casas (1 a 6) que o peão deve avançar.
     */
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

    /**
     * @return O {@link Tabuleiro} atual do jogo.
     */
    public Tabuleiro getTabuleiro() { return tabuleiro; }

    /**
     * @return A lista de todos os {@link Jogador}es na partida.
     */
    public List<Jogador> getJogadores() { return jogadores; }

    /**
     * @return O {@link Dado} utilizado para sorteios locais.
     */
    public Dado getDado() { return dado; }

    /**
     * @return O índice numérico (0 ou 1) do jogador que tem a vez de jogar.
     */
    public int getJogadorAtualIndex() { return jogadorAtualIndex; }

    /**
     * @return true se o jogo já encontrou um vencedor; false caso contrário.
     */
    public boolean isJogoTerminado() { return jogoTerminado; }
}