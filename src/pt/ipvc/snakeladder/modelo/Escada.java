package pt.ipvc.snakeladder.modelo;

public class Escada extends Obstaculo {
    public Escada(int inicio, int fim) {
        super(inicio, fim);
        if (inicio >= fim) {
            throw new IllegalArgumentException("A escada tem de subir.");
        }
    }

    @Override
    public void aplicar(Jogador j) {
        // A matemática para mover o jogador da base para o topo
        int diferenca = getFim() - j.getPosicao();
        j.mover(diferenca);
        System.out.println("Boa! Subiu uma escada para a casa " + getFim());
    }
}