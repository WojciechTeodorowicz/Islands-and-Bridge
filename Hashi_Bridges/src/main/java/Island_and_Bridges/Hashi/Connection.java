package Island_and_Bridges.Hashi;

/**
 * Created by Wojciech on 19/03/2018.
 */

import android.util.Log;

import org.w3c.dom.Node;

public class Connection {
    public BoardElement source;
    public BoardElement destination;
    public int second;
public Connection() {
    source = null;
    second = 0;
}
public Connection(BoardElement elt, int val) {
    source = elt;
    second = val;
}
public Connection clone() {
    Connection c = new Connection();
    c.source = source;
    c.second = second;
    return c;
}
}; // Connection}
