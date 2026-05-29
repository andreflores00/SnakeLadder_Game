package pt.ipvc.snakeladder.modelo;

public class Cobra extends Obstaculo {
    public Cobra(int inicio, int fim) {
        super(inicio, fim);
        // Validação defensiva: a cobra tem sempre de descer
        if (inicio <= fim) {
            throw new IllegalArgumentException("Erro: A cabeça da cobra tem de estar numa casa superior à cauda.");
        }
    }
}