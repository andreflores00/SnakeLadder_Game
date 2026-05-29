package pt.ipvc.snakeladder.modelo;

public class Quadricula {
    private final int numero;
    private final int x;
    private final int y;

    public Quadricula(int numero, int x, int y) {
        this.numero = numero;
        this.x = x;
        this.y = y;
    }

    public int getNumero() {
        return numero;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}