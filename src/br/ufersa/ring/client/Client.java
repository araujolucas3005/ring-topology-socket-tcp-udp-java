package br.ufersa.ring.client;

import br.ufersa.ring.utils.NumberUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {

  private final int id;
  private volatile int nextClientId;
  private final DatagramSocket socket;
  private final Socket serverSocket;
  private volatile boolean isFirst;
  private volatile boolean connection;

  public Client(
      int id, int nextClientId, boolean isFirst, DatagramSocket socket, Socket serverSocket) {
    this.id = id;
    this.nextClientId = nextClientId;
    this.socket = socket;
    this.serverSocket = serverSocket;
    this.isFirst = isFirst;
    this.connection = true;
  }

  // thread para receber as mensagens do servidor
  // serve para receber os ids com os quais o cliente deverá se conectar
  // durante a sua conexão
  public class ServerReceiver implements Runnable {
    @Override
    public void run() {
      try {
        Scanner scanner = new Scanner(serverSocket.getInputStream());
        int nextId;

        // enquanto houver conexão com o servidor
        while (true) {
          try {

            // pega o id que o servidor irá enviar
            nextId = scanner.nextInt();

            // se for 0, este nó deverá se tornar o primeiro
            if (nextId == 0) {
              isFirst = true;
            } else {
              nextClientId = nextId;
            }
          } catch (NoSuchElementException e) {
            System.out.println(
                "Conexão com o servidor perdida. Sua mensagem pode não mais chegar a um destinatário.");
            break;
          }
        }
      } catch (IOException e) {
        System.out.println("Não foi possível se conectar com o servidor no momento...");

        connection = false;
      }
    }
  }

  // thread para receber as mensagens do cliente com o qual este está ligado
  public class Receiver implements Runnable {
    @Override
    public void run() {

      while (connection) {
        // cria um buffer para receber a mensagem do nó anterior
        byte[] bufferRecebimento = new byte[1024];

        // cria um datagrama para receber a mensagem
        DatagramPacket datagramaRecebimento =
            new DatagramPacket(bufferRecebimento, bufferRecebimento.length);

        // recebe a mensagem
        try {
          socket.receive(datagramaRecebimento);
        } catch (IOException e) {
          System.out.println("Falha ao receber uma mensagem...");
          continue;
        }

        // pega a mensagem
        bufferRecebimento = datagramaRecebimento.getData();

        // remove o lixo da string
        String msgReceived = new String(bufferRecebimento).trim();

        // mensagem que será enviada para o próximo nó
        // será a mensagem recebida + id

        System.out.printf("Mensagem recebida < %s\n", msgReceived);

        // evita o loop infinito
        if (!isFirst) {
          System.out.println(
              "Incrementando a mensagem com o o meu id e enviando para o próximo cliente...");
          System.out.println("Mensagem enviada para o cliente de id " + nextClientId);

          String newMessage = String.valueOf(Integer.parseInt(msgReceived) + id);

          try {
            InetAddress receiverAddress = InetAddress.getByName("localhost");

            // cria um datagram para enviar a nova mensagem com o id incrementado
            DatagramPacket sendDatagram =
                new DatagramPacket(
                    newMessage.getBytes(),
                    newMessage.getBytes().length,
                    receiverAddress,
                    5050 + nextClientId);

            socket.send(sendDatagram);
          } catch (UnknownHostException e) {
            System.out.println("Host destinatário inexistente...");
          } catch (IOException e) {
            System.out.println("Falha ao enviar a mensagem...");
          }
        }
      }
    }
  }

  public class Sender implements Runnable {
    @Override
    public void run() {

      try {
        InetAddress receiverAddress = InetAddress.getByName("localhost");
        Scanner sc = new Scanner(System.in);
        String msg;

        while (connection) {
          msg = sc.nextLine();

          // apenas números
          while (!NumberUtils.isDigit(msg)) {
            System.out.println("Apenas números são aceitos como mensagem!");
            msg = sc.nextLine();
          }

          // cria um buffer de envio a partir da mensagem
          byte[] sendBuffer = msg.getBytes();

          // cria um datagrama contendo a mensagem
          DatagramPacket sendDatagram =
              new DatagramPacket(
                  sendBuffer, sendBuffer.length, receiverAddress, 5050 + nextClientId);

          // printa na tela que está enviando a mensagem
          System.out.printf(
              "Enviando a mensagem para o cliente de id %d > %s\n", nextClientId, msg);

          // envia a mensagem
          socket.send(sendDatagram);
        }
      } catch (IOException e) {
        socket.close();

        connection = false;
      }
    }
  }

  // incia todoas as threads da classe
  public void startThreads() {
    Thread threadReceiver = new Thread(new Receiver());
    Thread threadSender = new Thread(new Sender());
    Thread threadReceiverFromServer = new Thread(new ServerReceiver());

    threadReceiver.start();
    threadSender.start();
    threadReceiverFromServer.start();
  }
}
