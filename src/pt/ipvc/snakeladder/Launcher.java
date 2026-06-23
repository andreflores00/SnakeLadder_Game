package pt.ipvc.snakeladder;

import javafx.application.Application;

/**
 * Ponto de entrada (Entry Point) da aplicação.
 * É utilizado para contornar limitações de inicialização do JavaFX
 * quando a aplicação é empacotada em ficheiros .jar.
 *
 * @author André e Eduardo
 * @version 1.0
 */
public class Launcher {

    /**
     * Método principal responsável por arrancar a aplicação.
     * Delega a execução imediatamente para a classe Main do JavaFX.
     *
     * @param args Argumentos de linha de comandos passados no arranque do programa.
     */
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}