package pt.ipvc.snakeladder;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
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
    private Jogador jogador2;

    // Peças gráficas para os dois jogadores
    private Circle pecaJogador1;
    private Circle pecaJogador2;

    private final int TAMANHO_CASA = 60;

    private Label lblTurnoStatus;
    private Label lblEstadoDetalhado;
    private Label lblDadoResultado;
    private Label lblTituloPainel;

    @Override
    public void start(Stage primaryStage) {
        // 1. Inicializar o Motor com 2 Jogadores conforme as regras do jogo
        motorJogo = new Jogo();

        jogador1 = new Jogador(Color.DODGERBLUE); // Azul
        jogador2 = new Jogador(Color.ORANGE);     // Laranja

        motorJogo.adicionarJogador(jogador1);
        motorJogo.adicionarJogador(jogador2);
        motorJogo.iniciar();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");

        DropShadow sombraPaineis = new DropShadow(15, 0, 5, Color.color(0, 0, 0, 0.08));

        // --- TOPO: Barra Superior ---
        HBox barraTopo = new HBox();
        barraTopo.setStyle("-fx-background-color: #ffffff; -fx-padding: 12px 25px;");
        barraTopo.setEffect(new DropShadow(5, 0, 2, Color.color(0, 0, 0, 0.05)));

        Button btnNovoJogo = new Button("🔄 Novo Jogo");
        btnNovoJogo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 8px 16px; -fx-text-fill: #334155;");

        barraTopo.getChildren().add(btnNovoJogo);
        root.setTop(barraTopo);

        // --- CENTRO: Tabuleiro ---
        StackPane areaJogo = new StackPane();
        areaJogo.setEffect(sombraPaineis);
        BorderPane.setMargin(areaJogo, new Insets(25, 10, 25, 25));

        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        desenharTabuleiro(gc);
        desenharObstaculosVisuais(gc);

        Pane camadaPecas = new Pane();
        camadaPecas.setPrefSize(600, 600);
        camadaPecas.setMaxSize(600, 600);

        // Configurar Peça Gráfica do Jogador 1 (Azul)
        pecaJogador1 = new Circle(TAMANHO_CASA / 2.8);
        pecaJogador1.setFill(criarGradientePeca(jogador1.getCor()));
        pecaJogador1.setStroke(Color.WHITE);
        pecaJogador1.setStrokeWidth(1.5);
        pecaJogador1.setEffect(new DropShadow(6, 2, 2, Color.color(0, 0, 0, 0.5)));

        // Configurar Peça Gráfica do Jogador 2 (Laranja)
        pecaJogador2 = new Circle(TAMANHO_CASA / 2.8);
        pecaJogador2.setFill(criarGradientePeca(jogador2.getCor()));
        pecaJogador2.setStroke(Color.WHITE);
        pecaJogador2.setStrokeWidth(1.5);
        pecaJogador2.setEffect(new DropShadow(6, 2, 2, Color.color(0, 0, 0, 0.5)));

        // Colocar os dois na Casa 1 (com um ligeiro desvio para não se taparem totalmente)
        atualizarPosicaoGrafica(jogador1, pecaJogador1, 1, -5);
        atualizarPosicaoGrafica(jogador2, pecaJogador2, 1, 5);

        camadaPecas.getChildren().addAll(pecaJogador1, pecaJogador2);

        areaJogo.getChildren().addAll(canvas, camadaPecas);
        root.setCenter(areaJogo);

        // --- LATERAL: Painel de Controlo ---
        VBox painelLateral = new VBox(25);
        painelLateral.setStyle("-fx-background-color: #ffffff; -fx-padding: 30px; -fx-background-radius: 12px;");
        painelLateral.setEffect(sombraPaineis);
        painelLateral.setAlignment(Pos.TOP_CENTER);
        painelLateral.setPrefWidth(240);
        BorderPane.setMargin(painelLateral, new Insets(25, 25, 25, 15));

        lblTituloPainel = new Label("JOGADOR 1");
        lblTituloPainel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));
        lblTituloPainel.setTextFill(jogador1.getCor());

        Label lblDadoIcon = new Label("🎲");
        lblDadoIcon.setFont(Font.font("System", 60));

        Button btnLancarDado = new Button("Lançar Dado");
        btnLancarDado.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnLancarDado.setEffect(new DropShadow(10, 0, 4, Color.color(0.23, 0.51, 0.96, 0.4)));

        painelLateral.getChildren().addAll(lblTituloPainel, lblDadoIcon, btnLancarDado);
        root.setRight(painelLateral);

        // --- BOTTOM: Menu de Estado ---
        HBox barraInferior = new HBox();
        barraInferior.setStyle("-fx-background-color: #ffffff; -fx-padding: 20px 30px; -fx-background-radius: 12px; -fx-border-color: #e2e8f0; -fx-border-radius: 12px; -fx-border-width: 1px;");
        barraInferior.setEffect(sombraPaineis);
        barraInferior.setAlignment(Pos.CENTER);
        BorderPane.setMargin(barraInferior, new Insets(0, 25, 25, 25));

        VBox statusEsquerda = new VBox(5);
        statusEsquerda.setAlignment(Pos.CENTER_LEFT);

        lblTurnoStatus = new Label("TURNO: JOGADOR 1");
        lblTurnoStatus.setFont(Font.font("System", FontWeight.BLACK, 16));
        lblTurnoStatus.setTextFill(jogador1.getCor());

        lblEstadoDetalhado = new Label("J1: Casa 1  |  J2: Casa 1");
        lblEstadoDetalhado.setFont(Font.font("System", FontWeight.NORMAL, 15));
        lblEstadoDetalhado.setTextFill(Color.web("#475569"));

        statusEsquerda.getChildren().addAll(lblTurnoStatus, lblEstadoDetalhado);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox statusDireita = new VBox(5);
        statusDireita.setAlignment(Pos.CENTER_RIGHT);

        Label lblUltimoLancamentoTitulo = new Label("ÚLTIMO LANÇAMENTO");
        lblUltimoLancamentoTitulo.setFont(Font.font("System", FontWeight.BLACK, 13));
        lblUltimoLancamentoTitulo.setTextFill(Color.web("#94a3b8"));

        lblDadoResultado = new Label("[ Aguardando Lançamento... ]");
        lblDadoResultado.setFont(Font.font("System", FontWeight.NORMAL, 15));
        lblDadoResultado.setTextFill(Color.web("#475569"));

        statusDireita.getChildren().addAll(lblUltimoLancamentoTitulo, lblDadoResultado);

        barraInferior.getChildren().addAll(statusEsquerda, spacer, statusDireita);
        root.setBottom(barraInferior);

        // --- AÇÃO DO BOTÃO: Lançar Dado (Com alternância de turno reativa) ---
        btnLancarDado.setOnAction(e -> {
            if (!motorJogo.isJogoTerminado()) {
                // Descobrir quem vai jogar ANTES de rodar o turno
                int indexAtual = motorJogo.getJogadorAtualIndex();
                Jogador quemJoga = motorJogo.getJogadores().get(indexAtual);
                String nomeJogador = (indexAtual == 0) ? "Jogador 1" : "Jogador 2";

                // Executa a lógica e move a peça internamente
                motorJogo.jogarTurno();

                // Atualizar texto do dado lançado
                lblDadoResultado.setText(nomeJogador + " tirou um " + motorJogo.getDado().getValor() + "!");

                // Mudar a Interface para focar no PRÓXIMO jogador
                int proximoIndex = motorJogo.getJogadorAtualIndex();
                Jogador proximoJogador = motorJogo.getJogadores().get(proximoIndex);

                if (!motorJogo.isJogoTerminado()) {
                    lblTituloPainel.setText("JOGADOR " + (proximoIndex + 1));
                    lblTituloPainel.setTextFill(proximoJogador.getCor());
                    lblTurnoStatus.setText("TURNO: JOGADOR " + (proximoIndex + 1));
                    lblTurnoStatus.setTextFill(proximoJogador.getCor());

                    // Atualiza as cores do botão para combinar com o jogador atual
                    if (proximoIndex == 0) {
                        btnLancarDado.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-cursor: hand;");
                    } else {
                        btnLancarDado.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #f97316, #ea580c); -fx-text-fill: white; -fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-cursor: hand;");
                    }
                }
            }
        });

        // --- BOTÃO NOVO JOGO LOCAL ---
        btnNovoJogo.setOnAction(e -> {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Novo Jogo");
            alerta.setHeaderText("Começar um novo jogo local?");
            alerta.setContentText("O progresso dos dois jogadores será reiniciado. Continuar?");

            ButtonType btnSim = new ButtonType("Sim", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnNao = new ButtonType("Não", ButtonBar.ButtonData.CANCEL_CLOSE);
            alerta.getButtonTypes().setAll(btnSim, btnNao);

            Optional<ButtonType> resultado = alerta.showAndWait();
            if (resultado.isPresent() && resultado.get() == btnSim) {
                motorJogo = new Jogo();
                jogador1.posicaoProperty().set(1);
                jogador2.posicaoProperty().set(1);

                motorJogo.adicionarJogador(jogador1);
                motorJogo.adicionarJogador(jogador2);
                configurarObstaculosFixos();
                motorJogo.iniciar();

                lblTituloPainel.setText("JOGADOR 1");
                lblTituloPainel.setTextFill(jogador1.getCor());
                lblTurnoStatus.setText("TURNO: JOGADOR 1");
                lblTurnoStatus.setTextFill(jogador1.getCor());
                lblEstadoDetalhado.setText("J1: Casa 1  |  J2: Casa 1");
                lblDadoResultado.setText("[ Jogo Reiniciado ]");
                btnLancarDado.setDisable(false);
                btnLancarDado.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-cursor: hand;");

                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                desenharTabuleiro(gc);
                desenharObstaculosVisuais(gc);
                atualizarPosicaoGrafica(jogador1, pecaJogador1, 1, -5);
                atualizarPosicaoGrafica(jogador2, pecaJogador2, 1, 5);
            }
        });

        // --- LISTENERS REATIVOS (Acompanha os 2 jogadores no tabuleiro) ---
        jogador1.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            int novaPos = newVal.intValue();
            atualizarPosicaoGrafica(jogador1, pecaJogador1, novaPos, -5);
            atualizarBarraEstado();
            verificarVitoria(1, novaPos, btnLancarDado);
        });

        jogador2.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            int novaPos = newVal.intValue();
            atualizarPosicaoGrafica(jogador2, pecaJogador2, novaPos, 5);
            atualizarBarraEstado();
            verificarVitoria(2, novaPos, btnLancarDado);
        });

        // --- SCROLL PANE ---
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #f1f5f9; -fx-background: #f1f5f9; -fx-border-color: transparent;");
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY();
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY / 250.0);
            event.consume();
        });

        Scene scene = new Scene(scrollPane, 920, 750);
        primaryStage.setTitle("Snake and Ladder - Modo Local 2 Players");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private RadialGradient criarGradientePeca(Color corBase) {
        return new RadialGradient(
                0, 0, 0.35, 0.35, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE),
                new Stop(0.2, corBase.brighter()),
                new Stop(0.8, corBase.darker()),
                new Stop(1, Color.web("#0a1a3a"))
        );
    }

    private void atualizarBarraEstado() {
        int posJ1 = jogador1.getPosicao() > 100 ? 100 : jogador1.getPosicao();
        int posJ2 = jogador2.getPosicao() > 100 ? 100 : jogador2.getPosicao();
        lblEstadoDetalhado.setText("J1: Casa " + posJ1 + "  |  J2: Casa " + posJ2);
    }

    private void verificarVitoria(int numeroJogador, int posicao, Button botaoDado) {
        if (motorJogo.isJogoTerminado() || posicao >= 100) {
            lblTurnoStatus.setText("🎉 JOGADOR " + numeroJogador + " GANHOU! 🎉");
            lblTurnoStatus.setTextFill(Color.web("#10b981"));
            lblTituloPainel.setText("FIM DE JOGO");
            botaoDado.setDisable(true);
        }
    }

    private void configurarObstaculosFixos() {
        int[][] escadasPos = {{5, 25}, {14, 48}, {42, 63}, {74, 95}};
        int[][] cobrasPos = {{32, 12}, {56, 26}, {87, 66}, {98, 79}};

        for (int[] pos : escadasPos) motorJogo.getTabuleiro().adicionarObstaculo(new pt.ipvc.snakeladder.modelo.Escada(pos[0], pos[1]));
        for (int[] pos : cobrasPos) motorJogo.getTabuleiro().adicionarObstaculo(new pt.ipvc.snakeladder.modelo.Cobra(pos[0], pos[1]));
    }

    private void desenharTabuleiro(GraphicsContext gc) {
        for (int linha = 0; linha < 10; linha++) {
            for (int coluna = 0; coluna < 10; coluna++) {
                int x = coluna * TAMANHO_CASA;
                int y = linha * TAMANHO_CASA;

                gc.setFill((linha + coluna) % 2 == 0 ? Color.web("#f8fafc") : Color.WHITE);
                gc.fillRect(x, y, TAMANHO_CASA, TAMANHO_CASA);
                gc.setStroke(Color.web("#e2e8f0"));
                gc.strokeRect(x, y, TAMANHO_CASA, TAMANHO_CASA);

                int numeroCasa = calcularNumeroCasa(linha, coluna);
                gc.setFill(Color.web("#94a3b8"));
                gc.setFont(Font.font("System", FontWeight.BOLD, 12));
                gc.fillText(String.valueOf(numeroCasa), x + 6, y + 18);
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

                gc.setStroke(Color.web("#3b200e")); gc.setLineWidth(8);
                gc.strokeLine(pInicio[0] + oX, pInicio[1] + oY, pFim[0] + oX, pFim[1] + oY);
                gc.strokeLine(pInicio[0] - oX, pInicio[1] - oY, pFim[0] - oX, pFim[1] - oY);
                gc.setEffect(null);

                gc.setStroke(Color.web("#7a4520")); gc.setLineWidth(4);
                gc.strokeLine(pInicio[0] + oX, pInicio[1] + oY, pFim[0] + oX, pFim[1] + oY);
                gc.strokeLine(pInicio[0] - oX, pInicio[1] - oY, pFim[0] - oX, pFim[1] - oY);

                for (int i = 1; i <= 7; i++) {
                    double fr = (double) i / 8;
                    double pX = pInicio[0] + (pFim[0] - pInicio[0]) * fr;
                    double pY = pInicio[1] + (pFim[1] - pInicio[1]) * fr;
                    gc.setStroke(Color.web("#3b200e")); gc.setLineWidth(6);
                    gc.strokeLine(pX + oX, pY + oY, pX - oX, pY - oY);
                    gc.setStroke(Color.web("#8b522c")); gc.setLineWidth(3);
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

                gc.setStroke(Color.web("#003300")); gc.setLineWidth(10);
                gc.beginPath(); gc.moveTo(pInicio[0], pInicio[1]);
                gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pFim[0], pFim[1]); gc.stroke();
                gc.setEffect(null);

                gc.setStroke(Color.web("#228B22")); gc.setLineWidth(6);
                gc.beginPath(); gc.moveTo(pInicio[0], pInicio[1]);
                gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pFim[0], pFim[1]); gc.stroke();

                gc.setStroke(Color.web("#ADFF2F")); gc.setLineWidth(1.5); gc.setLineDashes(3, 5);
                gc.beginPath(); gc.moveTo(pInicio[0], pInicio[1]);
                gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pFim[0], pFim[1]); gc.stroke();
                gc.setLineDashes(0);

                double angHead = Math.atan2(pInicio[1] - c1Y, pInicio[0] - c1X);
                gc.save();
                gc.translate(pInicio[0], pInicio[1]);
                gc.rotate(Math.toDegrees(angHead) + 90);

                gc.setStroke(Color.CRIMSON); gc.setLineWidth(2);
                gc.strokeLine(0, 0, 0, -18); gc.strokeLine(0, -18, -4, -24); gc.strokeLine(0, -18, 4, -24);
                gc.setFill(Color.web("#003300")); gc.fillOval(-12, -14, 24, 22);
                gc.setFill(Color.web("#228B22")); gc.fillOval(-9, -11, 18, 16);
                gc.setFill(Color.GOLD); gc.fillOval(-8, -12, 5, 8); gc.fillOval(3, -12, 5, 8);
                gc.setFill(Color.BLACK); gc.fillOval(-6, -11, 1.5, 6); gc.fillOval(4.5, -11, 1.5, 6);
                gc.restore();
                gc.setLineWidth(1); gc.setLineCap(StrokeLineCap.SQUARE);
            }
        }
    }

    private int calcularNumeroCasa(int linha, int coluna) {
        int linhaInvertida = 9 - linha;
        return linhaInvertida % 2 == 0 ? (linhaInvertida * 10) + coluna + 1 : (linhaInvertida * 10) + (9 - coluna) + 1;
    }

    private void atualizarPosicaoGrafica(Jogador jogador, Circle circulo, int numeroCasa, double desvioX) {
        if (numeroCasa > 100) numeroCasa = 100;
        double[] pos = getCentroCasa(numeroCasa);
        // O desvio serve para que as duas peças não fiquem exatamente coladas no centro se estiverem na mesma casa
        circulo.setCenterX(pos[0] + desvioX);
        circulo.setCenterY(pos[1]);
    }
}