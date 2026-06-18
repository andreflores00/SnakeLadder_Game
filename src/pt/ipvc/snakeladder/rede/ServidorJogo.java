/**
 * Gere a criação de uma sala de jogo e atua como Host (Servidor).
 * Utiliza Sockets TCP e Threads secundárias para ouvir e transmitir
 * as jogadas de forma assíncrona, sem congelar a interface gráfica.
 *
 * @author André e Eduardo
 * @version 1.0
 */
package pt.ipvc.snakeladder.rede;

import java.io.*;
import java.net.*;

public class ServidorJogo {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    /**
     * Interface funcional responsável por escutar e tratar as mensagens (jogadas) recebidas da rede.
     */
    public interface MensagemListener {
        /**
         * Invocado sempre que o adversário (cliente) envia uma nova jogada.
         *
         * @param valorDado O valor do dado tirado pelo adversário remoto no seu turno.
         */
        void aoReceberJogada(int valorDado);
    }

    private MensagemListener listener;

    public ServidorJogo(MensagemListener listener) {
        this.listener = listener;
    }

    /**
     * Inicia o servidor na porta especificada e aguarda a ligação de um cliente.
     *
     * @param porta A porta TCP onde o servidor ficará à escuta.
     */
    public void iniciarServidor(int porta) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(porta);
                System.out.println("Servidor aberto na porta " + porta);

                clientSocket = serverSocket.accept();
                System.out.println("Jogador 2 ligou-se!");

                out = new DataOutputStream(clientSocket.getOutputStream());
                in = new DataInputStream(clientSocket.getInputStream());

                ouvirCliente();
            } catch (IOException e) {
                System.out.println("Erro no servidor: " + e.getMessage());
            }
        }).start();
    }

    private void ouvirCliente() {
        try {
            while (true) {
                int valorDadoRecebido = in.readInt();
                if (listener != null) listener.aoReceberJogada(valorDadoRecebido);
            }
        } catch (IOException e) {
            System.out.println("O Jogador 2 desconectou-se.");
        }
    }

    /**
     * Envia o resultado da jogada local do host para o cliente.
     *
     * @param valorDado O valor do dado que o anfitrião do jogo (host) acabou de rolar.
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