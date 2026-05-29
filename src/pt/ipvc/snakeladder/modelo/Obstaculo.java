package pt.ipvc.snakeladder.modelo;

public abstract class Obstaculo {
    private final int inicio;
    private final int fim;

    public Obstaculo(int inicio, int fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public int getInicio() {
        return inicio;
    }

    public int getFim() {
        return fim;
    }
}