import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packet {
    byte[] data;
    int seqNo;

    public Packet(byte[] data, int seqNo) {
        this.data = data;
        this.seqNo = seqNo;
    }

    byte[] dataWithSeqNo() throws IOException {
        byte[] seqNum = intToBytes(seqNo);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(seqNum);
        os.write(data);
        byte[] dataWithSeq = os.toByteArray();
        return dataWithSeq;
    }

    private static byte[] intToBytes(int n) {
        return (ByteBuffer.allocate(Integer.SIZE).order(ByteOrder.BIG_ENDIAN).putInt(n)).array();
    }
}
