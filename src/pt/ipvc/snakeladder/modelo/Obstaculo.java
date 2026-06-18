/**
 * Classe abstrata que serve de base para todos os elementos especiais do tabuleiro.
 * Define a estrutura comum para obstáculos que alteram a posição do jogador,
 * garantindo a aplicação correta do conceito de polimorfismo.
 *
 * @author André e Eduardo
 * @version 1.0
 */
package pt.ipvc.snakeladder.modelo;

public abstract class Obstaculo {
    private final int inicio;
    private final int fim;

    public Obstaculo(int inicio, int fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public int getInicio() { return inicio; }
    public int getFim() { return fim; }

    // O método exigido pelo Diagrama UML
    public abstract void aplicar(Jogador j);
}