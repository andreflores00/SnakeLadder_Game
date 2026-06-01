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
import java.util.Optional;

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

        // --- TOPO: Menus (Atualizado com a Interface Multiplayer) ---
        MenuBar menuBar = new MenuBar();

        Menu menuFicheiro = new Menu("Ficheiro");
        menuFicheiro.getItems().addAll(new MenuItem("Novo Jogo Local"), new MenuItem("Carregar Tabuleiro"));

        Menu menuRede = new Menu("Rede (Multiplayer)");
        MenuItem menuCriarServidor = new MenuItem("Criar Servidor (Host)");
        MenuItem menuLigarCliente = new MenuItem("Ligar a um Jogo (Client)");
        menuRede.getItems().addAll(menuCriarServidor, menuLigarCliente);

        menuBar.getMenus().addAll(menuFicheiro, menuRede);
        root.setTop(menuBar);

        // --- Ações FALSAS da Interface de Rede (A Lógica virá depois) ---
        menuCriarServidor.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Modo Servidor");
            alert.setHeaderText("A aguardar ligação do Jogador 2...");
            alert.setContentText("O servidor está aberto na porta 5000. \n(Nota: A lógica de Sockets será inserida aqui em breve!)");
            alert.showAndWait();
            lblTurnoStatus.setText("★ MODO MULTIPLAYER (HOST) ★");
        });

        menuLigarCliente.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("127.0.0.1");
            dialog.setTitle("Modo Cliente");
            dialog.setHeaderText("Ligar a um jogo existente");
            dialog.setContentText("Insira o IP do Servidor:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(ip -> {
                System.out.println("A tentar ligar ao IP: " + ip);
                // A lógica de ligar o Socket virá para aqui
                lblTurnoStatus.setText("★ MODO MULTIPLAYER (CLIENT) ★");
            });
        });

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

        // --- AÇÃO DO BOTÃO ---
        btnLancarDado.setOnAction(e -> {
            if (!motorJogo.isJogoTerminado()) {
                motorJogo.jogarTurno();
                int posAtual = jogador1.getPosicao();
                lblDadoResultado.setText("[ Dado Lançado! ]\nPosição Atual: Casa " + posAtual);
            }
        });

        // --- LISTENER REATIVO (Observer) ---
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

        // Bloquear a casa 1 e a 100 para não haver obstáculos na partida nem na chegada
        casasOcupadas.add(1);
        casasOcupadas.add(100);

        int numEscadas = 5; // Podes mudar a quantidade aqui!
        int numCobras = 5;

        // 1. Gerar Escadas
        for (int i = 0; i < numEscadas; i++) {
            int inicio, fim;
            do {
                inicio = 2 + random.nextInt(70); // Base da escada
                fim = inicio + 10 + random.nextInt(20); // Topo da escada (garante que sobe)
                if (fim > 99) fim = 99;
            } while (casasOcupadas.contains(inicio) || casasOcupadas.contains(fim));

            casasOcupadas.add(inicio);
            casasOcupadas.add(fim);

            desenharEscadaPremium(gc, inicio, fim);

            // Sincronizar o visual com a lógica do jogo!
            motorJogo.getTabuleiro().adicionarObstaculo(new pt.ipvc.snakeladder.modelo.Escada(inicio, fim));
        }

        // 2. Gerar Cobras
        for (int i = 0; i < numCobras; i++) {
            int inicio, fim;
            do {
                inicio = 30 + random.nextInt(69); // Cabeça da cobra
                fim = inicio - 10 - random.nextInt(20); // Cauda da cobra (garante que desce)
                if (fim < 2) fim = 2;
            } while (casasOcupadas.contains(inicio) || casasOcupadas.contains(fim));

            casasOcupadas.add(inicio);
            casasOcupadas.add(fim);

            desenharCobraPremium(gc, inicio, fim);

            // Sincronizar o visual com a lógica do jogo!
            motorJogo.getTabuleiro().adicionarObstaculo(new pt.ipvc.snakeladder.modelo.Cobra(inicio, fim));
        }
    }

    private void desenharEscadaPremium(GraphicsContext gc, int inicio, int fim) {
        double[] pInicio = getCentroCasa(inicio);
        double[] pFim = getCentroCasa(fim);

        // Sombras para efeito 3D
        javafx.scene.effect.DropShadow sombra = new javafx.scene.effect.DropShadow();
        sombra.setOffsetY(4.0);
        sombra.setOffsetX(4.0);
        sombra.setColor(Color.color(0, 0, 0, 0.4));
        gc.setEffect(sombra);

        gc.setStroke(Color.web("#8B4513")); // Cor de madeira escura
        gc.setLineWidth(6);

        double angulo = Math.atan2(pFim[1] - pInicio[1], pFim[0] - pInicio[0]);
        double offsetX = Math.sin(angulo) * 14;
        double offsetY = -Math.cos(angulo) * 14;

        // Desenhar os dois ferros laterais da escada
        gc.strokeLine(pInicio[0] + offsetX, pInicio[1] + offsetY, pFim[0] + offsetX, pFim[1] + offsetY);
        gc.strokeLine(pInicio[0] - offsetX, pInicio[1] - offsetY, pFim[0] - offsetX, pFim[1] - offsetY);

        // Desenhar os degraus dinamicamente conforme o tamanho da escada
        gc.setLineWidth(4);
        gc.setStroke(Color.web("#A0522D")); // Cor de madeira mais clara para contraste
        int numDegraus = 5 + (int)(Math.hypot(pFim[0] - pInicio[0], pFim[1] - pInicio[1]) / 40);

        for (int i = 1; i <= numDegraus; i++) {
            double fracao = (double) i / (numDegraus + 1);
            double pX = pInicio[0] + (pFim[0] - pInicio[0]) * fracao;
            double pY = pInicio[1] + (pFim[1] - pInicio[1]) * fracao;
            gc.strokeLine(pX + offsetX, pY + offsetY, pX - offsetX, pY - offsetY);
        }
        gc.setEffect(null); // Limpar o efeito para não afetar as próximas renderizações
    }

    private void desenharCobraPremium(GraphicsContext gc, int inicio, int fim) {
        double[] pInicio = getCentroCasa(inicio); // Cabeça
        double[] pFim = getCentroCasa(fim); // Cauda

        // Sombras para efeito 3D
        javafx.scene.effect.DropShadow sombra = new javafx.scene.effect.DropShadow();
        sombra.setOffsetY(5.0);
        sombra.setOffsetX(3.0);
        sombra.setColor(Color.color(0, 0, 0, 0.5));
        gc.setEffect(sombra);

        double angulo = Math.atan2(pFim[1] - pInicio[1], pFim[0] - pInicio[0]);
        double dist = Math.hypot(pFim[0] - pInicio[0], pFim[1] - pInicio[1]);

        // Matemática para gerar um corpo orgânico em formato "S" (Curvas de Bézier)
        double cp1X = pInicio[0] + Math.cos(angulo) * (dist * 0.3) + Math.sin(angulo) * 60;
        double cp1Y = pInicio[1] + Math.sin(angulo) * (dist * 0.3) - Math.cos(angulo) * 60;

        double cp2X = pInicio[0] + Math.cos(angulo) * (dist * 0.7) - Math.sin(angulo) * 60;
        double cp2Y = pInicio[1] + Math.sin(angulo) * (dist * 0.7) + Math.cos(angulo) * 60;

        // Corpo Principal
        gc.setStroke(Color.web("#228B22")); // Verde floresta
        gc.setLineWidth(14);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.beginPath();
        gc.moveTo(pInicio[0], pInicio[1]);
        gc.bezierCurveTo(cp1X, cp1Y, cp2X, cp2Y, pFim[0], pFim[1]);
        gc.stroke();

        // Detalhe: Padrão nas costas da cobra
        gc.setStroke(Color.web("#32CD32")); // Verde lima
        gc.setLineWidth(4);
        gc.stroke();

        gc.setEffect(null); // Limpar sombra para os detalhes da cara

        // Cabeça
        gc.setFill(Color.web("#006400")); // Verde escuro
        gc.fillOval(pInicio[0] - 12, pInicio[1] - 12, 24, 24);

        // Olhos (Brancos)
        gc.setFill(Color.WHITE);
        gc.fillOval(pInicio[0] - 9, pInicio[1] - 6, 7, 7);
        gc.fillOval(pInicio[0] + 2, pInicio[1] - 6, 7, 7);

        // Pupilas (Pretas)
        gc.setFill(Color.BLACK);
        gc.fillOval(pInicio[0] - 7, pInicio[1] - 4, 3, 3);
        gc.fillOval(pInicio[0] + 4, pInicio[1] - 4, 3, 3);

        // Língua Bifurcada
        gc.setStroke(Color.CRIMSON);
        gc.setLineWidth(2);
        gc.strokeLine(pInicio[0], pInicio[1] - 12, pInicio[0], pInicio[1] - 20); // Base da língua
        gc.strokeLine(pInicio[0], pInicio[1] - 20, pInicio[0] - 4, pInicio[1] - 25); // Garfo esquerdo
        gc.strokeLine(pInicio[0], pInicio[1] - 20, pInicio[0] + 4, pInicio[1] - 25); // Garfo direito
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