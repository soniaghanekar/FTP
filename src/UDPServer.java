import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UDPServer {

    public static void main(String[] args) {
        try {
            InetAddress clientAddress;
            int clientPort;

            DatagramSocket socket = new DatagramSocket(7735);
            byte[] receiveData = new byte[Integer.SIZE];

            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet);

            clientAddress = packet.getAddress();
            clientPort = packet.getPort();

            int mss = bytesToInt(receiveData);
            System.out.println("MSS = " + mss);

            socket.receive(packet);
            int fileSize = bytesToInt(receiveData);
            System.out.println("Filesize = " + fileSize);

            int seqNo =0;

            byte[] bytearray = new byte[mss + Integer.SIZE];
            DatagramPacket fileBytes = new DatagramPacket(bytearray, bytearray.length);
            FileOutputStream fos = new FileOutputStream("../output");
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int count = 0;
            while(count < fileSize) {
                socket.receive(fileBytes);
                Packet receivedPacket = Packet.extractPacket(fileBytes.getData());
                if(receivedPacket.seqNo == seqNo) {
                    byte[] seqBytes = intToBytes(seqNo);
                    socket.send(new DatagramPacket(seqBytes, seqBytes.length, clientAddress, clientPort));
                    bos.write(receivedPacket.data, 0, receivedPacket.data.length);
                    seqNo++;
                    count += receivedPacket.data.length;
                }
            }
            System.out.println("count = "+ count);
            fos.close();
            bos.close();
            socket.close();

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
