package pt.ipvc.snakeladder.rede;

import java.io.*;
import java.net.*;

/**
 * Estabelece a ligação a um jogo remoto através do IP do Host.
 * Atua como Cliente, utilizando Sockets TCP e Threads para receber
 * e sincronizar as jogadas com o motor do jogo local.
 *
 * @author André e Eduardo
 * @version 1.0
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
     *
     * @param ip O endereço IP da máquina host.
     * @param porta A porta TCP onde o servidor remoto se encontra à escuta.
     */
    public void conectar(String ip, int porta) {
        new Thread(() -> {
            try {
                socket = new Socket(ip, porta);
                System.out.println("Conectado ao servidor no IP: " + ip);

                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());

                ouvirServidor();
            } catch (IOException e) {
                System.out.println("Erro ao conectar ao servidor.");
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