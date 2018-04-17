package Island_and_Bridges.Hashi;

/**
 * Created by Wojciech on 19/03/2018.
 */

import android.util.Log;

import org.w3c.dom.Node;

public class Connection {
    int distance = 0;
    int number = 0;

    Node node;
    public BoardElement destination;
    public int second = 0;

    public void setNode (Node node) {
        this.node = node;
    }

    public Node getNode () {
        return node;
    }

    public int getDistance() {
        return distance;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void increment () {
        if (node != null)
            Log.d("Connections", "updating non existent node");
        setNumber (getNumber() + 1);
    }
}
