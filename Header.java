public class Header {
  private String seq_num;
  private String ack_num;
  private boolean ACK;
  private boolean SYN;
  private boolean FIN;

  private String convertTo16Bits(int num) {
    StringBuilder sb = new StringBuilder(Integer.toBinaryString(num));
    StringBuilder zero = new StringBuilder();
    for (int i = 0; i < 16 - sb.length(); i++) zero.append(0);
    zero.append(sb);
    return zero.toString();
  }

  public Header(int _seq_num, int _ack_num) {
    this.seq_num = convertTo16Bits(_seq_num);
    this.ack_num = convertTo16Bits(_ack_num);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    // top of header
    sb.append("  0                   1                     2                   3    \n");
    sb.append("| 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 | 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 |\n");
    sb.append("---------------------------------------------------------------------\n");
    
    // sequence number
    sb.append("| ");
    for (int i = 0; i < seq_num.length(); i++) sb.append(seq_num.charAt(i) + " ");
    sb.append("| ");

    // acknowledgement number
    for (int i = 0; i < ack_num.length()-1; i++) sb.append(ack_num.charAt(i) + " ");
    sb.append(ack_num.charAt(ack_num.length()-1));
    sb.append(" |");

    // window
    sb.append("\n");
    sb.append("---------------------------------------------------------------------\n");
    sb.append("|         W i n d o w             | ");

    // not used section
    for (int i = 0; i < 13; i++) sb.append("0 ");

    // A S F
    sb.append("A ");
    sb.append("S ");
    sb.append("F ");
    sb.append("|");

    sb.append("\n");
    sb.append("---------------------------------------------------------------------\n");

    return sb.toString();
  }

  public static void main(String[] args) {
    Header header = new Header(2905, 28796);
    System.out.println(header.toString());
  }
}