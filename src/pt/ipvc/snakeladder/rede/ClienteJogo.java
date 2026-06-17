/**
 * Estabelece a ligação a um jogo remoto através do IP do Host.
 * Atua como Cliente, utilizando Sockets TCP e Threads para receber
 * e sincronizar as jogadas com o motor do jogo local.
 *
 * @author André e Eduardo
 * @version 1.0
 */
package pt.ipvc.snakeladder.rede;

import java.io.*;
import java.net.*;

public class ClienteJogo {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public interface MensagemListener {
        void aoReceberJogada(int valorDado);
    }
    private MensagemListener listener;

    public ClienteJogo(MensagemListener listener) {
        this.listener = listener;
    }

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