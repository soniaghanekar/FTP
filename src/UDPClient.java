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
        client.sendFile(args[2], Integer.parseInt(args[3]), mss);
        client.socket.disconnect();
        client.socket.close();
    }

    private int readFileFromIndex(byte[] file, int index, int size, byte[] array) {
        int j;
        for (j = 0; j < size; j++) {
            if (index == file.length)
                break;
            array[j] = file[index++];
        }
        return j;
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
            byte[] array = new byte[mss];
            for (int i = 0; i < window.size; i++) {
                int count = readFileFromIndex(fileInBytes, fileIndex, mss, array);
                if(count!=0) {
                    fileIndex += count;
                    Packet packet = new Packet(array, seqNo++);
                    window.packetList.add(packet);
                    sendPacket(packet);
                }
            }

            byte[] ack = new byte[4];
            DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, serverAddress, serverPort);

            byte[] data = new byte[mss];
            while (!window.packetList.isEmpty()) {
                try {
                    socket.receive(ackPacket);

                    int ackedSeqNo = bytesToInt(ackPacket.getData());
                    if(window.packetList.get(0).seqNo == ackedSeqNo) {
                        window.packetList.remove(0);
                        int count = readFileFromIndex(fileInBytes, fileIndex, mss, data);
                        if(count != 0) {
                            fileIndex += count;
                            Packet packet = new Packet(data, seqNo++);
                            window.packetList.add(packet);
                            sendPacket(packet);
                        }
                    }
                }
                catch(SocketTimeoutException e) {
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
