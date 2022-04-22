
/**
 * @author: Lam Nguyen
 * @id: ltn18
 * @course: Computer Networks
 */

import java.io.*;
import java.net.*;
import java.util.*;

class Client {

  // port number
  private final int PORT = 5000;

  // maximum sequence number
  private final int MAX_SEQ_NUM = 30720;

  // transmission time out
  private final int TIMEOUT = 500;

  // maximum transmission trials
  private final int MAX_TRIES = 5;

  // UDP socket
  private DatagramSocket socket;

  // sequence number
  private int seq_num;

  // address
  private String address;

  // acknowledgement number
  private int ack_num = 0;

  // buffer for storing bytes of message
  private byte[] buf;

  // syn byte value
  private boolean SYN = false;

  // ack byte value
  private boolean ACK = false;

  // fin byte value
  private boolean FIN = false;

  /**
   * Client constructor for initializing client
   * 
   * @param _seq_num initial sequence number
   * @param _address IP address to connect to
   * @throws Exception
   */
  public Client(int _seq_num, String _address) throws Exception {
    seq_num = _seq_num;
    address = _address;
    socket = new DatagramSocket();
  }

  /**
   * Protocol interface for handshaking
   * 
   * @param address address for socket to send data to
   * @param message message that will be sent (ACK, SYN, FIN)
   * @return response of the server
   * @throws Exception
   */
  private String handshakeMessageProtocol(String address, String message) throws Exception {
    buf = message.getBytes();
    DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(address), PORT);
    packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(address), PORT);

    // send packet
    System.out.println("Sending packet " + seq_num + " " + message);
    socket.send(packet);
    seq_num++;

    // receive packet
    System.out.println("Receiving packet " + seq_num);
    socket.receive(packet);

    String res = new String(packet.getData(), 0, packet.getLength());
    return res;
  }

  /**
   * Handshaking process
   * 
   * @param address IP address for the socket to handshake
   * @throws Exception
   */
  private void handshake(String address) throws Exception {
    if (!SYN) {
      // initial syn protocol
      SYN = true;
      handshakeMessageProtocol(address, "SYN");
    } else {
      if (!ACK) {
        // ack protocol upon receiving synack
        ACK = true;
        handshakeMessageProtocol(address, "ACK");
      } else {
        // fin protocol terminates
        FIN = true;
        handshakeMessageProtocol(address, "FIN");
      }
    }
  }

  /**
   * Running the Client
   * 
   * @throws Exception
   */
  private void run() throws Exception {
    boolean running = true;
    while (running) {

      // get user's input
      System.out.print("Type Message> ");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

      // socket for communicating
      DatagramSocket socket = new DatagramSocket();

      // translate IP address
      InetAddress IPAddress = InetAddress.getByName(address);

      // storage of data to send
      byte[] sendData;

      // storage of data that client receives
      byte[] receiveData = new byte[256];

      // reading message from input
      String message = in.readLine();

      // send packet
      sendData = message.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
      System.out.println("Sending packet " + seq_num);
      socket.send(sendPacket);

      // receive packet
      System.out.println("Receiving packet " + seq_num);
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      socket.receive(receivePacket);

      String messageFromServer = new String(receivePacket.getData(), 0, receivePacket.getLength());

      // retransmit if no response from the server
      if (messageFromServer.length() == 0 || messageFromServer.equals(null) || messageFromServer.equals("")) {
        socket.setSoTimeout(TIMEOUT);
        int tries = 0;
        while ((messageFromServer.length() == 0 || messageFromServer.equals(null) || messageFromServer.equals(""))
            && tries < MAX_TRIES) {
          System.out.println("Sending packet " + seq_num + " " + "Retransmission");
          socket.send(sendPacket);
          socket.receive(receivePacket);
          messageFromServer = new String(receivePacket.getData(), 0, receivePacket.getLength());
          tries++;
        }
      }

      seq_num++;
      // set sequence number to 0 if reaches maximum
      if (seq_num >= MAX_SEQ_NUM)
        seq_num = 0;

      // if after retransmission the packet is not received, then it will be dropped
      if (messageFromServer.length() == 0 || messageFromServer.equals(null) || messageFromServer.equals(""))
        System.out.println("Packet dropped!");

      // terminate message
      if (messageFromServer.equals("quit")) {
        running = false;
      }

      System.out.println("Packet Data From Server: " + messageFromServer);
    }
  }

  // closing the client
  private void close() {
    socket.close();
  }

  public static void main(String args[]) throws Exception {
    Services services = new Services();

    System.out.print("Enter the IP address or Hostname of the server: ");

    // read user's input
    Scanner scanner = new Scanner(System.in);
    String address = scanner.nextLine();

    // get the address
    address = services.processHostname(address);

    // set initial sequence number
    int seq_num = services.getRandomNumber(10000, 15000);

    Client client = new Client(seq_num, address);

    // handshaking protocol
    while (!client.SYN || !client.ACK || !client.FIN)
      client.handshake(address);

    client.run();
    client.close();
  }
}
