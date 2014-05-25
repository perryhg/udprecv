public class UDPClient {
    private static final int PORT = 8888;
    private DatagramSocket dataSocket;
    private DatagramPacket dataPacket;
    private byte[] sendDataByte;
    private String sendStr;

    public UDPClient() {
        Init();
    }

    public void Init() {
        try {
            // 指定端口号，避免与其他应用程序发生冲突

            dataSocket = new DatagramSocket(PORT+1);
            sendDataByte = new byte[1024];
            sendStr = "UDPtestdata中文编码测试";
            sendDataByte = sendStr.getBytes("UTF-8");
            dataPacket = new DatagramPacket(sendDataByte, sendDataByte.length,
                    InetAddress.getByName("localhost"), PORT);
            dataSocket.send(dataPacket);
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new UDPClient();
    }
}