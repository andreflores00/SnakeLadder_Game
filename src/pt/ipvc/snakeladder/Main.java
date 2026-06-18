package pt.ipvc.snakeladder;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Interpolator;
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
import javafx.scene.media.AudioClip;
import java.net.URL;

import pt.ipvc.snakeladder.modelo.Jogo;
import pt.ipvc.snakeladder.modelo.Jogador;
import pt.ipvc.snakeladder.rede.ClienteJogo;
import pt.ipvc.snakeladder.rede.ServidorJogo;

import java.util.Optional;

/**
 * Classe principal da aplicação Snake and Ladder.
 * Estende a classe Application do JavaFX e é responsável por construir
 * toda a interface gráfica, gerir as animações dinâmicas e lidar com a
 * comunicação de rede (Sockets) para o modo multiplayer.
 *
 * @author André e Eduardo
 * @version 1.0
 */
public class Main extends Application {

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
    private TextArea txtHistorico;

    private Label lblVitoriaGigante = null;
    private SequentialTransition animacaoVitoriaAtiva = null;

    private boolean modoBot = false;
    private boolean modoRede = false;
    private boolean souHost = false;
    private ServidorJogo servidor;
    private ClienteJogo cliente;

    // Nomes dos jogadores (com sugestões pré-definidas para agilizar)
    private String nomeJ1 = "Dudz";
    private String nomeJ2 = "Adversário";

    // Sons do Jogo
    private AudioClip somDado;
    private AudioClip somEscada;
    private AudioClip somCobra;
    private AudioClip somVitoria;

    private final String[] FACES_DADO = {"", "⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};

    private Timeline animacaoJ1;
    private Timeline animacaoJ2;

    @Override
    public void start(Stage primaryStage) {
        carregarSons(); // Carrega os recursos de áudio logo ao abrir

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

        // --- TOPO: Menus ---
        HBox barraTopo = new HBox(15);
        barraTopo.setStyle("-fx-background-color: #ffffff; -fx-padding: 10px 20px;");
        barraTopo.setEffect(new DropShadow(5, 0, 2, Color.color(0, 0, 0, 0.05)));
        barraTopo.setAlignment(Pos.CENTER_LEFT);

        MenuButton btnNovoJogo = new MenuButton("🔄 Novo Jogo Local");
        btnNovoJogo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-text-fill: #334155;");
        MenuItem itemLocal = new MenuItem("👥 2 Jogadores (Local)");
        MenuItem itemBot = new MenuItem("🤖 Jogador vs Bot");
        btnNovoJogo.getItems().addAll(itemLocal, itemBot);

        MenuButton btnRede = new MenuButton("🌐 Multiplayer Online");
        btnRede.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-text-fill: #334155;");
        MenuItem itemHost = new MenuItem("Criar Sala (Host)");
        MenuItem itemClient = new MenuItem("Entrar em Sala (Client)");
        btnRede.getItems().addAll(itemHost, itemClient);

        barraTopo.getChildren().addAll(btnNovoJogo, btnRede);
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
        VBox painelLateral = new VBox(15);
        painelLateral.setStyle("-fx-background-color: #ffffff; -fx-padding: 25px 20px; -fx-background-radius: 12px;");
        painelLateral.setEffect(sombraPaineis);
        painelLateral.setAlignment(Pos.TOP_CENTER);
        painelLateral.setPrefWidth(240);
        BorderPane.setMargin(painelLateral, new Insets(15, 20, 15, 10));

        lblTituloPainel = new Label(nomeJ1.toUpperCase());
        lblTituloPainel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));
        lblTituloPainel.setTextFill(jogador1.getCor());

        lblDadoIcon = new Label("🎲");
        lblDadoIcon.setFont(Font.font("System", 60));

        btnLancarDado = new Button("Lançar Dado");
        btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnLancarDado.setEffect(new DropShadow(8, 0, 3, Color.color(0.23, 0.51, 0.96, 0.4)));

        Region espacador = new Region();
        VBox.setVgrow(espacador, Priority.ALWAYS);

        Button btnDesistir = new Button("🏳️ Reiniciar Rápido");
        btnDesistir.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-border-color: #fca5a5; -fx-border-radius: 6px; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6px 12px;");
        btnDesistir.setOnAction(e -> reiniciarJogo(modoBot, gc, canvas));

        painelLateral.getChildren().addAll(lblTituloPainel, lblDadoIcon, btnLancarDado, espacador, btnDesistir);
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

        lblEstadoDetalhado = new Label(nomeJ1 + " (Azul): Casa 1  |  " + nomeJ2 + " (Laranja): Casa 1");
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

        // --- ACÇÕES DOS BOTÕES ---
        btnLancarDado.setOnAction(e -> {
            if (!motorJogo.isJogoTerminado()) {
                btnLancarDado.setDisable(true);
                tocarSom(somDado, 0.6); // Toca som do dado ao clicar
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

        itemLocal.setOnAction(e -> confirmarEExecutar(
                "Novo Jogo Local",
                "Iniciar nova partida para 2 Jogadores?",
                () -> {
                    pedirNomes(false);
                    reiniciarJogo(false, gc, canvas);
                }
        ));

        itemBot.setOnAction(e -> confirmarEExecutar(
                "Novo Jogo contra Bot",
                "Iniciar nova partida contra a Inteligência Artificial?",
                () -> {
                    pedirNomes(true);
                    reiniciarJogo(true, gc, canvas);
                }
        ));

        itemHost.setOnAction(e -> {
            pedirNomes(true); // Só pede o nome local
            servidor = new ServidorJogo(valor -> Platform.runLater(() -> processarJogadaSincronizada(valor)));
            servidor.iniciarServidor(5000);
            modoRede = true; souHost = true;
            reiniciarJogo(false, gc, canvas);
            lblTurnoStatus.setText("SALA CRIADA! JOGAS PRIMEIRO.");
        });

        itemClient.setOnAction(e -> {
            pedirNomes(true); // Só pede o nome local
            TextInputDialog dialog = new TextInputDialog("127.0.0.1");
            dialog.setTitle("Ligar a Sala");
            dialog.setHeaderText("Ligar ao Computador do Host");
            dialog.setContentText("Insere o IP do Servidor:");
            dialog.showAndWait().ifPresent(ip -> {
                cliente = new ClienteJogo(valor -> Platform.runLater(() -> processarJogadaSincronizada(valor)));
                cliente.conectar(ip, 5000);
                modoRede = true; souHost = false;
                reiniciarJogo(false, gc, canvas);

                btnLancarDado.setDisable(true);
                lblTituloPainel.setText("ADVERSÁRIO");
                lblTituloPainel.setTextFill(jogador1.getCor());
                lblTurnoStatus.setText("A AGUARDAR JOGADA DO HOST...");
                lblTurnoStatus.setTextFill(jogador1.getCor());
            });
        });

        // --- LISTENERS REATIVOS ---
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
                    animacaoJ1.setOnFinished(e -> {
                        animacaoJ1 = t;
                        t.play();
                        t.setOnFinished(e2 -> aoTerminar.run());
                    });
                } else {
                    animacaoJ1 = t;
                    t.play();
                    t.setOnFinished(e -> aoTerminar.run());
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
                    animacaoJ2.setOnFinished(e -> {
                        animacaoJ2 = t;
                        t.play();
                        t.setOnFinished(e2 -> aoTerminar.run());
                    });
                } else {
                    animacaoJ2 = t;
                    t.play();
                    t.setOnFinished(e -> aoTerminar.run());
                }
            }
        });

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true); scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #f1f5f9; -fx-background: #f1f5f9; -fx-border-color: transparent;");
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            scrollPane.setVvalue(scrollPane.getVvalue() - event.getDeltaY() / 250.0);
            event.consume();
        });

        Scene scene = new Scene(scrollPane, 820, 660);
        primaryStage.setTitle("Snake and Ladder - Laboratório de Programação");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Pede os nomes logo na abertura da App para personalizar a primeira partida
        Platform.runLater(() -> {
            pedirNomes(false);
            atualizarTextosComNomes();
        });
    }

    // --- MÉTODOS DE ÁUDIO ---
    private void carregarSons() {
        try {
            // Repara que agora adicionámos o "/resources/" antes do nome do ficheiro
            URL urlDado = getClass().getResource("/resources/dado.wav");
            if (urlDado != null) somDado = new AudioClip(urlDado.toExternalForm());

            URL urlEscada = getClass().getResource("/resources/escada.wav");
            if (urlEscada != null) somEscada = new AudioClip(urlEscada.toExternalForm());

            URL urlCobra = getClass().getResource("/resources/cobra.mp3");
            if (urlCobra != null) somCobra = new AudioClip(urlCobra.toExternalForm());

            URL urlVitoria = getClass().getResource("/resources/vitoria.wav");
            if (urlVitoria != null) somVitoria = new AudioClip(urlVitoria.toExternalForm());

        } catch (Exception e) {
            System.out.println("Aviso: Ficheiros de som não configurados. A jogar em modo silencioso.");
        }
    }

    private void tocarSom(AudioClip clip, double duracaoSegundos) {
        if (clip != null) {
            clip.play(); // Começa a tocar o som

            // Se a duração for maior que zero, cria um temporizador para o desligar
            if (duracaoSegundos > 0) {
                PauseTransition pararSom = new PauseTransition(Duration.seconds(duracaoSegundos));
                pararSom.setOnFinished(e -> clip.stop());
                pararSom.play();
            }
        }
    }

    // --- GESTÃO DE NOMES ---
    private void pedirNomes(boolean modoSoP1) {
        TextInputDialog d1 = new TextInputDialog(nomeJ1);
        d1.setTitle("Registo de Jogador");
        d1.setHeaderText("Configuração: Jogador 1");
        d1.setContentText("Introduz o teu nome (Peão Azul):");
        d1.showAndWait().ifPresent(n -> nomeJ1 = n.trim().isEmpty() ? "Jogador 1" : n);

        if (!modoSoP1) {
            TextInputDialog d2 = new TextInputDialog("André");
            d2.setTitle("Registo de Jogador");
            d2.setHeaderText("Configuração: Jogador 2");
            d2.setContentText("Introduz o nome do oponente (Peão Laranja):");
            d2.showAndWait().ifPresent(n -> nomeJ2 = n.trim().isEmpty() ? "Jogador 2" : n);
        } else {
            nomeJ2 = modoRede ? "Adversário" : "Bot";
        }
    }

    private void atualizarTextosComNomes() {
        lblTituloPainel.setText(nomeJ1.toUpperCase());
        lblEstadoDetalhado.setText(nomeJ1 + " (Azul): Casa 1  |  " + nomeJ2 + " (Laranja): Casa 1");
    }

    private void confirmarEExecutar(String titulo, String cabecalho, Runnable acaoConfirmada) {
        if (motorJogo.isJogoTerminado() || (jogador1.getPosicao() == 1 && jogador2.getPosicao() == 1)) {
            acaoConfirmada.run();
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText("O progresso atual será perdido. Tens a certeza que queres continuar?");

        ButtonType btnSim = new ButtonType("Sim, reiniciar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNao = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alerta.getButtonTypes().setAll(btnSim, btnNao);

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == btnSim) {
            acaoConfirmada.run();
        }
    }

    private void animarDadoEExecutar(Runnable acaoFinal) {
        tocarSom(somDado, 0.6); // O som dispara exatamente quando a animação começa!

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
        if (motorJogo.isJogoTerminado()) return;

        int indexAtual = motorJogo.getJogadorAtualIndex();
        boolean euJoguei = (!modoRede) ? (indexAtual == 0) : ((souHost && indexAtual == 0) || (!souHost && indexAtual == 1));

        String nomeResponsavel = indexAtual == 0 ? nomeJ1 : nomeJ2;
        String prefixoTexto = nomeResponsavel + " tirou";

        lblDadoIcon.setText(FACES_DADO[valorDado]);

        int posAntiga = motorJogo.getJogadores().get(indexAtual).getPosicao();
        motorJogo.jogarTurno(valorDado);
        int posNova = motorJogo.getJogadores().get(indexAtual).getPosicao();

        lblDadoResultado.setText(prefixoTexto + " um " + valorDado + "!");

        String corLog = indexAtual == 0 ? "(Azul)" : "(Laranja)";
        String logLinha = nomeResponsavel + " " + corLog + " tirou " + valorDado + ".\n➔ Casa " + posAntiga + " para " + posNova;

        if (posNova > posAntiga + valorDado && posNova != 100) {
            logLinha += " \uD83E\uDE9C (Subiu uma Escada!)";
        } else if (posNova < posAntiga) {
            logLinha += " \uD83D\uDC0D (Desceu uma Cobra!)";
        }

        if (txtHistorico != null) {
            txtHistorico.appendText(logLinha + "\n\n");
        }

        if (motorJogo.isJogoTerminado()) return;

        int seguinteIndex = motorJogo.getJogadorAtualIndex();
        Jogador seguinteJogador = motorJogo.getJogadores().get(seguinteIndex);
        String nomeSeguinte = seguinteIndex == 0 ? nomeJ1 : nomeJ2;

        if (!modoRede && modoBot && seguinteIndex == 1) {
            lblTituloPainel.setText(nomeSeguinte.toUpperCase());
            lblTituloPainel.setTextFill(jogador2.getCor());
            lblTurnoStatus.setText("A PENSAR...");
            lblTurnoStatus.setTextFill(jogador2.getCor());
            btnLancarDado.setDisable(true);

            PauseTransition atrasoIA = new PauseTransition(Duration.seconds(1.5));
            atrasoIA.setOnFinished(evt -> {
                tocarSom(somDado, 0.6);
                animarDadoEExecutar(() -> {
                    int valorSorteadoBot = motorJogo.getDado().rolar();
                    lblDadoIcon.setText(FACES_DADO[valorSorteadoBot]);
                    processarJogadaSincronizada(valorSorteadoBot);
                });
            });
            atrasoIA.play();
        } else {
            boolean eMinhaVez = (!modoRede) ? true : ((souHost && seguinteIndex == 0) || (!souHost && seguinteIndex == 1));

            lblTituloPainel.setText(nomeSeguinte.toUpperCase());
            lblTituloPainel.setTextFill(seguinteJogador.getCor());
            lblTurnoStatus.setText(eMinhaVez ? "A TUA VEZ DE JOGAR" : "A AGUARDAR JOGADA...");
            lblTurnoStatus.setTextFill(seguinteJogador.getCor());

            btnLancarDado.setDisable(!eMinhaVez);
            btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand; " +
                    (seguinteIndex == 0 ? "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);" : "-fx-background-color: linear-gradient(to right, #f97316, #ea580c);"));
        }
    }

    private void reiniciarJogo(boolean ativarBot, GraphicsContext gc, Canvas canvas) {
        this.modoBot = ativarBot;
        if(modoBot) this.modoRede = false;

        motorJogo = new Jogo();
        jogador1.posicaoProperty().set(1);
        jogador2.posicaoProperty().set(1);

        motorJogo.adicionarJogador(jogador1);
        motorJogo.adicionarJogador(jogador2);
        configurarObstaculosFixos();
        motorJogo.iniciar();

        if (lblVitoriaGigante != null) {
            if (animacaoVitoriaAtiva != null) {
                animacaoVitoriaAtiva.stop();
            }
            areaJogo.getChildren().remove(lblVitoriaGigante);
            lblVitoriaGigante = null;
        }

        lblDadoIcon.setText("🎲");
        lblTituloPainel.setText(nomeJ1.toUpperCase());
        lblTituloPainel.setTextFill(jogador1.getCor());
        lblTurnoStatus.setText("A TUA VEZ DE JOGAR");
        lblTurnoStatus.setTextFill(jogador1.getCor());

        lblEstadoDetalhado.setText(nomeJ1 + " (Azul): Casa 1  |  " + nomeJ2 + " (Laranja): Casa 1");
        lblDadoResultado.setText("[ Jogo Reiniciado ]");

        if (txtHistorico != null) {
            txtHistorico.setText("=== JOGO REINICIADO ===\nBoa sorte, " + nomeJ1 + " e " + nomeJ2 + "!\n\n");
        }

        btnLancarDado.setDisable(false);
        btnLancarDado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        desenharTabuleiro(gc);
        desenharObstaculosVisuais(gc);

        criarAnimacaoPosicao(pecaJogador1, 1, 1, -4, false);
        criarAnimacaoPosicao(pecaJogador2, 1, 1, 4, false);
    }

    private RadialGradient criarGradientePeca(Color corBase) {
        return new RadialGradient(0, 0, 0.35, 0.35, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE), new Stop(0.2, corBase.brighter()),
                new Stop(0.8, corBase.darker()), new Stop(1, Color.web("#0a1a3a")));
    }

    private void atualizarBarraEstado() {
        int posJ1 = jogador1.getPosicao() > 100 ? 100 : jogador1.getPosicao();
        int posJ2 = jogador2.getPosicao() > 100 ? 100 : jogador2.getPosicao();
        lblEstadoDetalhado.setText(nomeJ1 + " (Azul): Casa " + posJ1 + "  |  " + nomeJ2 + " (Laranja): Casa " + posJ2);
    }

    private void verificarCondicaoVitoria(int idJogador, int localizacao) {
        if (motorJogo.isJogoTerminado() || localizacao >= 100) {
            String mensagemFinal;
            Color corVitoria;

            if (idJogador == 1) {
                mensagemFinal = "VITÓRIA DE\n" + nomeJ1.toUpperCase() + "!";
                corVitoria = Color.web("#10b981");
                tocarSom(somVitoria, 5.0); // Dispara som de celebração
            } else if (idJogador == 99) {
                mensagemFinal = "BOT VENCEU!";
                corVitoria = Color.CRIMSON;
            } else {
                mensagemFinal = nomeJ2.toUpperCase() + "\nGANHOU!";
                corVitoria = Color.CRIMSON;
            }

            lblTurnoStatus.setTextFill(corVitoria);
            lblTurnoStatus.setText("PARTIDA TERMINADA");
            lblTituloPainel.setText("FIM DE JOGO");
            btnLancarDado.setDisable(true);

            if(lblVitoriaGigante == null) {
                lblVitoriaGigante = new Label(mensagemFinal);
                lblVitoriaGigante.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 65));
                lblVitoriaGigante.setTextFill(corVitoria);
                lblVitoriaGigante.setAlignment(Pos.CENTER);
                lblVitoriaGigante.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                DropShadow sombra = new DropShadow();
                sombra.setRadius(15); sombra.setOffsetX(5); sombra.setOffsetY(5);
                sombra.setColor(Color.color(0, 0, 0, 0.7));
                lblVitoriaGigante.setEffect(sombra);
                areaJogo.getChildren().add(lblVitoriaGigante);
            }

            lblVitoriaGigante.setScaleX(0.5);
            lblVitoriaGigante.setScaleY(0.5);
            lblVitoriaGigante.setOpacity(0.0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), lblVitoriaGigante);
            fadeIn.setToValue(1.0);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400), lblVitoriaGigante);
            scaleUp.setToX(1.4); scaleUp.setToY(1.4);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), lblVitoriaGigante);
            scaleDown.setToX(1.0); scaleDown.setToY(1.0);
            scaleDown.setInterpolator(Interpolator.EASE_IN);

            RotateTransition wobble = new RotateTransition(Duration.millis(100), lblVitoriaGigante);
            wobble.setByAngle(10);
            wobble.setCycleCount(6);
            wobble.setAutoReverse(true);

            ScaleTransition pulsar = new ScaleTransition(Duration.millis(1000), lblVitoriaGigante);
            pulsar.setToX(1.08); pulsar.setToY(1.08);
            pulsar.setCycleCount(Animation.INDEFINITE);
            pulsar.setAutoReverse(true);

            animacaoVitoriaAtiva = new SequentialTransition(
                    new ParallelTransition(fadeIn, scaleUp),
                    scaleDown,
                    wobble,
                    pulsar
            );

            animacaoVitoriaAtiva.play();
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
            return null; // Nenhuma animação pendente
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
            // Se for uma Cobra ou Escada -> Desliza elegantemente
            double[] posDestino = getCentroCasa(novaPos);

            // NOVO: Descobre se está a subir ou a descer ANTES de criar o evento
            boolean aSubir = novaPos > posAntiga;

            // Adiciona um evento no instante ZERO da animação para tocar o som certo
            timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, evt -> {
                if (aSubir) {
                    tocarSom(somEscada, 1.5); // A peça está a subir, toca escada
                } else {
                    tocarSom(somCobra, 1.5);  // A peça está a descer, toca cobra
                }
            }));

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