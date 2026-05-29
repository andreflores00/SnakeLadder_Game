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
import pt.ipvc.snakeladder.modelo.Jogo;
import pt.ipvc.snakeladder.modelo.Jogador;

public class Main extends Application {

    private Jogo motorJogo;
    private Jogador jogador1;
    private Circle pecaGrafica;
    private final int TAMANHO_CASA = 60;

    // Componentes da barra inferior
    private Label lblTurnoStatus;
    private Label lblCorStatus;
    private Label lblEstadoDetalhado;
    private Label lblDadoResultado;

    @Override
    public void start(Stage primaryStage) {
        // Inicialização do motor de jogo oficial
        motorJogo = new Jogo();
        jogador1 = new Jogador(Color.DODGERBLUE);
        motorJogo.adicionarJogador(jogador1);
        motorJogo.iniciar();

        BorderPane root = new BorderPane();

        // --- TOPO: Menus ---
        MenuBar menuBar = new MenuBar();
        Menu menuFicheiro = new Menu("Ficheiro");
        menuFicheiro.getItems().addAll(new MenuItem("Novo Jogo"), new MenuItem("Carregar Tabuleiro"));
        menuBar.getMenus().add(menuFicheiro);
        root.setTop(menuBar);

        // --- CENTRO: Tabuleiro (StackPane) ---
        StackPane areaJogo = new StackPane();

        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        desenharTabuleiro(gc);
        desenharObstaculosVisuais(gc);

        Pane camadaPecas = new Pane();
        camadaPecas.setPrefSize(600, 600);

        pecaGrafica = new Circle(TAMANHO_CASA / 2.5, jogador1.getCor());
        atualizarPosicaoGrafica(1);
        camadaPecas.getChildren().add(pecaGrafica);

        areaJogo.getChildren().addAll(canvas, camadaPecas);
        root.setCenter(areaJogo);

        // --- LATERAL: Painel de Controlo ---
        VBox painelLateral = new VBox(20);
        painelLateral.setStyle("-fx-padding: 30px; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 0 1px;");
        painelLateral.setAlignment(Pos.TOP_CENTER);
        painelLateral.setPrefWidth(200);

        Label lblTitulo = new Label("JOGADOR 1");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(jogador1.getCor());

        Label lblDadoIcon = new Label("🎲");
        lblDadoIcon.setFont(Font.font("System", 45));

        Button btnLancarDado = new Button("Lançar Dado");
        btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #0d6efd; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 5px; -fx-cursor: hand;");

        painelLateral.getChildren().addAll(lblTitulo, lblDadoIcon, btnLancarDado);
        root.setRight(painelLateral);

        // --- BOTTOM: Barra de Estado Avançada ---
        GridPane barraInferior = new GridPane();
        barraInferior.setStyle("-fx-background-color: #f1f3f5; -fx-padding: 15px; -fx-border-color: #ced4da; -fx-border-width: 1px 0 0 0;");
        barraInferior.setHgap(40);
        barraInferior.setVgap(5);

        lblTurnoStatus = new Label("SEU TURNO");
        lblTurnoStatus.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTurnoStatus.setTextFill(Color.web("#212529"));

        lblCorStatus = new Label("A sua cor é Azul");
        lblCorStatus.setFont(Font.font("System", FontWeight.NORMAL, 14));
        lblCorStatus.setTextFill(Color.DODGERBLUE);

        lblEstadoDetalhado = new Label("ESTADO DO JOGADOR:\nJOGADOR 1 (Azul): Casa 1");
        lblEstadoDetalhado.setFont(Font.font("System", FontWeight.NORMAL, 13));
        lblEstadoDetalhado.setTextFill(Color.web("#495057"));

        Label lblUltimoLancamentoTitulo = new Label("ÚLTIMO LANÇAMENTO:");
        lblUltimoLancamentoTitulo.setFont(Font.font("System", FontWeight.BOLD, 13));

        lblDadoResultado = new Label("[ Aguardando Lançamento... ]\n(Dado 1: — )");
        lblDadoResultado.setFont(Font.font("System", FontWeight.NORMAL, 13));
        lblDadoResultado.setTextFill(Color.web("#495057"));

        barraInferior.add(lblTurnoStatus, 0, 0);
        barraInferior.add(lblCorStatus, 0, 1);
        barraInferior.add(lblEstadoDetalhado, 0, 2);
        barraInferior.add(lblUltimoLancamentoTitulo, 1, 0);
        barraInferior.add(lblDadoResultado, 1, 1, 1, 2);

        root.setBottom(barraInferior);

        // --- AÇÃO DO BOTÃO (Liga o clique ao Motor de Jogo) ---
        btnLancarDado.setOnAction(e -> {
            if (!motorJogo.isJogoTerminado()) {
                motorJogo.jogarTurno(); // A magia acontece toda aqui dentro

                int posAtual = jogador1.getPosicao();
                lblDadoResultado.setText("[ Dado Lançado! ]\nPosição Atual: Casa " + posAtual);
            }
        });

        // --- LISTENER REATIVO (Observer) ---
        jogador1.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            int novaPosicao = newVal.intValue();

            lblEstadoDetalhado.setText("ESTADO DO JOGADOR:\nJOGADOR 1 (Azul): Casa " + (novaPosicao > 100 ? 100 : novaPosicao));
            atualizarPosicaoGrafica(novaPosicao);

            // Verifica Vitória e bloqueia o jogo!
            if (motorJogo.isJogoTerminado() || novaPosicao >= 100) {
                lblTurnoStatus.setText("★ VITÓRIA! ★");
                lblTurnoStatus.setTextFill(Color.GREEN);
                btnLancarDado.setDisable(true); // Impede continuar a jogar
            }
        });

        Scene scene = new Scene(root, 820, 720);
        primaryStage.setTitle("Snake and Ladder - Laboratório de Programação");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void desenharTabuleiro(GraphicsContext gc) {
        for (int linha = 0; linha < 10; linha++) {
            for (int coluna = 0; coluna < 10; coluna++) {
                int x = coluna * TAMANHO_CASA;
                int y = linha * TAMANHO_CASA;

                gc.setFill((linha + coluna) % 2 == 0 ? Color.web("#e9ecef") : Color.WHITE);
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

    private double[] getCentroCasa(int numeroCasa) {
        if (numeroCasa > 100) numeroCasa = 100;
        int zeroIndex = numeroCasa - 1;
        int linhaInvertida = zeroIndex / 10;
        int coluna = zeroIndex % 10;
        if (linhaInvertida % 2 != 0) coluna = 9 - coluna;
        int linhaGrafica = 9 - linhaInvertida;
        return new double[]{(coluna * TAMANHO_CASA) + (TAMANHO_CASA / 2.0), (linhaGrafica * TAMANHO_CASA) + (TAMANHO_CASA / 2.0)};
    }

    private void desenharObstaculosVisuais(GraphicsContext gc) {
        java.util.Random random = new java.util.Random();
        java.util.Set<Integer> casasOcupadas = new java.util.HashSet<>();

        int escadaInicio;
        do { escadaInicio = 5 + random.nextInt(15); } while (casasOcupadas.contains(escadaInicio));
        casasOcupadas.add(escadaInicio);
        int escadaFim = escadaInicio + 30 + random.nextInt(40);

        int cobraInicio;
        do { cobraInicio = 60 + random.nextInt(30); } while (casasOcupadas.contains(cobraInicio));
        casasOcupadas.add(cobraInicio);
        int cobraFim = cobraInicio - 30 - random.nextInt(20);

        double[] pEscadaInicio = getCentroCasa(escadaInicio);
        double[] pEscadaFim = getCentroCasa(escadaFim);
        double[] pCobraInicio = getCentroCasa(cobraInicio);
        double[] pCobraFim = getCentroCasa(cobraFim);

        // Escada
        gc.setStroke(Color.SADDLEBROWN); gc.setLineWidth(4);
        double angEsc = Math.atan2(pEscadaFim[1] - pEscadaInicio[1], pEscadaFim[0] - pEscadaInicio[0]);
        double oX = Math.sin(angEsc) * 12, oY = -Math.cos(angEsc) * 12;
        gc.strokeLine(pEscadaInicio[0] + oX, pEscadaInicio[1] + oY, pEscadaFim[0] + oX, pEscadaFim[1] + oY);
        gc.strokeLine(pEscadaInicio[0] - oX, pEscadaInicio[1] - oY, pEscadaFim[0] - oX, pEscadaFim[1] - oY);
        gc.setLineWidth(3);
        for (int i = 1; i <= 6; i++) {
            double fr = (double) i / 7;
            double pX = pEscadaInicio[0] + (pEscadaFim[0] - pEscadaInicio[0]) * fr;
            double pY = pEscadaInicio[1] + (pEscadaFim[1] - pEscadaInicio[1]) * fr;
            gc.strokeLine(pX + oX, pY + oY, pX - oX, pY - oY);
        }

        // Cobra
        gc.setStroke(Color.FORESTGREEN); gc.setLineWidth(8);
        double angCob = Math.atan2(pCobraFim[1] - pCobraInicio[1], pCobraFim[0] - pCobraInicio[0]);
        double dist = Math.hypot(pCobraFim[0] - pCobraInicio[0], pCobraFim[1] - pCobraInicio[1]);
        double c1X = pCobraInicio[0] + Math.cos(angCob) * (dist * 0.3) + Math.sin(angCob) * 40;
        double c1Y = pCobraInicio[1] + Math.sin(angCob) * (dist * 0.3) - Math.cos(angCob) * 40;
        double c2X = pCobraInicio[0] + Math.cos(angCob) * (dist * 0.7) - Math.sin(angCob) * 40;
        double c2Y = pCobraInicio[1] + Math.sin(angCob) * (dist * 0.7) + Math.cos(angCob) * 40;
        gc.beginPath(); gc.moveTo(pCobraInicio[0], pCobraInicio[1]);
        gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pCobraFim[0], pCobraFim[1]); gc.stroke();
        gc.setFill(Color.FORESTGREEN); gc.fillOval(pCobraInicio[0] - 10, pCobraInicio[1] - 10, 20, 20);
        gc.setFill(Color.WHITE); gc.fillOval(pCobraInicio[0] - 6, pCobraInicio[1] - 4, 5, 5); gc.fillOval(pCobraInicio[0] + 2, pCobraInicio[1] - 4, 5, 5);
        gc.setLineWidth(1);
    }

    private int calcularNumeroCasa(int linha, int coluna) {
        int linhaInvertida = 9 - linha;
        return linhaInvertida % 2 == 0 ? (linhaInvertida * 10) + coluna + 1 : (linhaInvertida * 10) + (9 - coluna) + 1;
    }

    private void atualizarPosicaoGrafica(int numeroCasa) {
        if (numeroCasa > 100) numeroCasa = 100; // Segurança extra para o visual
        double[] pos = getCentroCasa(numeroCasa);
        pecaGrafica.setCenterX(pos[0]);
        pecaGrafica.setCenterY(pos[1]);
    }
}