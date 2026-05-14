package pt.ipvc.snakeladder;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

    private Jogador jogador1;
    private Circle pecaGrafica;
    private final int TAMANHO_CASA = 60;

    public static void main(String[] args) {
    }

    @Override
    public void start(Stage primaryStage) {
        jogador1 = new Jogador(Color.DODGERBLUE);

        BorderPane root = new BorderPane();

        // --- TOPO ---
        MenuBar menuBar = new MenuBar();
        Menu menuFicheiro = new Menu("Ficheiro");
        menuFicheiro.getItems().addAll(new MenuItem("Novo Jogo"), new MenuItem("Carregar Tabuleiro"));
        menuBar.getMenus().add(menuFicheiro);
        root.setTop(menuBar);

        // --- CENTRO (A grande melhoria com StackPane!) ---
        StackPane areaJogo = new StackPane();

        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        desenharTabuleiro(gc);

        Pane camadaPecas = new Pane(); // Painel transparente só para as peças
        camadaPecas.setPrefSize(600, 600);

        // Criar a peça gráfica (um círculo)
        pecaGrafica = new Circle(TAMANHO_CASA / 2.5, jogador1.getCor());

        // Posição inicial (Casa 1)
        atualizarPosicaoGrafica(1);
        camadaPecas.getChildren().add(pecaGrafica);

        areaJogo.getChildren().addAll(canvas, camadaPecas);
        root.setCenter(areaJogo);

        // --- LATERAL (Visual melhorado) ---
        VBox painelLateral = new VBox(20);
        painelLateral.setStyle("-fx-padding: 30px; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 0 1px;");
        painelLateral.setAlignment(Pos.TOP_CENTER);
        painelLateral.setPrefWidth(200);

        Label lblTitulo = new Label("JOGADOR 1");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(jogador1.getCor());

        Label lblDado = new Label("🎲");
        lblDado.setFont(Font.font("System", 45));

        Label lblPosicao = new Label("Casa: 1");
        lblPosicao.setFont(Font.font("System", FontWeight.NORMAL, 16));

        Button btnLancarDado = new Button("Lançar Dado");
        btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #0d6efd; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 5px; -fx-cursor: hand;");

        // Ação do Botão
        btnLancarDado.setOnAction(e -> {
            int valorDado = (int) (Math.random() * 6) + 1;
            lblDado.setText("🎲 " + valorDado);
            jogador1.mover(valorDado);
        });

        // O Listener (Padrão Observer) [cite: 41]
        jogador1.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            lblPosicao.setText("Casa: " + newVal.intValue());
            atualizarPosicaoGrafica(newVal.intValue());
        });

        painelLateral.getChildren().addAll(lblTitulo, lblDado, lblPosicao, btnLancarDado);
        root.setRight(painelLateral);

        // --- JANELA ---
        Scene scene = new Scene(root, 800, 650);
        primaryStage.setTitle("Snake and Ladder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void desenharTabuleiro(GraphicsContext gc) {
        for (int linha = 0; linha < 10; linha++) {
            for (int coluna = 0; coluna < 10; coluna++) {
                int x = coluna * TAMANHO_CASA;
                int y = linha * TAMANHO_CASA;

                // Cores alternadas mais suaves
                if ((linha + coluna) % 2 == 0) {
                    gc.setFill(Color.web("#e9ecef"));
                } else {
                    gc.setFill(Color.WHITE);
                }
                gc.fillRect(x, y, TAMANHO_CASA, TAMANHO_CASA);
                gc.setStroke(Color.web("#ced4da"));
                gc.strokeRect(x, y, TAMANHO_CASA, TAMANHO_CASA);

                // Bónus: Escrever os números das casas na grelha
                int numeroCasa = calcularNumeroCasa(linha, coluna);
                gc.setFill(Color.web("#adb5bd"));
                gc.setFont(Font.font("System", 10));
                gc.fillText(String.valueOf(numeroCasa), x + 5, y + 15);
            }
        }
    }

    // Lógica para desenhar os números em ziguezague
    private int calcularNumeroCasa(int linha, int coluna) {
        int linhaInvertida = 9 - linha;
        if (linhaInvertida % 2 == 0) {
            return (linhaInvertida * 10) + coluna + 1; // Esquerda para a direita
        } else {
            return (linhaInvertida * 10) + (9 - coluna) + 1; // Direita para a esquerda
        }
    }

    // Calcula onde a peça deve estar no ecrã com base no número da casa
    private void atualizarPosicaoGrafica(int numeroCasa) {
        if (numeroCasa > 100) numeroCasa = 100; // Limite do tabuleiro

        int zeroIndex = numeroCasa - 1;
        int linhaInvertida = zeroIndex / 10;
        int coluna = zeroIndex % 10;

        if (linhaInvertida % 2 != 0) {
            coluna = 9 - coluna; // Padrão ziguezague do Snake & Ladder
        }

        int linhaGrafica = 9 - linhaInvertida;

        // Calcula o centro da casa
        double x = (coluna * TAMANHO_CASA) + (TAMANHO_CASA / 2.0);
        double y = (linhaGrafica * TAMANHO_CASA) + (TAMANHO_CASA / 2.0);

        // Move a peça para o centro
        pecaGrafica.setCenterX(x);
        pecaGrafica.setCenterY(y);
    }
}