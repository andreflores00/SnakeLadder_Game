package pt.ipvc.snakeladder;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
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
        root.setStyle("-fx-background-color: #f4f7f6;"); // Fundo moderno e limpo

        // --- TOPO: Menus ---
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0; -fx-padding: 3px;");

        Menu menuFicheiro = new Menu("Ficheiro");
        menuFicheiro.getItems().addAll(new MenuItem("Novo Jogo Local"), new MenuItem("Carregar Tabuleiro"));

        Menu menuRede = new Menu("Rede (Multiplayer)");
        MenuItem menuCriarServidor = new MenuItem("Criar Servidor (Host)");
        MenuItem menuLigarCliente = new MenuItem("Ligar a um Jogo (Client)");
        menuRede.getItems().addAll(menuCriarServidor, menuLigarCliente);

        menuBar.getMenus().addAll(menuFicheiro, menuRede);
        root.setTop(menuBar);

        // Ações FALSAS da Interface de Rede (A Lógica virá depois)
        menuCriarServidor.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Modo Servidor");
            alert.setHeaderText("A aguardar ligação do Jogador 2...");
            alert.setContentText("O servidor está aberto na porta 5000.\n(Lógica de rede será implementada no próximo sprint)");
            alert.showAndWait();
            lblTurnoStatus.setText("★ MODO MULTIPLAYER (HOST) ★");
        });

        menuLigarCliente.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("127.0.0.1");
            dialog.setTitle("Modo Cliente");
            dialog.setHeaderText("Ligar a um jogo existente");
            dialog.setContentText("Insira o IP do Servidor:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(ip -> lblTurnoStatus.setText("★ MODO MULTIPLAYER (CLIENT) ★"));
        });

        // --- CENTRO: Tabuleiro Premium ---
        StackPane areaJogo = new StackPane();
        areaJogo.setPadding(new Insets(20)); // Margem para o tabuleiro respirar

        // Fundo do tabuleiro com sombra projetada (Efeito 3D)
        Pane fundoTabuleiro = new Pane();
        fundoTabuleiro.setMaxSize(600, 600);
        fundoTabuleiro.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");
        DropShadow sombraTabuleiro = new DropShadow(20, Color.rgb(0, 0, 0, 0.15));
        fundoTabuleiro.setEffect(sombraTabuleiro);

        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        desenharTabuleiro(gc);
        desenharObstaculosVisuais(gc);

        Pane camadaPecas = new Pane();
        camadaPecas.setMaxSize(600, 600);

        // Peça do jogador com gradiente 3D
        pecaGrafica = new Circle(TAMANHO_CASA / 3.0);
        LinearGradient gradientePeca = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.LIGHTSKYBLUE), new Stop(1, Color.DODGERBLUE));
        pecaGrafica.setFill(gradientePeca);
        pecaGrafica.setEffect(new DropShadow(5, 2, 2, Color.rgb(0,0,0,0.4)));

        atualizarPosicaoGrafica(1);
        camadaPecas.getChildren().add(pecaGrafica);

        areaJogo.getChildren().addAll(fundoTabuleiro, canvas, camadaPecas);
        root.setCenter(areaJogo);

        // --- LATERAL: Painel de Controlo Estilizado ---
        VBox painelLateral = new VBox(25);
        painelLateral.setStyle("-fx-padding: 40px 30px; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, -5, 0);");
        painelLateral.setAlignment(Pos.TOP_CENTER);
        painelLateral.setPrefWidth(220);

        Label lblTitulo = new Label("JOGADOR 1");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BLACK, 20));
        lblTitulo.setTextFill(Color.DODGERBLUE);

        Label lblDadoIcon = new Label("🎲");
        lblDadoIcon.setFont(Font.font("System", 60));
        lblDadoIcon.setEffect(new DropShadow(5, 0, 3, Color.rgb(0,0,0,0.2)));

        Button btnLancarDado = new Button("Lançar Dado");
        // Botão com estética premium, gradiente sutil verde desportivo
        btnLancarDado.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 15px; -fx-font-weight: bold; " +
                        "-fx-background-color: linear-gradient(to bottom, #2ecc71, #27ae60); " +
                        "-fx-text-fill: white; -fx-padding: 12px 25px; " +
                        "-fx-background-radius: 8px; -fx-cursor: hand;"
        );
        btnLancarDado.setEffect(new DropShadow(5, 0, 3, Color.rgb(39, 174, 96, 0.4)));

        painelLateral.getChildren().addAll(lblTitulo, lblDadoIcon, btnLancarDado);
        root.setRight(painelLateral);

        // --- BOTTOM: Barra de Estado Limpa ---
        GridPane barraInferior = new GridPane();
        barraInferior.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-border-color: #e2e8f0; -fx-border-width: 1px 0 0 0;");
        barraInferior.setHgap(50);
        barraInferior.setVgap(8);

        lblTurnoStatus = new Label("O SEU TURNO");
        lblTurnoStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblTurnoStatus.setTextFill(Color.web("#2d3436"));

        lblCorStatus = new Label("A sua cor é Azul");
        lblCorStatus.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        lblCorStatus.setTextFill(Color.DODGERBLUE);

        lblEstadoDetalhado = new Label("Posição: Casa 1");
        lblEstadoDetalhado.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        lblEstadoDetalhado.setTextFill(Color.web("#636e72"));

        lblDadoResultado = new Label("Último Dado: —");
        lblDadoResultado.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblDadoResultado.setTextFill(Color.web("#636e72"));

        barraInferior.add(lblTurnoStatus, 0, 0);
        barraInferior.add(lblCorStatus, 0, 1);
        barraInferior.add(lblEstadoDetalhado, 1, 0);
        barraInferior.add(lblDadoResultado, 1, 1);

        root.setBottom(barraInferior);

        // --- AÇÃO DO BOTÃO ---
        btnLancarDado.setOnAction(e -> {
            if (!motorJogo.isJogoTerminado()) {
                motorJogo.jogarTurno();
                lblDadoResultado.setText("Último Dado: " + motorJogo.getDado().getValor());
            }
        });

        // --- LISTENER REATIVO ---
        jogador1.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            int novaPosicao = newVal.intValue();
            lblEstadoDetalhado.setText("Posição: Casa " + (novaPosicao > 100 ? 100 : novaPosicao));
            atualizarPosicaoGrafica(novaPosicao);

            if (motorJogo.isJogoTerminado() || novaPosicao >= 100) {
                lblTurnoStatus.setText("🏆 VITÓRIA ALCANÇADA!");
                lblTurnoStatus.setTextFill(Color.web("#27ae60"));
                btnLancarDado.setDisable(true);
            }
        });

        Scene scene = new Scene(root, 850, 750);
        primaryStage.setTitle("Snake and Ladder - Laboratório de Programação");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void desenharTabuleiro(GraphicsContext gc) {
        // Cores base mais suaves
        Color corEscura = Color.web("#f1f2f6");
        Color corClara = Color.web("#ffffff");
        Color corBorda = Color.web("#dfe4ea");

        for (int linha = 0; linha < 10; linha++) {
            for (int coluna = 0; coluna < 10; coluna++) {
                int x = coluna * TAMANHO_CASA;
                int y = linha * TAMANHO_CASA;

                // Fundo da célula
                gc.setFill((linha + coluna) % 2 == 0 ? corEscura : corClara);
                gc.fillRect(x, y, TAMANHO_CASA, TAMANHO_CASA);

                // Desenhar um bloco interior para dar efeito de textura de azulejo/cartão
                gc.setFill((linha + coluna) % 2 == 0 ? Color.web("#e4e7ec") : Color.web("#f8f9fa"));
                gc.fillRoundRect(x + 4, y + 4, TAMANHO_CASA - 8, TAMANHO_CASA - 8, 10, 10);

                // Grelha discreta
                gc.setStroke(corBorda);
                gc.setLineWidth(1);
                gc.strokeRect(x, y, TAMANHO_CASA, TAMANHO_CASA);

                // Números com tipografia mais elegante
                int numeroCasa = calcularNumeroCasa(linha, coluna);
                gc.setFill(Color.web("#a4b0be"));
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                gc.fillText(String.valueOf(numeroCasa), x + 8, y + 20);
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
        // Vetores FIXOS redesenhados para criar variedade visual (inclinações para a esquerda e direita)
        int[][] escadasPos = {
                {4, 25},   // Inclina para a direita
                {15, 36},  // Inclina para a esquerda
                {21, 42},  // Inclina para a direita (canto inferior esquerdo)
                {55, 76},  // Inclina para a esquerda
                {72, 91}   // Inclina para a direita (canto superior direito)
        };

        int[][] cobrasPos = {
                {33, 18},  // Inclina para a esquerda
                {48, 29},  // Inclina para a direita
                {67, 51},  // Inclina para a direita
                {82, 61},  // Inclina para a esquerda
                {97, 78}   // Inclina para a esquerda (canto superior esquerdo)
        };

        for (int[] pos : escadasPos) {
            desenharEscadaPremium(gc, pos[0], pos[1]);
            motorJogo.getTabuleiro().adicionarObstaculo(new pt.ipvc.snakeladder.modelo.Escada(pos[0], pos[1]));
        }

        for (int[] pos : cobrasPos) {
            desenharCobraPremium(gc, pos[0], pos[1]);
            motorJogo.getTabuleiro().adicionarObstaculo(new pt.ipvc.snakeladder.modelo.Cobra(pos[0], pos[1]));
        }
    }

    private void desenharEscadaPremium(GraphicsContext gc, int inicio, int fim) {
        double[] pInicio = getCentroCasa(inicio);
        double[] pFim = getCentroCasa(fim);

        javafx.scene.effect.DropShadow sombra = new javafx.scene.effect.DropShadow(6, 3, 3, Color.rgb(0, 0, 0, 0.4));
        gc.setEffect(sombra);

        gc.setStroke(Color.web("#d35400")); // Madeira rica
        gc.setLineWidth(7);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        double angulo = Math.atan2(pFim[1] - pInicio[1], pFim[0] - pInicio[0]);
        double offsetX = Math.sin(angulo) * 12;
        double offsetY = -Math.cos(angulo) * 12;

        gc.strokeLine(pInicio[0] + offsetX, pInicio[1] + offsetY, pFim[0] + offsetX, pFim[1] + offsetY);
        gc.strokeLine(pInicio[0] - offsetX, pInicio[1] - offsetY, pFim[0] - offsetX, pFim[1] - offsetY);

        gc.setLineWidth(4);
        gc.setStroke(Color.web("#e67e22"));
        int numDegraus = 4 + (int)(Math.hypot(pFim[0] - pInicio[0], pFim[1] - pInicio[1]) / 35);

        for (int i = 1; i <= numDegraus; i++) {
            double fracao = (double) i / (numDegraus + 1);
            double pX = pInicio[0] + (pFim[0] - pInicio[0]) * fracao;
            double pY = pInicio[1] + (pFim[1] - pInicio[1]) * fracao;
            gc.strokeLine(pX + offsetX, pY + offsetY, pX - offsetX, pY - offsetY);
        }
        gc.setEffect(null);
    }

    private void desenharCobraPremium(GraphicsContext gc, int inicio, int fim) {
        double[] pInicio = getCentroCasa(inicio);
        double[] pFim = getCentroCasa(fim);

        javafx.scene.effect.DropShadow sombra = new javafx.scene.effect.DropShadow(8, 4, 4, Color.rgb(0, 0, 0, 0.35));
        gc.setEffect(sombra);

        double angulo = Math.atan2(pFim[1] - pInicio[1], pFim[0] - pInicio[0]);
        double dist = Math.hypot(pFim[0] - pInicio[0], pFim[1] - pInicio[1]);

        double cp1X = pInicio[0] + Math.cos(angulo) * (dist * 0.3) + Math.sin(angulo) * 55;
        double cp1Y = pInicio[1] + Math.sin(angulo) * (dist * 0.3) - Math.cos(angulo) * 55;
        double cp2X = pInicio[0] + Math.cos(angulo) * (dist * 0.7) - Math.sin(angulo) * 55;
        double cp2Y = pInicio[1] + Math.sin(angulo) * (dist * 0.7) + Math.cos(angulo) * 55;

        // Corpo
        gc.setStroke(Color.web("#20bf6b"));
        gc.setLineWidth(15);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.beginPath();
        gc.moveTo(pInicio[0], pInicio[1]);
        gc.bezierCurveTo(cp1X, cp1Y, cp2X, cp2Y, pFim[0], pFim[1]);
        gc.stroke();

        // Padrão interno para dar textura
        gc.setStroke(Color.web("#26de81"));
        gc.setLineWidth(5);
        gc.stroke();
        gc.setEffect(null);

        // Cabeça
        gc.setFill(Color.web("#20bf6b"));
        gc.fillOval(pInicio[0] - 13, pInicio[1] - 13, 26, 26);

        // Olhos expressivos
        gc.setFill(Color.WHITE);
        gc.fillOval(pInicio[0] - 10, pInicio[1] - 8, 8, 8);
        gc.fillOval(pInicio[0] + 2, pInicio[1] - 8, 8, 8);

        gc.setFill(Color.BLACK);
        gc.fillOval(pInicio[0] - 8, pInicio[1] - 6, 4, 4);
        gc.fillOval(pInicio[0] + 4, pInicio[1] - 6, 4, 4);

        // Língua bifurcada estilizada
        gc.setStroke(Color.web("#eb3b5a"));
        gc.setLineWidth(2.5);
        gc.strokeLine(pInicio[0], pInicio[1] - 13, pInicio[0], pInicio[1] - 22);
        gc.strokeLine(pInicio[0], pInicio[1] - 22, pInicio[0] - 5, pInicio[1] - 28);
        gc.strokeLine(pInicio[0], pInicio[1] - 22, pInicio[0] + 5, pInicio[1] - 28);
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