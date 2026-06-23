package pt.ipvc.snakeladder.modelo;

/**
 * Representa o obstáculo "Cobra" no tabuleiro.
 * Herda da classe base Obstaculo e aplica uma penalização ao jogador,
 * forçando-o a recuar da cabeça para a cauda (uma posição inferior).
 *
 * @author André e Eduardo
 * @version 1.0
 */
public class Cobra extends Obstaculo {

    /**
     * Construtor da classe Cobra.
     *
     * @param inicio A casa onde se encontra a cabeça da cobra (maior).
     * @param fim A casa onde se encontra a cauda da cobra (menor).
     * @throws IllegalArgumentException Se a casa de início for menor ou igual à de fim.
     */
    public Cobra(int inicio, int fim) {
        super(inicio, fim);
        if (inicio <= fim) {
            throw new IllegalArgumentException("A cobra tem de descer.");
        }
    }

    /**
     * Aplica o efeito negativo da cobra ao jogador, forçando-o a recuar no tabuleiro até à casa de destino.
     *
     * @param j O jogador que aterrou na casa onde se encontra a cabeça da cobra.
     */
    @Override
    public void aplicar(Jogador j) {
        int diferenca = getFim() - j.getPosicao();
        j.mover(diferenca);
        System.out.println("Oh não! Apanhou uma cobra. Desceu para a casa " + getFim());
    }
}