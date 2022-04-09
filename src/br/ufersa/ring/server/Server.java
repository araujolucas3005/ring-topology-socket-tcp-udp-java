package br.ufersa.ring.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static br.ufersa.ring.server.ServerStarter.clients;

public class Server implements Runnable {

  private final Socket clientSocket;
  private final int id;

  public Server(Socket clientSocket, int id) {
    this.clientSocket = clientSocket;
    this.id = id;
  }

  @Override
  public void run() {
    try {
      Scanner in = new Scanner(clientSocket.getInputStream());

      // laço para verificar se o cliente ainda está conectado
      while (true) {
        try {
          in.nextLine();
        } catch (NoSuchElementException e) {
          break;
        }
      }
      // cliente se desconectou

      int firstKey = clients.firstKey();

      // se tiver mais de um cliente
      if (clients.size() > 1) {
        // se o cliente desconectado for o primeiro nó
        if (id == firstKey) {
          // pega o segundo nó
          int nextKey = clients.higherKey(id);

          Socket socket = clients.get(nextKey);
          PrintStream os = new PrintStream(socket.getOutputStream());

          // envia para o segundo nó que agora ele deve ser o primeiro
          os.println(0);

          // pega o último cliente
          Socket lastSocket = clients.lastEntry().getValue();
          os = new PrintStream(lastSocket.getOutputStream());

          // envia para o cliente que agora o seu próximo nó é o segundo (que será o primeiro)
          os.println(nextKey);

        } else {
          // se não for o primeiro cliente, pega o anterior
          int prevKey = clients.lowerKey(id);

          Socket prevClientSocket = clients.get(prevKey);
          PrintStream os = new PrintStream(prevClientSocket.getOutputStream());

          // se o nó desconectado for o último, envia o id do primeiro nó para o anterior
          if (id == clients.lastKey()) {
            os.println(firstKey);
          } else {
            // senão, envia o próximo id que o cliente desconectado enviava mensagens
            int nextKey = clients.higherKey(id);
            os.println(nextKey);
          }
        }
      }

      // remove o cliente desconectado da lista
      clients.remove(id);

      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
