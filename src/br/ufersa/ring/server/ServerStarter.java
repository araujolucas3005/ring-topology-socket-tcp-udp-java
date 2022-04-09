package br.ufersa.ring.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NavigableMap;
import java.util.TreeMap;

/*
* O servidor gerenciará os clientes conectados
* Irá fazer com que o anel não se quebre quando clientes forem desconectados
 */

public class ServerStarter {

  // para armazenar os clientes ativos
  // chave = id do cliente
  // valor = socket do cliente
  // NavigableMap para poder navegar entre as chaves
  // com os métodos firstKey(), lastKey(), higherKey() e lowerKey()
  // TreeMap para poder ordenar as chaves (ids) e saber a posição do cliente no anel
  public static NavigableMap<Integer, Socket> clients = new TreeMap<>();

  // para providenciar os ids para os clientes
  private static int currentClientId = 1;

  public static void main(String[] args) throws IOException {
    // inicia o servidor
    ServerSocket socketServidor = new ServerSocket(5050);

    // equanto o servidor estiver online
    while (true) {
      System.out.println("Aguardando conexão do cliente...");

      // aguarda algum cliente se conectar
      Socket client = socketServidor.accept();

      // printa informações do cliente
      System.out.printf(
          "Cliente %d %s %s conectado\n",
          currentClientId, client.getInetAddress().getHostAddress(), client.getPort());

      // pega o output do cliente que entrou
      PrintStream outputStream = new PrintStream(client.getOutputStream(), true);

      // se não tiverem clientes, envia o id do próximo cliente como 0
      // informando que é o primeiro
      if (clients.isEmpty()) {
        outputStream.println(currentClientId + "@" + 0);
      } else {
        // senão, envia o id do primeiro cliente para fechar o anel
        outputStream.println(currentClientId + "@" + clients.firstKey());

        // envia para o último cliente a sua nova ligação
        Socket prevClient = clients.lastEntry().getValue();
        PrintStream out = new PrintStream(prevClient.getOutputStream());
        out.println(currentClientId);
      }

      // insere o novo cliente na hashtable
      clients.put(currentClientId, client);

      // inicia a thread para o cliente e incrementa o id para o próximo cliente
      Thread threadServer = new Thread(new Server(client, currentClientId++));
      threadServer.start();
    }
  }
}
