import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UDPClient {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    public static void main(String[] args) {
        if (args.length != 5) {
            for (String arg : args)
                System.out.println(arg);
            System.out.println("FTP Client Usage: ftp_client server-host-name server-port# file-name N MSS");
            return;
        }

        UDPClient client = new UDPClient(args[0], Integer.parseInt(args[1]));

        int mss = Integer.parseInt(args[4]);
        client.sendMssValue(mss);
        long startTime = System.currentTimeMillis();
        client.sendFile(args[2], Integer.parseInt(args[3]), mss);
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken = " + (endTime-startTime)/1000);
        client.socket.disconnect();
        client.socket.close();
    }

    private byte[] readFileFromIndex(byte[] file, int index, int size) {
        int j;
        byte array[] = new byte[size];
        for (j = 0; j < size; j++) {
            if (index >= file.length) {
                if(j==0)
                    return null;
                byte[] smaller = new byte[j];
                System.arraycopy(array, 0, smaller, 0, j);
                return smaller;
            }
            array[j] = file[index++];
        }
        return array;
    }

    private void sendFile(String fileName, int N, int mss) {
        try {
            File file = new File(fileName);
            byte[] fileInBytes = new byte[(int) file.length()];

            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(fin);
            bin.read(fileInBytes, 0, fileInBytes.length);

            byte[] fileSize = intToBytes(fileInBytes.length);
            socket.send(new DatagramPacket(fileSize, fileSize.length, serverAddress, serverPort));

            Window window = new Window(N);

            int fileIndex = 0;
            int seqNo = 0;
            byte[] array;
            for (int i = 0; i < window.size; i++) {
                array = readFileFromIndex(fileInBytes, fileIndex, mss);
                if(array != null) {
                    Packet packet = new Packet(array, seqNo++);
                    window.packetList.add(packet);
                    sendPacket(packet);
                    fileIndex += array.length;
                }
            }

            byte[] ack = new byte[4];
            DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, serverAddress, serverPort);

            byte[] data;
            while (!window.packetList.isEmpty()) {
                try {
                    socket.receive(ackPacket);

                    int ackedSeqNo = bytesToInt(ackPacket.getData());
                    if(window.packetList.get(0).seqNo == ackedSeqNo) {
                        window.packetList.remove(0);
                        data = readFileFromIndex(fileInBytes, fileIndex, mss);
                        if(data != null) {
                            fileIndex += data.length;
                            Packet packet = new Packet(data, seqNo++);
                            window.packetList.add(packet);
                            sendPacket(packet);
                        }
                    }
                }
                catch(SocketTimeoutException e) {
                    System.out.println("Timeout, sequence number = " + window.packetList.get(0).seqNo);
                    for(Packet p: window.packetList)
                        sendPacket(p);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(Packet packet) throws IOException {
        byte[] buf = packet.dataWithSeqNo();
        DatagramPacket dgram = new DatagramPacket(buf, buf.length, serverAddress, serverPort);
        socket.send(dgram);
        socket.setSoTimeout(1000);
    }

    private void sendMssValue(int mss) {
        try {
            byte[] mssBuf = intToBytes(mss);
            DatagramPacket packet = new DatagramPacket(mssBuf, mssBuf.length, serverAddress, serverPort);
            socket.send(packet);
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
