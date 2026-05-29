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
        jogador1 = new Jogador(Color.DODGERBLUE);

        BorderPane root = new BorderPane(); // [cite: 30]

        // --- TOPO: Menus ---
        MenuBar menuBar = new MenuBar();
        Menu menuFicheiro = new Menu("Ficheiro");
        menuFicheiro.getItems().addAll(new MenuItem("Novo Jogo"), new MenuItem("Carregar Tabuleiro"));
        menuBar.getMenus().add(menuFicheiro);
        root.setTop(menuBar); // [cite: 31]

        // --- CENTRO: Tabuleiro e Peças ---
        StackPane areaJogo = new StackPane();

        Canvas canvas = new Canvas(600, 600); // [cite: 32]
        GraphicsContext gc = canvas.getGraphicsContext2D(); // [cite: 37]
        desenharTabuleiro(gc);
        desenharObstaculosVisuais(gc); // Desenha a cobra e a escada dinamicamente

        Pane camadaPecas = new Pane();
        camadaPecas.setPrefSize(600, 600);

        pecaGrafica = new Circle(TAMANHO_CASA / 2.5, jogador1.getCor());
        atualizarPosicaoGrafica(1);
        camadaPecas.getChildren().add(pecaGrafica);

        areaJogo.getChildren().addAll(canvas, camadaPecas);
        root.setCenter(areaJogo); // [cite: 32]

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

        Button btnLancarDado = new Button("Lançar Dado"); // [cite: 33]
        btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #0d6efd; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 5px; -fx-cursor: hand;");

        // Ação do Botão
        btnLancarDado.setOnAction(e -> {
            int valorDado = (int) (Math.random() * 6) + 1; // [cite: 15]
            lblDado.setText("🎲 " + valorDado);
            jogador1.mover(valorDado); // [cite: 16]
        });

        // O Listener
        jogador1.posicaoProperty().addListener((obs, oldVal, newVal) -> { // [cite: 40]
            lblPosicao.setText("Casa: " + newVal.intValue());
            atualizarPosicaoGrafica(newVal.intValue()); // [cite: 40]
        });

        painelLateral.getChildren().addAll(lblTitulo, lblDado, lblPosicao, btnLancarDado);
        root.setRight(painelLateral); // [cite: 33]

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

                if ((linha + coluna) % 2 == 0) {
                    gc.setFill(Color.web("#e9ecef"));
                } else {
                    gc.setFill(Color.WHITE);
                }
                gc.fillRect(x, y, TAMANHO_CASA, TAMANHO_CASA);
                gc.setStroke(Color.web("#ced4da"));
                gc.strokeRect(x, y, TAMANHO_CASA, TAMANHO_CASA);

                int numeroCasa = calcularNumeroCasa(linha, coluna);
                gc.setFill(Color.web("#adb5bd"));
                gc.setFont(Font.font("System", 10));
                gc.fillText(String.valueOf(numeroCasa), x + 5, y + 15);
            }
        }
    }

    // NOVA FUNÇÃO: Ajuda-nos a encontrar as coordenadas X e Y de qualquer casa no ecrã
    private double[] getCentroCasa(int numeroCasa) {
        if (numeroCasa > 100) numeroCasa = 100;
        int zeroIndex = numeroCasa - 1;
        int linhaInvertida = zeroIndex / 10;
        int coluna = zeroIndex % 10;

        if (linhaInvertida % 2 != 0) {
            coluna = 9 - coluna;
        }

        int linhaGrafica = 9 - linhaInvertida;

        double x = (coluna * TAMANHO_CASA) + (TAMANHO_CASA / 2.0);
        double y = (linhaGrafica * TAMANHO_CASA) + (TAMANHO_CASA / 2.0);

        return new double[]{x, y};
    }

    private void desenharObstaculosVisuais(GraphicsContext gc) {
        java.util.Random random = new java.util.Random();
        // Conjunto para garantir que não usamos a mesma casa duas vezes
        java.util.Set<Integer> casasOcupadas = new java.util.HashSet<>();

        // --- GERAR ESCADA ALEATÓRIA ---
        int escadaInicio;
        do {
            escadaInicio = 5 + random.nextInt(15); // Casa entre 5 e 20
        } while (casasOcupadas.contains(escadaInicio));
        casasOcupadas.add(escadaInicio);

        int escadaFim = escadaInicio + 30 + random.nextInt(40);

        // --- GERAR COBRA ALEATÓRIA ---
        int cobraInicio;
        do {
            cobraInicio = 60 + random.nextInt(30); // Casa entre 60 e 90
        } while (casasOcupadas.contains(cobraInicio));
        casasOcupadas.add(cobraInicio);

        int cobraFim = cobraInicio - 30 - random.nextInt(20);

        // --- DESENHO DA ESCADA ---
        double[] pEscadaInicio = getCentroCasa(escadaInicio);
        double[] pEscadaFim = getCentroCasa(escadaFim);

        gc.setStroke(Color.SADDLEBROWN);
        gc.setLineWidth(4);
        double anguloEscada = Math.atan2(pEscadaFim[1] - pEscadaInicio[1], pEscadaFim[0] - pEscadaInicio[0]);
        double offsetX = Math.sin(anguloEscada) * 12;
        double offsetY = -Math.cos(anguloEscada) * 12;

        gc.strokeLine(pEscadaInicio[0] + offsetX, pEscadaInicio[1] + offsetY, pEscadaFim[0] + offsetX, pEscadaFim[1] + offsetY);
        gc.strokeLine(pEscadaInicio[0] - offsetX, pEscadaInicio[1] - offsetY, pEscadaFim[0] - offsetX, pEscadaFim[1] - offsetY);

        // Desenhar degraus
        gc.setLineWidth(3);
        for (int i = 1; i <= 6; i++) {
            double fracao = (double) i / 7;
            double posX = pEscadaInicio[0] + (pEscadaFim[0] - pEscadaInicio[0]) * fracao;
            double posY = pEscadaInicio[1] + (pEscadaFim[1] - pEscadaInicio[1]) * fracao;
            gc.strokeLine(posX + offsetX, posY + offsetY, posX - offsetX, posY - offsetY);
        }

        // --- DESENHO DA COBRA ---
        double[] pCobraInicio = getCentroCasa(cobraInicio);
        double[] pCobraFim = getCentroCasa(cobraFim);

        gc.setStroke(Color.FORESTGREEN);
        gc.setLineWidth(8);
        double dist = Math.hypot(pCobraFim[0] - pCobraInicio[0], pCobraFim[1] - pCobraInicio[1]);
        double anguloCobra = Math.atan2(pCobraFim[1] - pCobraInicio[1], pCobraFim[0] - pCobraInicio[0]);

        double cp1X = pCobraInicio[0] + Math.cos(anguloCobra) * (dist * 0.3) + Math.sin(anguloCobra) * 40;
        double cp1Y = pCobraInicio[1] + Math.sin(anguloCobra) * (dist * 0.3) - Math.cos(anguloCobra) * 40;
        double cp2X = pCobraInicio[0] + Math.cos(anguloCobra) * (dist * 0.7) - Math.sin(anguloCobra) * 40;
        double cp2Y = pCobraInicio[1] + Math.sin(anguloCobra) * (dist * 0.7) + Math.cos(anguloCobra) * 40;

        gc.beginPath();
        gc.moveTo(pCobraInicio[0], pCobraInicio[1]);
        gc.bezierCurveTo(cp1X, cp1Y, cp2X, cp2Y, pCobraFim[0], pCobraFim[1]);
        gc.stroke();

        // Cabeça e Olhos
        gc.setFill(Color.FORESTGREEN);
        gc.fillOval(pCobraInicio[0] - 10, pCobraInicio[1] - 10, 20, 20);
        gc.setFill(Color.WHITE);
        gc.fillOval(pCobraInicio[0] - 6, pCobraInicio[1] - 4, 5, 5);
        gc.fillOval(pCobraInicio[0] + 2, pCobraInicio[1] - 4, 5, 5);

        gc.setLineWidth(1); // Reset
    }

    private int calcularNumeroCasa(int linha, int coluna) {
        int linhaInvertida = 9 - linha;
        if (linhaInvertida % 2 == 0) {
            return (linhaInvertida * 10) + coluna + 1;
        } else {
            return (linhaInvertida * 10) + (9 - coluna) + 1;
        }
    }

    private void atualizarPosicaoGrafica(int numeroCasa) {
        // Agora usamos a nossa função auxiliar para não repetir código!
        double[] pos = getCentroCasa(numeroCasa);
        pecaGrafica.setCenterX(pos[0]);
        pecaGrafica.setCenterY(pos[1]);
    }
}
