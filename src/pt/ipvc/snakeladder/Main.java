package pt.ipvc.snakeladder;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application { // [cite: 21]

    @Override
    public void start(Stage primaryStage) {
        // Layout principal da aplicação
        BorderPane root = new BorderPane();

        // Topo: MenuBar com opções de jogo [cite: 31]
        MenuBar menuBar = new MenuBar();
        Menu menuFicheiro = new Menu("Ficheiro");
        MenuItem novoJogo = new MenuItem("Novo Jogo");
        MenuItem carregarTabuleiro = new MenuItem("Carregar Tabuleiro");
        menuFicheiro.getItems().addAll(novoJogo, carregarTabuleiro);
        menuBar.getMenus().add(menuFicheiro);
        root.setTop(menuBar);

        // Centro: Canvas para desenhar o tabuleiro
        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D(); // Linha NOVA
        desenharTabuleiro(gc);                              // Linha NOVA
        root.setCenter(canvas);

        // Lateral: Painel de controlo e informação [cite: 33]
        VBox painelLateral = new VBox(15);
        painelLateral.setStyle("-fx-padding: 20px;");
        Button btnLancarDado = new Button("Lançar Dado");
        painelLateral.getChildren().add(btnLancarDado);
        root.setRight(painelLateral);

        // Configurar a Janela
        Scene scene = new Scene(root, 800, 650);
        primaryStage.setTitle("Snake and Ladder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void desenharTabuleiro(GraphicsContext gc) {
        int tamanhoCasa = 60; // 600px de largura / 10 colunas = 60px por casa

        for (int linha = 0; linha < 10; linha++) {
            for (int coluna = 0; coluna < 10; coluna++) {
                int x = coluna * tamanhoCasa;
                int y = linha * tamanhoCasa;

                // Desenhar o fundo das casas com cores alternadas
                if ((linha + coluna) % 2 == 0) {
                    gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
                } else {
                    gc.setFill(javafx.scene.paint.Color.WHITE);
                }
                gc.fillRect(x, y, tamanhoCasa, tamanhoCasa);

                // Desenhar a borda preta em volta de cada casa
                gc.setStroke(javafx.scene.paint.Color.BLACK);
                gc.strokeRect(x, y, tamanhoCasa, tamanhoCasa);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}