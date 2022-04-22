import java.net.*;

public class Server {
  private final int PORT = 5000;
  private DatagramSocket socket;
  private int seq_num;
  private String address;
  private int ack_num = 0;
  private byte[] buf;
  private boolean handshaked = false;
  private boolean SYN = false;
  private boolean ACK = false;
  private boolean FIN = false;
  private Services services;

  public Server(int _seq_num) throws Exception {
    seq_num = _seq_num;
    socket = new DatagramSocket();
  }

  private void run() throws Exception {
    socket = new DatagramSocket(PORT);
    Services services = new Services();
    int seq_num = services.getRandomNumber(20000, 25000);

    byte[] receiveData = new byte[1024];
    byte[] sendData;

    while (true) {
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

      socket.receive(receivePacket);
      System.out.println("Receiving packet " + seq_num);
      String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
      InetAddress IPAddress = receivePacket.getAddress();

      int port = receivePacket.getPort();

      String capitalizedSentence = message.toUpperCase();

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
      } else sendData = capitalizedSentence.getBytes();

      System.out.println(log);
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      socket.send(sendPacket);
      String messageToClient = new String(receivePacket.getData(), 0, receivePacket.getLength());

      if (messageToClient.length() == 0 || messageToClient.equals(null) || messageToClient.equals("")) {
        socket.setSoTimeout(500);
        int tries = 0;
        while ((messageToClient.length() == 0 || messageToClient.equals(null) || messageToClient.equals(""))
            && tries < 5) {
          System.out.println("Sending packet " + seq_num + " " + "Retransmission");
          socket.send(sendPacket);
          socket.receive(receivePacket);
          messageToClient = new String(receivePacket.getData(), 0, receivePacket.getLength());
          tries++;
        }
      }

      seq_num++;

      if ((messageToClient.equals(null) || messageToClient.equals("")))
        System.out.println("Packet dropped!");
    }
  }

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
