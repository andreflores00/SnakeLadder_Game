package pt.ipvc.snakeladder;

import javafx.animation.PauseTransition;
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
import javafx.util.Duration;
import pt.ipvc.snakeladder.modelo.Jogo;
import pt.ipvc.snakeladder.modelo.Jogador;

import java.util.Optional;

public class Main extends Application {

    private Jogo motorJogo;
    private Jogador jogador1;
    private Jogador jogador2;

    private Circle pecaJogador1;
    private Circle pecaJogador2;

    // REDUZIDO PARA 50: O tabuleiro passa para 500px, fazendo com que o painel inferior caiba no ecrã!
    private final int TAMANHO_CASA = 50;

    private Label lblTurnoStatus;
    private Label lblEstadoDetalhado;
    private Label lblDadoResultado;
    private Label lblTituloPainel;
    private Button btnLancarDado;

    // Controla se o segundo jogador é o Computador
    private boolean modoBot = false;

    @Override
    public void start(Stage primaryStage) {
        motorJogo = new Jogo();

        jogador1 = new Jogador(Color.DODGERBLUE);
        jogador2 = new Jogador(Color.ORANGE);

        motorJogo.adicionarJogador(jogador1);
        motorJogo.adicionarJogador(jogador2);
        configurarObstaculosFixos();
        motorJogo.iniciar();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");

        DropShadow sombraPaineis = new DropShadow(15, 0, 5, Color.color(0, 0, 0, 0.08));

        // --- TOPO: Menu Dropdown para Escolha de Modo ---
        HBox barraTopo = new HBox();
        barraTopo.setStyle("-fx-background-color: #ffffff; -fx-padding: 10px 20px;");
        barraTopo.setEffect(new DropShadow(5, 0, 2, Color.color(0, 0, 0, 0.05)));

        MenuButton btnNovoJogo = new MenuButton("🔄 Novo Jogo");
        btnNovoJogo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #334155;");

        MenuItem itemLocal = new MenuItem("👥 2 Jogadores (Local)");
        MenuItem itemBot = new MenuItem("🤖 Jogador vs Bot");
        btnNovoJogo.getItems().addAll(itemLocal, itemBot);

        barraTopo.getChildren().add(btnNovoJogo);
        root.setTop(barraTopo);

        // --- CENTRO: Tabuleiro Otimizado ---
        StackPane areaJogo = new StackPane();
        areaJogo.setEffect(sombraPaineis);
        BorderPane.setMargin(areaJogo, new Insets(15, 10, 15, 20));

        Canvas canvas = new Canvas(500, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        desenharTabuleiro(gc);
        desenharObstaculosVisuais(gc);

        Pane camadaPecas = new Pane();
        camadaPecas.setPrefSize(500, 500);
        camadaPecas.setMaxSize(500, 500);

        pecaJogador1 = new Circle(TAMANHO_CASA / 2.8);
        pecaJogador1.setFill(criarGradientePeca(jogador1.getCor()));
        pecaJogador1.setStroke(Color.WHITE);
        pecaJogador1.setStrokeWidth(1.5);
        pecaJogador1.setEffect(new DropShadow(6, 2, 2, Color.color(0, 0, 0, 0.5)));

        pecaJogador2 = new Circle(TAMANHO_CASA / 2.8);
        pecaJogador2.setFill(criarGradientePeca(jogador2.getCor()));
        pecaJogador2.setStroke(Color.WHITE);
        pecaJogador2.setStrokeWidth(1.5);
        pecaJogador2.setEffect(new DropShadow(6, 2, 2, Color.color(0, 0, 0, 0.5)));

        atualizarPosicaoGrafica(jogador1, pecaJogador1, 1, -4);
        atualizarPosicaoGrafica(jogador2, pecaJogador2, 1, 4);

        camadaPecas.getChildren().addAll(pecaJogador1, pecaJogador2);
        areaJogo.getChildren().addAll(canvas, camadaPecas);
        root.setCenter(areaJogo);

        // --- LATERAL: Painel de Controlo ---
        VBox painelLateral = new VBox(20);
        painelLateral.setStyle("-fx-background-color: #ffffff; -fx-padding: 25px 20px; -fx-background-radius: 12px;");
        painelLateral.setEffect(sombraPaineis);
        painelLateral.setAlignment(Pos.TOP_CENTER);
        painelLateral.setPrefWidth(220);
        BorderPane.setMargin(painelLateral, new Insets(15, 20, 15, 10));

        lblTituloPainel = new Label("O TEU TURNO");
        lblTituloPainel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));
        lblTituloPainel.setTextFill(jogador1.getCor());

        Label lblDadoIcon = new Label("🎲");
        lblDadoIcon.setFont(Font.font("System", 55));

        btnLancarDado = new Button("Lançar Dado");
        btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnLancarDado.setEffect(new DropShadow(8, 0, 3, Color.color(0.23, 0.51, 0.96, 0.4)));

        painelLateral.getChildren().addAll(lblTituloPainel, lblDadoIcon, btnLancarDado);
        root.setRight(painelLateral);

        // --- BOTTOM: Menu de Estado Limpo ---
        HBox barraInferior = new HBox();
        barraInferior.setStyle("-fx-background-color: #ffffff; -fx-padding: 15px 25px; -fx-background-radius: 12px; -fx-border-color: #e2e8f0; -fx-border-radius: 12px; -fx-border-width: 1px;");
        barraInferior.setEffect(sombraPaineis);
        barraInferior.setAlignment(Pos.CENTER);
        BorderPane.setMargin(barraInferior, new Insets(0, 20, 20, 20));

        VBox statusEsquerda = new VBox(5);
        statusEsquerda.setAlignment(Pos.CENTER_LEFT);

        lblTurnoStatus = new Label("A TUA VEZ DE JOGAR");
        lblTurnoStatus.setFont(Font.font("System", FontWeight.BLACK, 15));
        lblTurnoStatus.setTextFill(jogador1.getCor());

        lblEstadoDetalhado = new Label("Tu (Azul): Casa 1  |  Adversário: Casa 1");
        lblEstadoDetalhado.setFont(Font.font("System", FontWeight.NORMAL, 14));
        lblEstadoDetalhado.setTextFill(Color.web("#475569"));

        statusEsquerda.getChildren().addAll(lblTurnoStatus, lblEstadoDetalhado);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox statusDireita = new VBox(5);
        statusDireita.setAlignment(Pos.CENTER_RIGHT);

        Label lblUltimoLancamentoTitulo = new Label("ÚLTIMO LANÇAMENTO");
        lblUltimoLancamentoTitulo.setFont(Font.font("System", FontWeight.BLACK, 12));
        lblUltimoLancamentoTitulo.setTextFill(Color.web("#94a3b8"));

        lblDadoResultado = new Label("[ Aguardando Lançamento... ]");
        lblDadoResultado.setFont(Font.font("System", FontWeight.NORMAL, 14));
        lblDadoResultado.setTextFill(Color.web("#475569"));

        statusDireita.getChildren().addAll(lblUltimoLancamentoTitulo, lblDadoResultado);
        barraInferior.getChildren().addAll(statusEsquerda, spacer, statusDireita);
        root.setBottom(barraInferior);

        // --- ACÇÃO DO BOTÃO DO DADO ---
        btnLancarDado.setOnAction(e -> processarJogada());

        // --- ACÇÕES DO SELECÇÃO DE MODOS ---
        itemLocal.setOnAction(e -> reiniciarJogoCompleto(false, gc, canvas));
        itemBot.setOnAction(e -> reiniciarJogoCompleto(true, gc, canvas));

        // --- LISTENERS REATIVOS ---
        jogador1.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            int novaPos = newVal.intValue();
            atualizarPosicaoGrafica(jogador1, pecaJogador1, novaPos, -4);
            atualizarBarraEstado();
            verificarCondicaoVitoria(1, novaPos);
        });

        jogador2.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            int novaPos = newVal.intValue();
            atualizarPosicaoGrafica(jogador2, pecaJogador2, novaPos, 4);
            atualizarBarraEstado();
            verificarCondicaoVitoria(modoBot ? 99 : 2, novaPos);
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

        // Dimensões ideais para portáteis sem causar clipping
        Scene scene = new Scene(scrollPane, 820, 660);
        primaryStage.setTitle("Snake and Ladder - Laboratório de Programação");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // --- MOTOR DOS TURNOS E INTELIGÊNCIA DO BOT ---
    private void processarJogada() {
        if (motorJogo.isJogoTerminado()) return;

        int indexContexto = motorJogo.getJogadorAtualIndex();
        String prefixoTexto = (indexContexto == 0) ? "Tu tiraste" : (modoBot ? "O Bot tirou" : "O Adversário tirou");

        motorJogo.jogarTurno();
        lblDadoResultado.setText(prefixoTexto + " um " + motorJogo.getDado().getValor() + "!");

        if (motorJogo.isJogoTerminado()) return;

        int seguinteIndex = motorJogo.getJogadorAtualIndex();
        Jogador seguinteJogador = motorJogo.getJogadores().get(seguinteIndex);

        if (modoBot && seguinteIndex == 1) {
            // Configura o visual para a jogada automática do Computador
            lblTituloPainel.setText("BOT (Laranja)");
            lblTituloPainel.setTextFill(jogador2.getCor());
            lblTurnoStatus.setText("TURNO DO ADVERSÁRIO (BOT...)");
            lblTurnoStatus.setTextFill(jogador2.getCor());
            btnLancarDado.setDisable(true); // Impede cliques manuais durante o turno do bot

            // Cria um delay de 1.2 segundos para simular a decisão do Bot
            PauseTransition atrasoIA = new PauseTransition(Duration.seconds(1.2));
            atrasoIA.setOnFinished(evt -> processarJogada());
            atrasoIA.play();
        } else {
            // Devolve o controlo ao Jogador Humano
            lblTituloPainel.setText(seguinteIndex == 0 ? "O TEU TURNO" : "ADVERSÁRIO");
            lblTituloPainel.setTextFill(seguinteJogador.getCor());
            lblTurnoStatus.setText(seguinteIndex == 0 ? "A TUA VEZ DE JOGAR" : "TURNO DO ADVERSÁRIO");
            lblTurnoStatus.setTextFill(seguinteJogador.getCor());
            btnLancarDado.setDisable(false);

            if (seguinteIndex == 0) {
                btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");
            } else {
                btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #f97316, #ea580c); -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");
            }
        }
    }

    private void reiniciarJogoCompleto(boolean ativarBot, GraphicsContext canvasGc, Canvas canvasElemento) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Reiniciar Partida");
        confirmacao.setHeaderText(ativarBot ? "Iniciar modo Jogador vs Bot?" : "Iniciar modo Local (2 Jogadores)?");
        confirmacao.setContentText("Todo o progresso atual do tabuleiro será perdido. Pretendes continuar?");

        Optional<ButtonType> escolha = confirmacao.showAndWait();
        if (escolha.isPresent() && escolha.get() == ButtonType.OK) {
            this.modoBot = ativarBot;

            motorJogo = new Jogo();
            jogador1.posicaoProperty().set(1);
            jogador2.posicaoProperty().set(1);

            motorJogo.adicionarJogador(jogador1);
            motorJogo.adicionarJogador(jogador2);
            configurarObstaculosFixos();
            motorJogo.iniciar();

            lblTituloPainel.setText("O TEU TURNO");
            lblTituloPainel.setTextFill(jogador1.getCor());
            lblTurnoStatus.setText("A TUA VEZ DE JOGAR");
            lblTurnoStatus.setTextFill(jogador1.getCor());
            lblEstadoDetalhado.setText("Tu (Azul): Casa 1  |  " + (ativarBot ? "Bot" : "Adversário") + ": Casa 1");
            lblDadoResultado.setText("[ Jogo Reiniciado ]");
            btnLancarDado.setDisable(false);
            btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");

            canvasGc.clearRect(0, 0, canvasElemento.getWidth(), canvasElemento.getHeight());
            desenharTabuleiro(canvasGc);
            desenharObstaculosVisuais(canvasGc);
            atualizarPosicaoGrafica(jogador1, pecaJogador1, 1, -4);
            atualizarPosicaoGrafica(jogador2, pecaJogador2, 1, 4);
        }
    }

    private RadialGradient criarGradientePeca(Color corBase) {
        return new RadialGradient(0, 0, 0.35, 0.35, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE), new Stop(0.2, corBase.brighter()),
                new Stop(0.8, corBase.darker()), new Stop(1, Color.web("#0a1a3a")));
    }

    private void atualizarBarraEstado() {
        int posJ1 = jogador1.getPosicao() > 100 ? 100 : jogador1.getPosicao();
        int posJ2 = jogador2.getPosicao() > 100 ? 100 : jogador2.getPosicao();
        lblEstadoDetalhado.setText("Tu (Azul): Casa " + posJ1 + "  |  " + (modoBot ? "Bot" : "Adversário") + ": Casa " + posJ2);
    }

    private void verificarCondicaoVitoria(int idJogador, int localizacao) {
        if (motorJogo.isJogoTerminado() || localizacao >= 100) {
            String mensagemFinal;
            if (idJogador == 1) {
                mensagemFinal = "🎉 PARABÉNS, GANHASTE A PARTIDA! 🎉";
                lblTurnoStatus.setTextFill(Color.web("#10b981"));
            } else if (idJogador == 99) {
                mensagemFinal = "🤖 O BOT VENCEU O JOGO! 🤖";
                lblTurnoStatus.setTextFill(Color.CRIMSON);
            } else {
                mensagemFinal = "💀 O ADVERSÁRIO GANHOU! 💀";
                lblTurnoStatus.setTextFill(Color.CRIMSON);
            }
            lblTurnoStatus.setText(mensagemFinal);
            lblTituloPainel.setText("FIM DE JOGO");
            btnLancarDado.setDisable(true);
        }
    }

    private void configurarObstaculosFixos() {
        // Exatamente 4 de cada, balanceados e sem qualquer colisão
        int[][] escadasPos = {{5, 25}, {14, 48}, {42, 63}, {74, 95}};
        int[][] cobrasPos = {{32, 12}, {56, 26}, {87, 66}, {98, 79}};
        for (int[] pos : escadasPos) motorJogo.getTabuleiro().adicionarObstaculo(new pt.ipvc.snakeladder.modelo.Escada(pos[0], pos[1]));
        for (int[] pos : cobrasPos) motorJogo.getTabuleiro().adicionarObstaculo(new pt.ipvc.snakeladder.modelo.Cobra(pos[0], pos[1]));
    }

    private void desenharTabuleiro(GraphicsContext gc) {
        for (int linha = 0; linha < 10; linha++) {
            for (int coluna = 0; coluna < 10; coluna++) { // Corrigido column++ para coluna++
                int x = coluna * TAMANHO_CASA;
                int y = linha * TAMANHO_CASA;
                gc.setFill((linha + coluna) % 2 == 0 ? Color.web("#f8fafc") : Color.WHITE);
                gc.fillRect(x, y, TAMANHO_CASA, TAMANHO_CASA);
                gc.setStroke(Color.web("#e2e8f0"));
                gc.strokeRect(x, y, TAMANHO_CASA, TAMANHO_CASA);
                int numeroCasa = calcularNumeroCasa(linha, coluna);
                gc.setFill(Color.web("#94a3b8"));
                gc.setFont(Font.font("System", FontWeight.BOLD, 11));
                gc.fillText(String.valueOf(numeroCasa), x + 5, y + 16);
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
        DropShadow sombraObstaculo = new DropShadow(5, 2, 2, Color.color(0, 0, 0, 0.35));
        for (pt.ipvc.snakeladder.modelo.Obstaculo obs : motorJogo.getTabuleiro().getObstaculos()) {
            double[] pInicio = getCentroCasa(obs.getInicio());
            double[] pFim = getCentroCasa(obs.getFim());
            if (obs instanceof pt.ipvc.snakeladder.modelo.Escada) {
                gc.setEffect(sombraObstaculo); gc.setLineCap(StrokeLineCap.ROUND);
                double angEsc = Math.atan2(pFim[1] - pInicio[1], pFim[0] - pInicio[0]);
                double oX = Math.sin(angEsc) * 10, oY = -Math.cos(angEsc) * 10;
                gc.setStroke(Color.web("#3b200e")); gc.setLineWidth(7);
                gc.strokeLine(pInicio[0] + oX, pInicio[1] + oY, pFim[0] + oX, pFim[1] + oY);
                gc.strokeLine(pInicio[0] - oX, pInicio[1] - oY, pFim[0] - oX, pFim[1] - oY);
                gc.setEffect(null);
                gc.setStroke(Color.web("#7a4520")); gc.setLineWidth(3.5);
                gc.strokeLine(pInicio[0] + oX, pInicio[1] + oY, pFim[0] + oX, pFim[1] + oY);
                gc.strokeLine(pInicio[0] - oX, pInicio[1] - oY, pFim[0] - oX, pFim[1] - oY);
                for (int i = 1; i <= 7; i++) {
                    double fr = (double) i / 8;
                    double pX = pInicio[0] + (pFim[0] - pInicio[0]) * fr;
                    double pY = pInicio[1] + (pFim[1] - pInicio[1]) * fr;
                    gc.setStroke(Color.web("#3b200e")); gc.setLineWidth(5); gc.strokeLine(pX + oX, pY + oY, pX - oX, pY - oY);
                    gc.setStroke(Color.web("#8b522c")); gc.setLineWidth(2.5); gc.strokeLine(pX + oX, pY + oY, pX - oX, pY - oY);
                }
                gc.setLineCap(StrokeLineCap.SQUARE);
            } else if (obs instanceof pt.ipvc.snakeladder.modelo.Cobra) {
                gc.setEffect(sombraObstaculo); gc.setLineCap(StrokeLineCap.ROUND);
                double dist = Math.hypot(pFim[0] - pInicio[0], pFim[1] - pInicio[1]);
                double angCob = Math.atan2(pFim[1] - pInicio[1], pFim[0] - pInicio[0]);
                double amp = Math.min(dist * 0.15, 25);
                double c1X = pInicio[0] + Math.cos(angCob) * (dist * 0.3) + Math.sin(angCob) * amp;
                double c1Y = pInicio[1] + Math.sin(angCob) * (dist * 0.3) - Math.cos(angCob) * amp;
                double c2X = pInicio[0] + Math.cos(angCob) * (dist * 0.7) - Math.sin(angCob) * amp;
                double c2Y = pInicio[1] + Math.sin(angCob) * (dist * 0.7) + Math.cos(angCob) * amp;
                gc.setStroke(Color.web("#003300")); gc.setLineWidth(8); gc.beginPath();
                gc.moveTo(pInicio[0], pInicio[1]); gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pFim[0], pFim[1]); gc.stroke();
                gc.setEffect(null);
                gc.setStroke(Color.web("#228B22")); gc.setLineWidth(4.5); gc.beginPath();
                gc.moveTo(pInicio[0], pInicio[1]); gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pFim[0], pFim[1]); gc.stroke();
                gc.setStroke(Color.web("#ADFF2F")); gc.setLineWidth(1); gc.setLineDashes(2, 4); gc.beginPath();
                gc.moveTo(pInicio[0], pInicio[1]); gc.bezierCurveTo(c1X, c1Y, c2X, c2Y, pFim[0], pFim[1]); gc.stroke();
                gc.setLineDashes(0);
                double angHead = Math.atan2(pInicio[1] - c1Y, pInicio[0] - c1X);
                gc.save(); gc.translate(pInicio[0], pInicio[1]); gc.rotate(Math.toDegrees(angHead) + 90);
                gc.setStroke(Color.CRIMSON); gc.setLineWidth(1.5);
                gc.strokeLine(0, 0, 0, -14); gc.strokeLine(0, -14, -3, -19); gc.strokeLine(0, -14, 3, -19);
                gc.setFill(Color.web("#003300")); gc.fillOval(-10, -11, 20, 18);
                gc.setFill(Color.web("#228B22")); gc.fillOval(-7, -9, 14, 13);
                gc.setFill(Color.GOLD); gc.fillOval(-6, -10, 4, 6); gc.fillOval(2, -10, 4, 6);
                gc.setFill(Color.BLACK); gc.fillOval(-4.5, -9, 1.2, 4); gc.fillOval(3.3, -9, 1.2, 4);
                gc.restore(); gc.setLineWidth(1); gc.setLineCap(StrokeLineCap.SQUARE);
            }
        }
    }

    private void atualizarPosicaoGrafica(Jogador jogador, Circle circulo, int numeroCasa, double desvioX) {
        if (numeroCasa > 100) numeroCasa = 100;
        double[] pos = getCentroCasa(numeroCasa);
        circulo.setCenterX(pos[0] + desvioX);
        circulo.setCenterY(pos[1]);
    }

    private int calcularNumeroCasa(int linha, int coluna) {
        int linhaInvertida = 9 - linha;
        return linhaInvertida % 2 == 0 ? (linhaInvertida * 10) + coluna + 1 : (linhaInvertida * 10) + (9 - coluna) + 1;
    }
}