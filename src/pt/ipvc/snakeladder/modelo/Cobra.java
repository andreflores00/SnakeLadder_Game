package pt.ipvc.snakeladder.modelo;

public class Cobra extends Obstaculo {
    public Cobra(int inicio, int fim) {
        super(inicio, fim);
        if (inicio <= fim) {
            throw new IllegalArgumentException("A cobra tem de descer.");
        }
    }

    @Override
    public void aplicar(Jogador j) {
        // A matemática para mover o jogador da cabeça para a cauda
        int diferenca = getFim() - j.getPosicao();
        j.mover(diferenca);
        System.out.println("Oh não! Apanhou uma cobra. Desceu para a casa " + getFim());
    }
}