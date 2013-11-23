import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UDPClient {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    public static void main(String[] args) {
        if(args.length != 5) {
            for(String arg: args)
                System.out.println(arg);
            System.out.println("FTP Client Usage: ftp_client server-host-name server-port# file-name N MSS");
            return;
        }

        UDPClient client = new UDPClient(args[0], Integer.parseInt(args[1]));

        int mss = Integer.parseInt(args[4]);
        byte[] mssBuf = intToBytes(mss);
        DatagramPacket packet = new DatagramPacket(mssBuf, mssBuf.length, client.serverAddress, client.serverPort);
        try {
            client.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public UDPClient(String serverName, int serverPort) {
        try {
            this.socket = new DatagramSocket();
            this.serverAddress = InetAddress.getByName(serverName);
            this.serverPort = serverPort;

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static byte[] intToBytes(int n) {
        return (ByteBuffer.allocate(Integer.SIZE).order(ByteOrder.BIG_ENDIAN).putInt(n)).array();
    }

    private static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }

}
