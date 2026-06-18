/**
 * Representa o obstáculo "Escada" no tabuleiro.
 * Herda da classe base Obstaculo e atua como um atalho estratégico,
 * movendo o jogador da base para o topo (uma posição superior).
 *
 * @author André e Eduardo
 * @version 1.0
 */
package pt.ipvc.snakeladder.modelo;

public class Escada extends Obstaculo {
    public Escada(int inicio, int fim) {
        super(inicio, fim);
        if (inicio >= fim) {
            throw new IllegalArgumentException("A escada tem de subir.");
        }
    }

    /**
     * Aplica o efeito positivo da escada ao jogador, fazendo-o avançar no tabuleiro até ao topo da escada.
     *
     * @param j O {@link Jogador} que aterrou na casa onde se encontra a base da escada.
     */
    @Override
    public void aplicar(Jogador j) {
        int diferenca = getFim() - j.getPosicao();
        j.mover(diferenca);
        System.out.println("Boa! Subiu uma escada para a casa " + getFim());
    }
}