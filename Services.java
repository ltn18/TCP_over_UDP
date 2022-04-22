import java.net.*;
import java.util.regex.*;

public class Services {
  // regex to validate ip address
  private final String IPv4_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
      "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
      "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
      "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

  // pattern to match user's ip input
  private final Pattern IPv4_PATTERN = Pattern.compile(IPv4_REGEX);

  // string format for invalid ip/hostname
  private final String invalidIPAndHostnameError = "IP address or Hostname \"%s\" is not valid";

  public Services() {}

  public int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  public String messageType(boolean ack, boolean syn, boolean fin) {
    if (fin)
      return "FIN";
    if (syn) {
      if (ack)
        return "SYNACK";
      else
        return "SYN";
    }
    return null;
  }

  /**
   * Process hostname and convert it into IP format
   * 
   * @param hostname the hostname to be process
   * @return the ip address associated with the hostname
   */
  public String processHostname(String hostname) {
    if (hostname.equals("")) {
      System.out.println("No IP address or Hostname found!");
      return null;
    }

    // get localhost ip address
    if (hostname.equals("localhost")) {
      try {
        hostname = InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e) {
        System.out.println("Cannot get the IP address of localhost");
      }
    }

    // check if ip is valid or not
    else if (!IPv4_PATTERN.matcher(hostname).matches()) {
      try {
        hostname = InetAddress.getByName(hostname).getHostAddress();
      } catch (UnknownHostException e) {
        System.out.printf(invalidIPAndHostnameError, hostname);
        return null;
      }
    }

    // after converting the user's input to be ip format,
    // check if the final result is an ip or not
    if (!IPv4_PATTERN.matcher(hostname).matches()) {
      System.out.printf(invalidIPAndHostnameError, hostname);
      return null;
    }

    return hostname;
  }

}
