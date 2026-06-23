package pt.ipvc.snakeladder.rede;

import java.io.*;
import java.net.*;

/**
 * Estabelece a ligação a um jogo remoto através do IP do Host.
 * Atua como Cliente, utilizando Sockets TCP e Threads para receber
 * e sincronizar as jogadas com o motor do jogo local.
 *
 * @author André e Eduardo
 * @version 1.2
 */
public class ClienteJogo {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    /**
     * Interface funcional responsável por escutar e tratar as mensagens (jogadas) recebidas da rede.
     */
    public interface MensagemListener {
        /**
         * Invocado sempre que o adversário remoto (host) envia uma nova jogada.
         *
         * @param valorDado O valor do dado tirado pelo adversário remoto no seu turno.
         */
        void aoReceberJogada(int valorDado);
    }

    private MensagemListener listener;

    /**
     * Construtor do ClienteJogo.
     *
     * @param listener A classe ou função que irá escutar as jogadas recebidas do Servidor.
     */
    public ClienteJogo(MensagemListener listener) {
        this.listener = listener;
    }

    /**
     * Estabelece a ligação a um servidor remoto utilizando Sockets num processo em segundo plano (Thread).
     * Inclui um limite de tempo (timeout) para evitar bloqueios na interface gráfica caso o IP não exista.
     *
     * @param ip O endereço IP da máquina host.
     * @param porta A porta TCP onde o servidor remoto se encontra à escuta.
     * @param aoSucesso Função (Callback) executada se a ligação for estabelecida com sucesso.
     * @param aoFalhar Função (Callback) executada se a ligação falhar (ex: IP inválido ou Host não encontrado).
     */
    public void conectar(String ip, int porta, Runnable aoSucesso, Runnable aoFalhar) {
        new Thread(() -> {
            try {
                socket = new Socket();
                // Tenta ligar durante um máximo de 3 segundos para não congelar o jogo
                socket.connect(new InetSocketAddress(ip, porta), 3000);
                System.out.println("Conectado ao servidor no IP: " + ip);

                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());

                // Se chegou aqui sem dar erro, avisa a interface gráfica que correu bem!
                if (aoSucesso != null) aoSucesso.run();

                ouvirServidor();
            } catch (IOException e) {
                System.out.println("Erro ao conectar ao servidor.");
                // Se deu erro, avisa a interface gráfica para mostrar o popup
                if (aoFalhar != null) aoFalhar.run();
            }
        }).start();
    }

    /**
     * Escuta continuamente as mensagens enviadas pelo servidor (Host).
     */
    private void ouvirServidor() {
        try {
            while (true) {
                int valorDadoRecebido = in.readInt();
                if (listener != null) listener.aoReceberJogada(valorDadoRecebido);
            }
        } catch (IOException e) {
            System.out.println("O Servidor foi desligado.");
        }
    }

    /**
     * Envia o resultado da jogada local deste cliente para o adversário através do Socket estabelecido.
     *
     * @param valorDado O valor do dado que o jogador deste cliente acabou de rolar.
     */
    public void enviarJogada(int valorDado) {
        try {
            if (out != null) {
                out.writeInt(valorDado);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}