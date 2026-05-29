package pt.ipvc.snakeladder.modelo;

import java.util.Random;

public class Dado {
    private final int faces;
    private final Random random;

    public Dado() {
        this.faces = 6;
        this.random = new Random();
    }

    /**
     * Rola o dado e devolve um valor aleatório.
     * @return um número inteiro entre 1 e 6.
     */
    public int rolar() {
        return random.nextInt(faces) + 1;
    }
}