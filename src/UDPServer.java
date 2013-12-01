import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.Math.random;

public class UDPServer {

    public static void main(String[] args) {

        if(args.length != 3) {
            System.out.println("UDPServer Usage: UDPServer port# file-name p");
            return;
        }

        try {
            InetAddress clientAddress;
            int clientPort;

            DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[0]));
            byte[] receiveData = new byte[Integer.SIZE];

            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet);

            clientAddress = packet.getAddress();
            clientPort = packet.getPort();

            int mss = bytesToInt(receiveData);

            socket.receive(packet);
            int fileSize = bytesToInt(receiveData);

            int seqNo = 0;

            byte[] bytearray = new byte[mss + Integer.SIZE];
            DatagramPacket fileBytes = new DatagramPacket(bytearray, bytearray.length);
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(args[1])));

            double failureProbability = Double.parseDouble(args[2]);

            int count = 0;
            while(count < fileSize) {
                socket.receive(fileBytes);
                byte[] data = getProperSizedBuffer(fileBytes);
                Packet receivedPacket = Packet.extractPacket(data);
                if(random() > failureProbability) {
                    if(receivedPacket.seqNo == seqNo) {
                        byte[] seqBytes = intToBytes(seqNo);
                        socket.send(new DatagramPacket(seqBytes, seqBytes.length, clientAddress, clientPort));
                        printWriter.print(new String(receivedPacket.data));
                        seqNo++;
                        count += receivedPacket.data.length;
                        fileBytes = new DatagramPacket(bytearray, bytearray.length);
                    }
                    else if(receivedPacket.seqNo < seqNo) {
                        byte[] seqBytes = intToBytes(receivedPacket.seqNo);
                        socket.send(new DatagramPacket(seqBytes, seqBytes.length, clientAddress, clientPort));
                    }
                }
                else {
                    System.out.println("Packet loss, sequence number = " + receivedPacket.seqNo);
                }
            }
            printWriter.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static byte[] getProperSizedBuffer(DatagramPacket fileBytes) {
        if(fileBytes.getLength() != fileBytes.getData().length){
            byte[] bytes = new byte[fileBytes.getLength()];
            System.arraycopy(fileBytes.getData(), 0, bytes, 0, fileBytes.getLength());
            return bytes;
        }
        return fileBytes.getData();
    }

    private static byte[] intToBytes(int n) {
        return (ByteBuffer.allocate(Integer.SIZE).order(ByteOrder.BIG_ENDIAN).putInt(n)).array();
    }

    private static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }

}
