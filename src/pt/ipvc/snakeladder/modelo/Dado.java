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
    private int valor;

    /**
     * Simula o lançamento de um dado convencional de 6 faces.
     *
     * @return O valor resultante do lançamento (entre 1 e 6).
     */
    public int rolar() {
        Random random = new Random();
        this.valor = random.nextInt(6) + 1;
        return this.valor;
    }

    /**
     * Devolve o valor guardado em memória referente ao último lançamento do dado.
     *
     * @return O valor numérico do último lançamento.
     */
    public int getValor() {
        return this.valor;
    }
}