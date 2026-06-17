/**
 * Responsável pela geração de valores aleatórios no jogo.
 * Simula o lançamento de um dado tradicional de seis faces,
 * determinando o número de casas que o peão deve avançar.
 *
 * @author André e Eduardo
 * @version 1.0
 */
package pt.ipvc.snakeladder.modelo;

import java.util.Random;

public class Dado {
    private int valor; // O atributo definido no teu UML

    public int rolar() {
        Random random = new Random();
        this.valor = random.nextInt(6) + 1; // Guarda o valor gerado
        return this.valor;
    }

    // O método que a nova interface está a pedir!
    public int getValor() {
        return this.valor;
    }
}