import java.util.ArrayList;
import java.util.List;

public class Window {
    List<Packet> window;
    int size;

    public Window(int N) {
        this.window = new ArrayList<Packet>();
        this.size = N;
    }
}
