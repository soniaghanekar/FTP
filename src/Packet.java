import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packet {
    byte[] data;
    int seqNo;

    public Packet(byte[] data, int seqNo) {
        this.data = data;
        this.seqNo = seqNo;
    }

    byte[] dataWithSeqNo() {
        int i,j;

        byte[] dataWithSeq = new byte[data.length + 4];
        byte[] seqNum = intToBytes(seqNo);
        dataWithSeq = seqNum;
        for(i=seqNum.length, j=0; i<dataWithSeq.length; i++, j++)
            dataWithSeq[i] = data[j];
        return dataWithSeq;
    }

    private static byte[] intToBytes(int n) {
        return (ByteBuffer.allocate(Integer.SIZE).order(ByteOrder.BIG_ENDIAN).putInt(n)).array();
    }
}
