package pt.ipvc.snakeladder;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
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
import pt.ipvc.snakeladder.rede.ClienteJogo;
import pt.ipvc.snakeladder.rede.ServidorJogo;

/**
 * Classe principal da aplicação Snake and Ladder.
 * Estende a classe Application do JavaFX e é responsável por construir
 * toda a interface gráfica, gerir as animações dinâmicas e lidar com a
 * comunicação de rede (Sockets) para o modo multiplayer.
 *
 * @author André e Eduardo
 * @version 1.3
 */
public class Main extends Application {

    private Stage palcoPrincipal;
    private Jogo motorJogo;
    private Jogador jogador1;
    private Jogador jogador2;
    private Circle pecaJogador1;
    private Circle pecaJogador2;
    private final int TAMANHO_CASA = 50;
    private StackPane areaJogo;
    private Label lblTurnoStatus;
    private Label lblEstadoDetalhado;
    private Label lblDadoResultado;
    private Label lblTituloPainel;
    private Label lblDadoIcon;
    private Button btnLancarDado;
    private Label lblVitoriaGigante = null;

    private boolean modoBot = false;
    private boolean modoRede = false;
    private boolean souHost = false;
    private boolean jogoInterrompido = false; // Flag para impedir ações após desistência

    private ServidorJogo servidor;
    private ClienteJogo cliente;

    private final String[] FACES_DADO = {"", "⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};
    private Timeline animacaoJ1;
    private Timeline animacaoJ2;

    // Efeitos Sonoros
    private AudioClip somDado;
    private AudioClip somCobra;
    private AudioClip somEscada;
    private AudioClip somVitoria;

    @Override
    public void start(Stage primaryStage) {
        this.palcoPrincipal = primaryStage;
        carregarSons();
        mostrarMenuPrincipal();
    }

    private void carregarSons() {
        try {
            somDado = new AudioClip(getClass().getResource("/resources/dado.wav").toExternalForm());
            somCobra = new AudioClip(getClass().getResource("/resources/cobra.mp3").toExternalForm());
            somEscada = new AudioClip(getClass().getResource("/resources/escada.wav").toExternalForm());
            somVitoria = new AudioClip(getClass().getResource("/resources/vitoria.wav").toExternalForm());
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao carregar ficheiros de som. Verifica a pasta resources.");
        }
    }

    private void mostrarMenuPrincipal() {
        VBox menuRoot = new VBox(25);
        menuRoot.setAlignment(Pos.CENTER);
        menuRoot.setStyle("-fx-background-color: linear-gradient(to bottom right, #f1f5f9, #cbd5e1);");

        Label lblTitulo = new Label("SNAKE & LADDER");
        lblTitulo.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 45));
        lblTitulo.setTextFill(Color.web("#1e293b"));
        lblTitulo.setEffect(new DropShadow(10, 0, 5, Color.color(0,0,0,0.15)));

        Label lblSubtitulo = new Label("Laboratório de Programação");
        lblSubtitulo.setFont(Font.font("System", FontWeight.NORMAL, 16));
        lblSubtitulo.setTextFill(Color.web("#475569"));
        VBox.setMargin(lblSubtitulo, new Insets(-15, 0, 30, 0));

        Button btnLocal = criarBotaoMenu("👥 2 Jogadores (Local)");
        btnLocal.setOnAction(e -> prepararJogo(false, false, false, null));

        Button btnBot = criarBotaoMenu("🤖 Jogador vs Bot");
        btnBot.setOnAction(e -> prepararJogo(true, false, false, null));

        Button btnHost = criarBotaoMenu("🌐 Criar Sala Online (Host)");
        btnHost.setOnAction(e -> {
            servidor = new ServidorJogo(valor -> Platform.runLater(() -> processarJogadaSincronizada(valor)));
            servidor.iniciarServidor(5000);
            prepararJogo(false, true, true, null);
        });

        Button btnClient = criarBotaoMenu("🌐 Entrar em Sala (Client)");
        btnClient.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("127.0.0.1");
            dialog.setTitle("Ligar a Sala");
            dialog.setHeaderText("Ligar ao Computador do Host");
            dialog.setContentText("Insere o IP do Servidor:");
            dialog.showAndWait().ifPresent(ip -> {
                cliente = new ClienteJogo(valor -> Platform.runLater(() -> processarJogadaSincronizada(valor)));
                cliente.conectar(ip, 5000);
                prepararJogo(false, true, false, ip);
            });
        });

        menuRoot.getChildren().addAll(lblTitulo, lblSubtitulo, btnLocal, btnBot, btnHost, btnClient);

        Scene cenaMenu = new Scene(menuRoot, 820, 660);
        palcoPrincipal.setTitle("Snake and Ladder - Laboratório de Programação");
        palcoPrincipal.setScene(cenaMenu);
        palcoPrincipal.show();
    }

    private Button criarBotaoMenu(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(280);
        btn.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: #ffffff; -fx-border-color: #94a3b8; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-text-fill: #334155; -fx-cursor: hand; -fx-padding: 12px;");
        btn.setEffect(new DropShadow(5, 0, 2, Color.color(0,0,0,0.08)));

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: #f8fafc; -fx-border-color: #3b82f6; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-text-fill: #2563eb; -fx-cursor: hand; -fx-padding: 12px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: #ffffff; -fx-border-color: #94a3b8; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-text-fill: #334155; -fx-cursor: hand; -fx-padding: 12px;"));

        return btn;
    }

    private void prepararJogo(boolean isBot, boolean isRede, boolean isHost, String ip) {
        this.modoBot = isBot;
        this.modoRede = isRede;
        this.souHost = isHost;
        this.jogoInterrompido = false;

        motorJogo = new Jogo();
        jogador1 = new Jogador(Color.DODGERBLUE);
        jogador2 = new Jogador(Color.ORANGE);
        jogador1.posicaoProperty().set(1);
        jogador2.posicaoProperty().set(1);

        motorJogo.adicionarJogador(jogador1);
        motorJogo.adicionarJogador(jogador2);
        configurarObstaculosFixos();
        motorJogo.iniciar();

        BorderPane rootJogo = construirEcraJogo();

        lblDadoIcon.setText("🎲");
        lblDadoResultado.setText("[ Jogo Iniciado ]");

        if (modoRede) {
            if (souHost) {
                lblTurnoStatus.setText("SALA CRIADA! JOGAS PRIMEIRO.");
            } else {
                lblTituloPainel.setText("ADVERSÁRIO");
                lblTituloPainel.setTextFill(jogador2.getCor());
                lblTurnoStatus.setText("A AGUARDAR JOGADA DO HOST...");
                lblTurnoStatus.setTextFill(jogador2.getCor());
                btnLancarDado.setDisable(true);
            }
        }

        ScrollPane scrollPane = new ScrollPane(rootJogo);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #f1f5f9; -fx-background: #f1f5f9; -fx-border-color: transparent;");
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            scrollPane.setVvalue(scrollPane.getVvalue() - event.getDeltaY() / 250.0);
            event.consume();
        });

        Scene cenaJogo = new Scene(scrollPane, 820, 660);
        palcoPrincipal.setScene(cenaJogo);
    }

    private BorderPane construirEcraJogo() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");
        DropShadow sombraPaineis = new DropShadow(15, 0, 5, Color.color(0, 0, 0, 0.08));

        // --- TOPO ---
        HBox barraTopo = new HBox(15);
        barraTopo.setStyle("-fx-background-color: #ffffff; -fx-padding: 10px 20px;");
        barraTopo.setEffect(new DropShadow(5, 0, 2, Color.color(0, 0, 0, 0.05)));
        barraTopo.setAlignment(Pos.CENTER_LEFT);

        Button btnVoltarMenu = new Button("⬅ Voltar ao Menu");
        btnVoltarMenu.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-text-fill: #334155;");

        btnVoltarMenu.setOnAction(e -> {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Abandonar Jogo");
            alerta.setHeaderText("Tens a certeza que queres voltar ao Menu?");
            alerta.setContentText("Todo o progresso desta partida será perdido.");

            alerta.showAndWait().ifPresent(resposta -> {
                if (resposta == ButtonType.OK) {
                    // Se o jogador sair a meio de um jogo online, manda o sinal de desistência para o outro PC!
                    if (modoRede && !jogoInterrompido && !motorJogo.isJogoTerminado()) {
                        if (souHost && servidor != null) servidor.enviarJogada(-1);
                        else if (!souHost && cliente != null) cliente.enviarJogada(-1);
                    }
                    this.servidor = null;
                    this.cliente = null;
                    mostrarMenuPrincipal();
                }
            });
        });

        barraTopo.getChildren().add(btnVoltarMenu);
        root.setTop(barraTopo);

        // --- CENTRO: Tabuleiro ---
        areaJogo = new StackPane();
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

        criarAnimacaoPosicao(pecaJogador1, 1, 1, -4, false);
        criarAnimacaoPosicao(pecaJogador2, 1, 1, 4, false);

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

        lblDadoIcon = new Label("🎲");
        lblDadoIcon.setFont(Font.font("System", 60));

        btnLancarDado = new Button("Lançar Dado");
        btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnLancarDado.setEffect(new DropShadow(8, 0, 3, Color.color(0.23, 0.51, 0.96, 0.4)));

        Region espacador = new Region();
        VBox.setVgrow(espacador, Priority.ALWAYS);

        // O botão altera o texto consoante estamos num jogo local ou multijogador
        Button btnReiniciar = new Button(modoRede ? "🏳️ Desistir da Partida" : "🏳️ Reiniciar Rápido");
        btnReiniciar.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-border-color: #fca5a5; -fx-border-radius: 6px; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6px 12px;");

        btnReiniciar.setOnAction(e -> {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle(modoRede ? "Desistir da Partida" : "Reiniciar Jogo");
            alerta.setHeaderText(modoRede ? "Queres mesmo desistir? O teu adversário vencerá por W.O." : "Queres reiniciar a partida?");
            alerta.showAndWait().ifPresent(resposta -> {
                if(resposta == ButtonType.OK) {
                    if (modoRede) {
                        if (!jogoInterrompido && !motorJogo.isJogoTerminado()) {
                            // Envia o código secreto -1 para o outro PC!
                            if (souHost && servidor != null) servidor.enviarJogada(-1);
                            else if (!souHost && cliente != null) cliente.enviarJogada(-1);

                            jogoInterrompido = true;
                            processarDesistencia(true);
                        }
                    } else {
                        prepararJogo(modoBot, modoRede, souHost, null);
                    }
                }
            });
        });

        painelLateral.getChildren().addAll(lblTituloPainel, lblDadoIcon, btnLancarDado, espacador, btnReiniciar);
        root.setRight(painelLateral);

        // --- BOTTOM: Menu de Estado ---
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

        String oponenteNome = modoRede ? "Adversário" : (modoBot ? "Bot" : "J2");
        lblEstadoDetalhado = new Label("Tu (Azul): Casa 1  |  " + oponenteNome + ": Casa 1");
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

        // --- ACÇÕES E LISTENERS ---
        btnLancarDado.setOnAction(e -> {
            if (!motorJogo.isJogoTerminado() && !jogoInterrompido) {
                btnLancarDado.setDisable(true);
                animarDadoEExecutar(() -> {
                    int valorSorteado = motorJogo.getDado().rolar();
                    lblDadoIcon.setText(FACES_DADO[valorSorteado]);

                    if (modoRede) {
                        if (souHost && servidor != null) servidor.enviarJogada(valorSorteado);
                        else if (!souHost && cliente != null) cliente.enviarJogada(valorSorteado);
                    }
                    processarJogadaSincronizada(valorSorteado);
                });
            }
        });

        jogador1.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            int posAntiga = oldVal.intValue();
            int novaPos = newVal.intValue();
            Timeline t = criarAnimacaoPosicao(pecaJogador1, posAntiga, novaPos, -4, true);

            if (t != null) {
                Runnable aoTerminar = () -> {
                    atualizarBarraEstado();
                    verificarCondicaoVitoria((modoRede && !souHost) ? 2 : 1, novaPos);
                };

                if (animacaoJ1 != null && animacaoJ1.getStatus() == Animation.Status.RUNNING) {
                    animacaoJ1.setOnFinished(e -> { animacaoJ1 = t; t.play(); t.setOnFinished(e2 -> aoTerminar.run()); });
                } else {
                    animacaoJ1 = t; t.play(); t.setOnFinished(e -> aoTerminar.run());
                }
            }
        });

        jogador2.posicaoProperty().addListener((obs, oldVal, newVal) -> {
            int posAntiga = oldVal.intValue();
            int novaPos = newVal.intValue();
            Timeline t = criarAnimacaoPosicao(pecaJogador2, posAntiga, novaPos, 4, true);

            if (t != null) {
                Runnable aoTerminar = () -> {
                    atualizarBarraEstado();
                    verificarCondicaoVitoria((modoRede && souHost) ? 2 : (modoBot ? 99 : 2), novaPos);
                };

                if (animacaoJ2 != null && animacaoJ2.getStatus() == Animation.Status.RUNNING) {
                    animacaoJ2.setOnFinished(e -> { animacaoJ2 = t; t.play(); t.setOnFinished(e2 -> aoTerminar.run()); });
                } else {
                    animacaoJ2 = t; t.play(); t.setOnFinished(e -> aoTerminar.run());
                }
            }
        });

        return root;
    }

    private void animarDadoEExecutar(Runnable acaoFinal) {
        if (somDado != null) {
            somDado.play();
        }

        ScaleTransition st = new ScaleTransition(Duration.millis(250), lblDadoIcon);
        st.setByX(0.5); st.setByY(0.5);
        st.setAutoReverse(true); st.setCycleCount(2);

        RotateTransition rt = new RotateTransition(Duration.millis(500), lblDadoIcon);
        rt.setByAngle(360);
        rt.setCycleCount(1);

        Timeline shuffleFaces = new Timeline(new KeyFrame(Duration.millis(50), evt -> {
            lblDadoIcon.setText(FACES_DADO[(int)(Math.random() * 6) + 1]);
        }));
        shuffleFaces.setCycleCount(10);
        shuffleFaces.setOnFinished(anim -> acaoFinal.run());

        st.play(); rt.play(); shuffleFaces.play();
    }

    private void processarJogadaSincronizada(int valorDado) {
        if (motorJogo.isJogoTerminado() || jogoInterrompido) return;

        // O SEGREDO: Intercetar a desistência enviada pelo adversário
        if (valorDado == -1) {
            jogoInterrompido = true;
            processarDesistencia(false);
            return;
        }

        int indexAtual = motorJogo.getJogadorAtualIndex();
        boolean euJoguei = (!modoRede) ? (indexAtual == 0) : ((souHost && indexAtual == 0) || (!souHost && indexAtual == 1));

        String prefixoTexto = euJoguei ? "Tu tiraste" : "O Adversário tirou";
        if (!modoRede && modoBot && indexAtual == 1) prefixoTexto = "O Bot tirou";

        lblDadoIcon.setText(FACES_DADO[valorDado]);
        motorJogo.jogarTurno(valorDado);
        lblDadoResultado.setText(prefixoTexto + " um " + valorDado + "!");

        if (motorJogo.isJogoTerminado()) return;

        int seguinteIndex = motorJogo.getJogadorAtualIndex();
        Jogador seguinteJogador = motorJogo.getJogadores().get(seguinteIndex);

        if (!modoRede && modoBot && seguinteIndex == 1) {
            lblTituloPainel.setText("BOT (Laranja)");
            lblTituloPainel.setTextFill(jogador2.getCor());
            lblTurnoStatus.setText("TURNO DO BOT...");
            lblTurnoStatus.setTextFill(jogador2.getCor());
            btnLancarDado.setDisable(true);

            PauseTransition atrasoIA = new PauseTransition(Duration.seconds(1.5));
            atrasoIA.setOnFinished(evt -> {
                if (!jogoInterrompido) {
                    animarDadoEExecutar(() -> {
                        int valorSorteadoBot = motorJogo.getDado().rolar();
                        lblDadoIcon.setText(FACES_DADO[valorSorteadoBot]);
                        processarJogadaSincronizada(valorSorteadoBot);
                    });
                }
            });
            atrasoIA.play();
        } else {
            boolean eMinhaVez = (!modoRede) ? true : ((souHost && seguinteIndex == 0) || (!souHost && seguinteIndex == 1));

            lblTituloPainel.setText(eMinhaVez ? "O TEU TURNO" : "ADVERSÁRIO");
            lblTituloPainel.setTextFill(seguinteJogador.getCor());
            lblTurnoStatus.setText(eMinhaVez ? "A TUA VEZ DE JOGAR" : "TURNO DO ADVERSÁRIO");
            lblTurnoStatus.setTextFill(seguinteJogador.getCor());

            btnLancarDado.setDisable(!eMinhaVez);
            btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand; " +
                    (seguinteIndex == 0 ? "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);" : "-fx-background-color: linear-gradient(to right, #f97316, #ea580c);"));
        }
    }

    /**
     * Aplica os efeitos visuais e sonoros de uma desistência no tabuleiro.
     */
    private void processarDesistencia(boolean euDesisti) {
        if (!euDesisti && somVitoria != null) {
            somVitoria.play();
        }

        String mensagemFinal = euDesisti ? "DESISTISTE!" : "ADVERSÁRIO DESISTIU!";
        Color corVitoria = euDesisti ? Color.CRIMSON : Color.web("#10b981");

        lblTurnoStatus.setTextFill(corVitoria);
        lblTurnoStatus.setText(mensagemFinal);
        lblTituloPainel.setText("FIM DE JOGO");
        btnLancarDado.setDisable(true);
        lblDadoResultado.setText(euDesisti ? "[ Abandonaste a partida ]" : "[ Vitória por W.O. ]");

        if(lblVitoriaGigante == null) {
            lblVitoriaGigante = new Label(mensagemFinal);
            lblVitoriaGigante.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 45));
            lblVitoriaGigante.setTextFill(corVitoria);

            DropShadow sombra = new DropShadow();
            sombra.setRadius(15); sombra.setOffsetX(5); sombra.setOffsetY(5);
            sombra.setColor(Color.color(0, 0, 0, 0.7));
            lblVitoriaGigante.setEffect(sombra);
            areaJogo.getChildren().add(lblVitoriaGigante);
        } else {
            lblVitoriaGigante.setText(mensagemFinal);
            lblVitoriaGigante.setTextFill(corVitoria);
        }

        lblVitoriaGigante.setScaleX(0.0);
        lblVitoriaGigante.setScaleY(0.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(800), lblVitoriaGigante);
        st.setToX(1.3); st.setToY(1.3);
        st.setCycleCount(2); st.setAutoReverse(true);
        st.setOnFinished(e -> {
            ScaleTransition st2 = new ScaleTransition(Duration.millis(300), lblVitoriaGigante);
            st2.setToX(1.0); st2.setToY(1.0);
            st2.play();
        });
        st.play();
    }

    private RadialGradient criarGradientePeca(Color corBase) {
        return new RadialGradient(0, 0, 0.35, 0.35, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE), new Stop(0.2, corBase.brighter()),
                new Stop(0.8, corBase.darker()), new Stop(1, Color.web("#0a1a3a")));
    }

    private void atualizarBarraEstado() {
        int posJ1 = jogador1.getPosicao() > 100 ? 100 : jogador1.getPosicao();
        int posJ2 = jogador2.getPosicao() > 100 ? 100 : jogador2.getPosicao();
        String oponente = modoRede ? "Adversário" : (modoBot ? "Bot" : "J2");
        lblEstadoDetalhado.setText("Tu (Azul): Casa " + posJ1 + "  |  " + oponente + ": Casa " + posJ2);
    }

    private void verificarCondicaoVitoria(int idJogador, int localizacao) {
        if (motorJogo.isJogoTerminado() || localizacao >= 100) {

            if (somVitoria != null) {
                somVitoria.play();
            }

            String mensagemFinal;
            Color corVitoria;

            if (idJogador == 1) {
                mensagemFinal = "GANHASTE!";
                corVitoria = Color.web("#10b981");
            } else if (idJogador == 99) {
                mensagemFinal = "BOT VENCEU!";
                corVitoria = Color.CRIMSON;
            } else {
                mensagemFinal = "ADVERSÁRIO GANHOU!";
                corVitoria = Color.CRIMSON;
            }

            lblTurnoStatus.setTextFill(corVitoria);
            lblTurnoStatus.setText(mensagemFinal);
            lblTituloPainel.setText("FIM DE JOGO");
            btnLancarDado.setDisable(true);

            if(lblVitoriaGigante == null) {
                lblVitoriaGigante = new Label(mensagemFinal);
                lblVitoriaGigante.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 70));
                lblVitoriaGigante.setTextFill(corVitoria);

                DropShadow sombra = new DropShadow();
                sombra.setRadius(15); sombra.setOffsetX(5); sombra.setOffsetY(5);
                sombra.setColor(Color.color(0, 0, 0, 0.7));
                lblVitoriaGigante.setEffect(sombra);
                areaJogo.getChildren().add(lblVitoriaGigante);
            }

            lblVitoriaGigante.setScaleX(0.0);
            lblVitoriaGigante.setScaleY(0.0);
            ScaleTransition st = new ScaleTransition(Duration.millis(800), lblVitoriaGigante);
            st.setToX(1.3); st.setToY(1.3);
            st.setCycleCount(2); st.setAutoReverse(true);
            st.setOnFinished(e -> {
                ScaleTransition st2 = new ScaleTransition(Duration.millis(300), lblVitoriaGigante);
                st2.setToX(1.0); st2.setToY(1.0); st2.play();
            });
            st.play();
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

    private Timeline criarAnimacaoPosicao(Circle circulo, int posAntiga, int novaPos, double desvioX, boolean animar) {
        if (novaPos > 100) novaPos = 100;

        if (!animar || posAntiga <= 0) {
            double[] pos = getCentroCasa(novaPos);
            circulo.setCenterX(pos[0] + desvioX);
            circulo.setCenterY(pos[1]);
            return null;
        }

        Timeline timeline = new Timeline();
        double tempoAcumulado = 0;

        if (novaPos > posAntiga && (novaPos - posAntiga) <= 6) {
            for (int i = posAntiga + 1; i <= novaPos; i++) {
                double[] posAnterior = getCentroCasa(i - 1);
                double[] posAtual = getCentroCasa(i);

                double startX = posAnterior[0] + desvioX;
                double startY = posAnterior[1];
                double endX = posAtual[0] + desvioX;
                double endY = posAtual[1];

                double midX = startX + (endX - startX) / 2.0;
                double midY = Math.min(startY, endY) - 25;

                tempoAcumulado += 150;
                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(tempoAcumulado),
                        new KeyValue(circulo.centerXProperty(), midX, javafx.animation.Interpolator.EASE_OUT),
                        new KeyValue(circulo.centerYProperty(), midY, javafx.animation.Interpolator.EASE_OUT)
                ));

                tempoAcumulado += 150;
                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(tempoAcumulado),
                        new KeyValue(circulo.centerXProperty(), endX, javafx.animation.Interpolator.EASE_IN),
                        new KeyValue(circulo.centerYProperty(), endY, javafx.animation.Interpolator.EASE_IN)
                ));
            }
        } else {
            if (novaPos < posAntiga) {
                if (somCobra != null) somCobra.play();
            } else {
                if (somEscada != null) somEscada.play();
            }

            double[] posDestino = getCentroCasa(novaPos);
            tempoAcumulado += 600;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(tempoAcumulado),
                    new KeyValue(circulo.centerXProperty(), posDestino[0] + desvioX, javafx.animation.Interpolator.EASE_BOTH),
                    new KeyValue(circulo.centerYProperty(), posDestino[1], javafx.animation.Interpolator.EASE_BOTH)
            ));
        }

        return timeline;
    }

    private int calcularNumeroCasa(int linha, int coluna) {
        int linhaInvertida = 9 - linha;
        return linhaInvertida % 2 == 0 ? (linhaInvertida * 10) + coluna + 1 : (linhaInvertida * 10) + (9 - coluna) + 1;
    }
}