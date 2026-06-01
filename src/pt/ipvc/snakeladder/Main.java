package pt.ipvc.snakeladder;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import pt.ipvc.snakeladder.modelo.Jogo;
import pt.ipvc.snakeladder.modelo.Jogador;

import java.util.Optional;

public class Main extends Application {

    private Jogo motorJogo;
    private Jogador jogador1;
    private Circle pecaGrafica;
    private final int TAMANHO_CASA = 60;

    private Label lblTurnoStatus;
    private Label lblCorStatus;
    private Label lblEstadoDetalhado;
    private Label lblDadoResultado;

    @Override
    public void start(Stage primaryStage) {
        motorJogo = new Jogo();
        jogador1 = new Jogador(Color.DODGERBLUE);
        motorJogo.adicionarJogador(jogador1);
        motorJogo.iniciar();

        BorderPane root = new BorderPane();

        // --- TOPO: Barra Superior de Ações ---
        HBox barraTopo = new HBox();
        barraTopo.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10px 15px; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1px 0;");

        Button btnNovoJogo = new Button("🔄 Novo Jogo");
        btnNovoJogo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: #e9ecef; -fx-border-color: #ced4da; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        barraTopo.getChildren().add(btnNovoJogo);
        root.setTop(barraTopo);

        // --- CENTRO: Tabuleiro ---
        StackPane areaJogo = new StackPane();

        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        desenharTabuleiro(gc);
        desenharObstaculosVisuais(gc);

        Pane camadaPecas = new Pane();
        camadaPecas.setPrefSize(600, 600);

        pecaGrafica = new Circle(TAMANHO_CASA / 2.6);
        RadialGradient gradientePeca = new RadialGradient(
                0, 0, 0.35, 0.35, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE),
                new Stop(0.2, jogador1.getCor().brighter()),
                new Stop(0.8, jogador1.getCor().darker()),
                new Stop(1, Color.web("#0a1a3a"))
        );
        pecaGrafica.setFill(gradientePeca);
        pecaGrafica.setStroke(Color.WHITE);
        pecaGrafica.setStrokeWidth(1.5);

        DropShadow sombraPeca = new DropShadow(8, 3, 3, Color.color(0, 0, 0, 0.6));
        pecaGrafica.setEffect(sombraPeca);

        atualizarPosicaoGrafica(1);
        camadaPecas.getChildren().add(pecaGrafica);

        areaJogo.getChildren().addAll(canvas, camadaPecas);
        root.setCenter(areaJogo);

        // --- LATERAL: Painel ---
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

        // --- BOTTOM: Barra de Estado ---
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

        // --- AÇÃO: BOTÃO LANÇAR DADO ---
        btnLancarDado.setOnAction(e -> {
            if (!motorJogo.isJogoTerminado()) {
                motorJogo.jogarTurno();
                int posAtual = jogador1.getPosicao();
                lblDadoResultado.setText("[ Dado Lançado! ]\nPosição Atual: Casa " + posAtual);
            }
        });

        // --- AÇÃO: BOTÃO NOVO JOGO COM AVISO ---
        btnNovoJogo.setOnAction(e -> {
            // Criação da janela de aviso
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Novo Jogo");
            alerta.setHeaderText("Começar um novo jogo?");
            alerta.setContentText("O teu progresso atual será perdido e o tabuleiro vai ser baralhado de novo. Queres continuar?");

            // Alterar os botões do alerta para Português
            ButtonType btnSim = new ButtonType("Sim", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnNao = new ButtonType("Não", ButtonBar.ButtonData.CANCEL_CLOSE);
            alerta.getButtonTypes().setAll(btnSim, btnNao);

            // Aguarda a resposta do utilizador
            Optional<ButtonType> resultado = alerta.showAndWait();

            // Se o utilizador clicou em "Sim"
            if (resultado.isPresent() && resultado.get() == btnSim) {
                // 1. Novo motor (com novos obstáculos)
                motorJogo = new Jogo();

                // 2. Repõe o jogador (o Listener atualiza a peça graficamente de imediato)
                jogador1.posicaoProperty().set(1);
                motorJogo.adicionarJogador(jogador1);
                motorJogo.iniciar();

                // 3. Limpa a UI
                lblTurnoStatus.setText("SEU TURNO");
                lblTurnoStatus.setTextFill(Color.web("#212529"));
                lblDadoResultado.setText("[ Jogo Reiniciado ]\n(Lança o dado para começar)");
                btnLancarDado.setDisable(false); // Volta a ligar o botão do dado

                // 4. Redesenha o tabuleiro e os obstáculos
                desenharTabuleiro(gc);
                desenharObstaculosVisuais(gc);
            }
        });

        // --- LISTENER REATIVO ---
        jogador1.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            int novaPosicao = newVal.intValue();
            lblEstadoDetalhado.setText("ESTADO DO JOGADOR:\nJOGADOR 1 (Azul): Casa " + (novaPosicao > 100 ? 100 : novaPosicao));
            atualizarPosicaoGrafica(novaPosicao);

            if (motorJogo.isJogoTerminado() || novaPosicao >= 100) {
                lblTurnoStatus.setText("★ VITÓRIA! ★");
                lblTurnoStatus.setTextFill(Color.GREEN);
                btnLancarDado.setDisable(true);
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

                gc.setFill((linha + coluna) % 2 == 0 ? Color.web("#f8f9fa") : Color.WHITE);
                gc.fillRect(x, y, TAMANHO_CASA, TAMANHO_CASA);
                gc.setStroke(Color.web("#dee2e6"));
                gc.strokeRect(x, y, TAMANHO_CASA, TAMANHO_CASA);

                int numeroCasa = calcularNumeroCasa(linha, coluna);
                gc.setFill(Color.web("#adb5bd"));
                gc.setFont(Font.font("System", FontWeight.BOLD, 11));
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
        DropShadow sombraObstaculo = new DropShadow(6, 3, 3, Color.color(0, 0, 0, 0.4));

        for (pt.ipvc.snakeladder.modelo.Obstaculo obs : motorJogo.getTabuleiro().getObstaculos()) {

            double[] pInicio = getCentroCasa(obs.getInicio());
            double[] pFim = getCentroCasa(obs.getFim());

            if (obs instanceof pt.ipvc.snakeladder.modelo.Escada) {
                gc.setEffect(sombraObstaculo);
                gc.setLineCap(StrokeLineCap.ROUND);

                double angEsc = Math.atan2(pFim[1] - pInicio[1], pFim[0] - pInicio[0]);
                double oX = Math.sin(angEsc) * 12, oY = -Math.cos(angEsc) * 12;

                gc.setStroke(Color.web("#3b200e"));
                gc.setLineWidth(8);
                gc.strokeLine(pInicio[0] + oX, pInicio[1] + oY, pFim[0] + oX, pFim[1] + oY);
                gc.strokeLine(pInicio[0] - oX, pInicio[1] - oY, pFim[0] - oX, pFim[1] - oY);

                gc.setEffect(null);

                gc.setStroke(Color.web("#7a4520"));
                gc.setLineWidth(4);
                gc.strokeLine(pInicio[0] + oX, pInicio[1] + oY, pFim[0] + oX, pFim[1] + oY);
                gc.strokeLine(pInicio[0] - oX, pInicio[1] - oY, pFim[0] - oX, pFim[1] - oY);

                for (int i = 1; i <= 7; i++) {
                    double fr = (double) i / 8;
                    double pX = pInicio[0] + (pFim[0] - pInicio[0]) * fr;
                    double pY = pInicio[1] + (pFim[1] - pInicio[1]) * fr;

                    gc.setStroke(Color.web("#3b200e"));
                    gc.setLineWidth(6);
                    gc.strokeLine(pX + oX, pY + oY, pX - oX, pY - oY);

                    gc.setStroke(Color.web("#8b522c"));
                    gc.setLineWidth(3);
                    gc.strokeLine(pX + oX, pY + oY, pX - oX, pY - oY);
                }
                gc.setLineCap(StrokeLineCap.SQUARE);

            } else if (obs instanceof pt.ipvc.snakeladder.modelo.Cobra) {
                gc.setEffect(sombraObstaculo);
                gc.setLineCap(StrokeLineCap.ROUND);

                double dist = Math.hypot(pFim[0] - pInicio[0], pFim[1] - pInicio[1]);
                double angCob = Math.atan2(pFim[1] - pInicio[1], pFim[0] - pInicio[0]);

                double amp = Math.min(dist * 0.15, 30);
                double c1X = pInicio[0] + Math.cos(angCob) * (dist * 0.3) + Math.sin(angCob) * amp;
                double c1Y = pInicio[1] + Math.sin(angCob) * (dist * 0.3) - Math.cos(angCob) * amp;
                double c2X = pInicio[0] + Math.cos(angCob) * (dist * 0.7) - Math.sin(angCob) * amp;
                double c2Y = pInicio[1] + Math.sin(angCob) * (dist * 0.7) + Math.cos(angCob) * amp;

                gc.setStroke(Color.web("#003300"));
                gc.setLineWidth(10);
                gc.beginPath();
                gc.moveTo(pInicio[0], pInicio[1]);
                gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pFim[0], pFim[1]);
                gc.stroke();

                gc.setEffect(null);

                gc.setStroke(Color.web("#228B22"));
                gc.setLineWidth(6);
                gc.beginPath();
                gc.moveTo(pInicio[0], pInicio[1]);
                gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pFim[0], pFim[1]);
                gc.stroke();

                gc.setStroke(Color.web("#ADFF2F"));
                gc.setLineWidth(1.5);
                gc.setLineDashes(3, 5);
                gc.beginPath();
                gc.moveTo(pInicio[0], pInicio[1]);
                gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pFim[0], pFim[1]);
                gc.stroke();
                gc.setLineDashes(0);

                double angHead = Math.atan2(c1Y - pInicio[1], c1X - pInicio[0]);

                gc.save();
                gc.translate(pInicio[0], pInicio[1]);
                gc.rotate(Math.toDegrees(angHead) + 90);

                gc.setStroke(Color.CRIMSON);
                gc.setLineWidth(2);
                gc.strokeLine(0, 0, 0, -18);
                gc.strokeLine(0, -18, -4, -24);
                gc.strokeLine(0, -18, 4, -24);

                gc.setFill(Color.web("#003300"));
                gc.fillOval(-12, -14, 24, 22);
                gc.setFill(Color.web("#228B22"));
                gc.fillOval(-9, -11, 18, 16);

                gc.setFill(Color.GOLD);
                gc.fillOval(-8, -12, 5, 8);
                gc.fillOval(3, -12, 5, 8);

                gc.setFill(Color.BLACK);
                gc.fillOval(-6, -11, 1.5, 6);
                gc.fillOval(4.5, -11, 1.5, 6);

                gc.restore();
                gc.setLineWidth(1);
                gc.setLineCap(StrokeLineCap.SQUARE);
            }
        }
    }

    private int calcularNumeroCasa(int linha, int coluna) {
        int linhaInvertida = 9 - linha;
        return linhaInvertida % 2 == 0 ? (linhaInvertida * 10) + coluna + 1 : (linhaInvertida * 10) + (9 - coluna) + 1;
    }

    private void atualizarPosicaoGrafica(int numeroCasa) {
        if (numeroCasa > 100) numeroCasa = 100;
        double[] pos = getCentroCasa(numeroCasa);
        pecaGrafica.setCenterX(pos[0]);
        pecaGrafica.setCenterY(pos[1]);
    }
}