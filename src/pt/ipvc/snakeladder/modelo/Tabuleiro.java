package pt.ipvc.snakeladder.modelo;

import java.util.ArrayList;
import java.util.List;

public class Tabuleiro {
    // Array de 100 posições conforme o diagrama UML
    private Quadricula[] casas;
    private List<Obstaculo> obstaculos;

    public Tabuleiro() {
        this.casas = new Quadricula[100];
        this.obstaculos = new ArrayList<>();
        gerarQuadriculas();
    }

    private void gerarQuadriculas() {
        for (int i = 0; i < 100; i++) {
            int numero = i + 1;
            // Para a lógica de consola/modelo, os valores X e Y podem ser 0 inicialmente
            // A interface gráfica (Main) tratará das coordenadas reais depois
            casas[i] = new Quadricula(numero, 0, 0);
        }
    }

    public void adicionarObstaculo(Obstaculo obs) {
        obstaculos.add(obs);
    }

    // Método atualizado que devolve o obstáculo se ele existir na casa atual
    public Obstaculo verificarObstaculo(int posicaoAtual) {
        for (Obstaculo obs : obstaculos) {
            if (obs.getInicio() == posicaoAtual) {
                return obs; // Devolve a Cobra ou Escada encontrada
            }
        }
        return null; // Não há obstáculo nesta casa
    }

    public Quadricula[] getCasas() { return casas; }
    public List<Obstaculo> getObstaculos() { return obstaculos; }
}