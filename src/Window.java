import java.util.ArrayList;
import java.util.List;

public class Window {
    List<Packet> packetList;
    int size;

    public Window(int N) {
        this.packetList = new ArrayList<Packet>();
        this.size = N;
    }
}
