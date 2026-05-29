package pt.ipvc.snakeladder.modelo;

public class Escada extends Obstaculo {
    public Escada(int inicio, int fim) {
        super(inicio, fim);
        // Validação defensiva: a escada tem sempre de subir
        if (inicio >= fim) {
            throw new IllegalArgumentException("Erro: A base da escada tem de estar numa casa inferior ao topo.");
        }
    }
}