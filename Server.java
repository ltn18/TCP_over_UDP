
/**
 * @author: Lam Nguyen
 * @id: ltn18
 * @course: Computer Networks
 */

import java.net.*;

public class Server {

  // port number
  private final int PORT = 5000;

  // maximum sequence number
  private final int MAX_SEQ_NUM = 30720;

  // transmission time out
  private final int TIMEOUT = 500;

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

  // services
  private Services services;

  /**
   * Server constructor for initializing server
   * 
   * @param _seq_num initial sequence number
   * @throws Exception
   */
  public Server(int _seq_num) throws Exception {
    seq_num = _seq_num;
    socket = new DatagramSocket();
  }

  /**
   * Running the Server
   * 
   * @throws Exception
   */
  private void run() throws Exception {
    socket = new DatagramSocket(PORT);
    Services services = new Services();

    // setting initial sequence number
    int seq_num = services.getRandomNumber(20000, 25000);

    // bytes storing data to send to client
    byte[] sendData;

    // bytes storing receive data from client
    byte[] receiveData = new byte[256];

    while (true) {
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

      // receive packet from the server
      socket.receive(receivePacket);
      System.out.println("Receiving packet " + seq_num);

      // extract message from packet
      String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

      // get the packet address
      InetAddress IPAddress = receivePacket.getAddress();

      // get the packet port
      int port = receivePacket.getPort();

      // log format for printing out states of server
      String log = "Sending packet " + seq_num;

      if (message.equals("SYN") && !SYN && !ACK) {
        SYN = true;
        ACK = true;
        log += " " + services.messageType(ACK, SYN, FIN);
        sendData = "SYNACK".getBytes();
      } else if (message.equals("ACK") && !FIN) {
        FIN = true;
        log += " " + services.messageType(ACK, SYN, FIN);
        sendData = "FIN".getBytes();
      } else
        // send back to client the uppercase version of their message
        sendData = message.toUpperCase().getBytes();

      // message from client
      String messageFromClient = new String(receivePacket.getData(), 0, receivePacket.getLength());

      // send packet to client
      System.out.println(log);
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      socket.send(sendPacket);

      // retransmit if no response from the client
      if (messageFromClient.length() == 0 || messageFromClient.equals(null) || messageFromClient.equals("")) {
        socket.setSoTimeout(TIMEOUT);
        int tries = 0;
        while ((messageFromClient.length() == 0 || messageFromClient.equals(null) || messageFromClient.equals(""))
            && tries < 5) {
          System.out.println("Sending packet " + seq_num + " " + "Retransmission");
          socket.send(sendPacket);
          socket.receive(receivePacket);
          messageFromClient = new String(receivePacket.getData(), 0, receivePacket.getLength());
          tries++;
        }
      }

      seq_num++;
      // set sequence number to 0 if reaches maximum
      if (seq_num >= MAX_SEQ_NUM)
        seq_num = 0;

      // if after retransmission the packet is not received, then it will be dropped
      if (messageFromClient.length() == 0 || messageFromClient.equals(null) || messageFromClient.equals(""))
        System.out.println("Packet dropped!");

      System.out.println("Packet Data From Client: " + messageFromClient);
    }
  }

  // closing the server
  private void close() {
    socket.close();
  }

  public static void main(String args[]) throws Exception {
    Services services = new Services();
    Server server = new Server(services.getRandomNumber(10000, 15000));
    server.run();
    server.close();
  }
}
