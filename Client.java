import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.plaf.SliderUI;

class Client {
  private final int PORT = 5000;
  private DatagramSocket socket;
  private int seq_num;
  private String address;
  private int ack_num = 0;
  private byte[] buf;
  private boolean SYN = false;
  private boolean ACK = false;
  private boolean FIN = false;

  public Client(int _seq_num, String _address) throws Exception {
    seq_num = _seq_num;
    address = _address;
    socket = new DatagramSocket();
  }

  private String messageProtocol(String address, String message) throws Exception {
    buf = message.getBytes();
    DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(address), PORT);
    packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(address), PORT);
    System.out.println("Sending packet " + seq_num + " " + message);
    socket.send(packet);
    seq_num++;

    System.out.println("Receiving packet " + seq_num);
    socket.receive(packet);

    String res = new String(packet.getData(), 0, packet.getLength());
    return res;
  }

  private void handshake(String address) throws Exception {
    if (!SYN) {
      SYN = true;
      messageProtocol(address, "SYN");
    } else {
      if (!ACK) {
        ACK = true;
        messageProtocol(address, "ACK");
      } else {
        FIN = true;
        messageProtocol(address, "FIN");
      }
    }
  }

  private void run() throws Exception {
    boolean running = true;
    while (running) {
      System.out.print("Type Message> ");
      BufferedReader in = new BufferedReader(
          new InputStreamReader(System.in));

      DatagramSocket socket = new DatagramSocket();

      InetAddress IPAddress = InetAddress.getByName(address);

      byte[] sendData;
      byte[] receiveData = new byte[256];

      String message = in.readLine();

      sendData = message.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);

      System.out.println("Sending packet " + seq_num);
      socket.send(sendPacket);

      System.out.println("Receiving packet " + seq_num);
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      socket.receive(receivePacket);

      String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());

      socket.setSoTimeout(500);
      int tries = 0;
      while ((modifiedSentence.length() == 0 || modifiedSentence.equals(null) || modifiedSentence.equals(""))
          && tries < 5) {
        System.out.println("Sending packet " + seq_num + " " + "Retransmission");
        socket.send(sendPacket);
        socket.receive(receivePacket);
        modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
        tries++;
      }

      seq_num++;

      if ((modifiedSentence.equals(null) || modifiedSentence.equals("")))
        System.out.println("Packet dropped!");

      if (modifiedSentence.equals("quit")) {
        running = false;
      }

      System.out.println("Packet Data From Server: " + modifiedSentence);
    }
  }

  private void close() {
    socket.close();
  }

  public static void main(String args[]) throws Exception {
    Services services = new Services();

    System.out.print("Enter the IP address or Hostname of the server: ");

    // read user's input
    Scanner scanner = new Scanner(System.in);
    String address = scanner.nextLine();

    address = services.processHostname(address);
    int seq_num = services.getRandomNumber(10000, 15000);

    Client client = new Client(seq_num, address);
    while (!client.SYN || !client.ACK || !client.FIN)
      client.handshake(address);
    client.run();
    client.close();
  }
}
