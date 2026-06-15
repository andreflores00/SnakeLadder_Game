package pt.ipvc.snakeladder.rede;

import java.io.*;
import java.net.*;

public class ServidorJogo {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    public interface MensagemListener {
        void aoReceberJogada(int valorDado);
    }
    private MensagemListener listener;

    public ServidorJogo(MensagemListener listener) {
        this.listener = listener;
    }

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