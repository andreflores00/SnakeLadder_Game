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

public class Main extends Application { // [cite: 21]

    private Jogador jogador1;
    private Circle pecaGrafica;
    private final int TAMANHO_CASA = 60;

    @Override
    public void start(Stage primaryStage) {
        // Inicializa o jogador na casa 1
        jogador1 = new Jogador(Color.DODGERBLUE);

        // Layout principal da aplicação
        BorderPane root = new BorderPane();

        // --- TOPO: Menus ---
        MenuBar menuBar = new MenuBar();
        Menu menuFicheiro = new Menu("Ficheiro");
        menuFicheiro.getItems().addAll(new MenuItem("Novo Jogo"), new MenuItem("Carregar Tabuleiro"));
        menuBar.getMenus().add(menuFicheiro);
        root.setTop(menuBar);

        // --- CENTRO: Tabuleiro e Peças --- [cite: 32]
        StackPane areaJogo = new StackPane();

        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D(); // [cite: 37]
        desenharTabuleiro(gc);
        desenharObstaculosVisuais(gc); // Desenha as nossas cobras e escadas bonitas

        Pane camadaPecas = new Pane(); // Painel transparente só para as peças (movimento suave)
        camadaPecas.setPrefSize(600, 600);

        // Criar a peça gráfica (um círculo)
        pecaGrafica = new Circle(TAMANHO_CASA / 2.5, jogador1.getCor());

        // Posição inicial (Casa 1)
        atualizarPosicaoGrafica(1);
        camadaPecas.getChildren().add(pecaGrafica);

        areaJogo.getChildren().addAll(canvas, camadaPecas);
        root.setCenter(areaJogo);

        // --- LATERAL: Painel de controlo ---
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

        // Ação do Botão (Lançar o dado) [cite: 15]
        btnLancarDado.setOnAction(e -> {
            int valorDado = (int) (Math.random() * 6) + 1; // Gera valor entre 1 e 6 [cite: 13]
            lblDado.setText("🎲 " + valorDado);
            jogador1.mover(valorDado); // [cite: 16]
        });

        // O Listener (Padrão Observer): Ouve mudanças na posição e atualiza a interface gráfica [cite: 40, 41]
        jogador1.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            lblPosicao.setText("Casa: " + newVal.intValue());
            atualizarPosicaoGrafica(newVal.intValue());
        });

        painelLateral.getChildren().addAll(lblTitulo, lblDado, lblPosicao, btnLancarDado);
        root.setRight(painelLateral);

        // --- Configurar a Janela ---
        Scene scene = new Scene(root, 800, 650);
        primaryStage.setTitle("Snake and Ladder - Laboratório de Programação");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void desenharTabuleiro(GraphicsContext gc) {
        for (int linha = 0; linha < 10; linha++) {
            for (int coluna = 0; coluna < 10; coluna++) {
                int x = coluna * TAMANHO_CASA;
                int y = linha * TAMANHO_CASA;

                // Cores alternadas do xadrez
                if ((linha + coluna) % 2 == 0) {
                    gc.setFill(Color.web("#e9ecef"));
                } else {
                    gc.setFill(Color.WHITE);
                }
                gc.fillRect(x, y, TAMANHO_CASA, TAMANHO_CASA);
                gc.setStroke(Color.web("#ced4da"));
                gc.strokeRect(x, y, TAMANHO_CASA, TAMANHO_CASA);

                // Escrever os números das casas na grelha
                int numeroCasa = calcularNumeroCasa(linha, coluna);
                gc.setFill(Color.web("#adb5bd"));
                gc.setFont(Font.font("System", 10));
                gc.fillText(String.valueOf(numeroCasa), x + 5, y + 15);
            }
        }
    }

    private void desenharObstaculosVisuais(GraphicsContext gc) {
        // --- DESENHAR UMA ESCADA (Textura/Cor de Madeira) ---
        gc.setStroke(Color.SADDLEBROWN);
        gc.setLineWidth(4);

        // As duas calhas laterais da escada
        gc.strokeLine(260, 575, 380, 515);
        gc.strokeLine(275, 585, 395, 525);

        // Os degraus da escada
        gc.setLineWidth(3);
        gc.strokeLine(265, 565, 280, 575);
        gc.strokeLine(295, 550, 310, 560);
        gc.strokeLine(325, 535, 340, 545);
        gc.strokeLine(355, 520, 370, 530);

        // --- DESENHAR UMA COBRA (Corpo ondulado, cabeça e olhos) ---
        gc.setStroke(Color.FORESTGREEN);
        gc.setLineWidth(8); // Corpo gordinho para a cobra

        // Curva de Bézier para criar o efeito ziguezague a rastejar
        gc.beginPath();
        gc.moveTo(390, 450); // Cauda (Perto da Casa 27)
        gc.bezierCurveTo(340, 480, 440, 530, 390, 570); // Curvatura
        gc.stroke();

        // Desenhar a cabeça da cobra (Oval)
        gc.setFill(Color.FORESTGREEN);
        gc.fillOval(380, 560, 20, 20);

        // Desenhar os olhos (Branco com pupila preta)
        gc.setFill(Color.WHITE);
        gc.fillOval(384, 566, 5, 5);
        gc.fillOval(392, 566, 5, 5);
        gc.setFill(Color.BLACK);
        gc.fillOval(385, 567, 2, 2);
        gc.fillOval(393, 567, 2, 2);

        // Desenhar a língua bifurcada em vermelho
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(390, 580, 390, 590); // Base da língua
        gc.strokeLine(390, 590, 385, 595); // Ponta esquerda
        gc.strokeLine(390, 590, 395, 595); // Ponta direita

        // Repor a espessura da linha original
        gc.setLineWidth(1);
    }

    // Lógica matemática para as casas ficarem em ziguezague
    private int calcularNumeroCasa(int linha, int coluna) {
        int linhaInvertida = 9 - linha;
        if (linhaInvertida % 2 == 0) {
            return (linhaInvertida * 10) + coluna + 1; // Esquerda para a direita
        } else {
            return (linhaInvertida * 10) + (9 - coluna) + 1; // Direita para a esquerda
        }
    }

    // Calcula as coordenadas X e Y onde a peça deve ser desenhada
    private void atualizarPosicaoGrafica(int numeroCasa) {
        if (numeroCasa > 100) numeroCasa = 100; // O tabuleiro termina na casa 100 [cite: 7]

        int zeroIndex = numeroCasa - 1;
        int linhaInvertida = zeroIndex / 10;
        int coluna = zeroIndex % 10;

        if (linhaInvertida % 2 != 0) {
            coluna = 9 - coluna;
        }

        int linhaGrafica = 9 - linhaInvertida;

        // Calcula o centro exato da casa
        double x = (coluna * TAMANHO_CASA) + (TAMANHO_CASA / 2.0);
        double y = (linhaGrafica * TAMANHO_CASA) + (TAMANHO_CASA / 2.0);

        // Posiciona a peça no ecrã
        pecaGrafica.setCenterX(x);
        pecaGrafica.setCenterY(y);
    }
}