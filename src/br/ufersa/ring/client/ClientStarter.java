package br.ufersa.ring.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Scanner;

public class ClientStarter {

  public static void main(String[] args) throws IOException {

    // cria um socket para se conectar com o servidor
    Socket socket = new Socket("127.0.0.1", 5050);

    // flag para saber se esse nó será o primeiro
    boolean isFirst = false;

    // pega o id atual provido pelo servidor no formato "id@proximoId"
    Scanner s = new Scanner(socket.getInputStream());
    String[] msg = s.nextLine().split("@", 2);

    // id que será dado ao cliente
    int id = Integer.parseInt(msg[0]);

    // id do cliente que ele deve se conectar
    int nextId = Integer.parseInt(msg[1]);

    // se o id do próximo cliente for 0, quer dizer que
    // esse cliente é o primeiro
    if (nextId == 0) {
      isFirst = true;
      nextId = id;
    }

    System.out.println("MEU ID: " + id);

    /*
     *  porta cliente 1: 5051
     *  porta cliente 2: 5052
     *        ...
     *  porta client n: 5050 + n
     */
    DatagramSocket clientSocket = new DatagramSocket(5050 + id);

    // cria o objeto cliente
    // se for o primeiro cliente, o próximo id
    Client client = new Client(id, nextId, isFirst, clientSocket, socket);

    // incia as threads
    client.startThreads();
  }
}
