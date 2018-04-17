package Island_and_Bridges.Hashi;

import android.util.Log;

//this class holds information about the grid
public class BoardElement {
  // *********************************************************
  // *********************************************************
  // General members
  // *********************************************************
  // *********************************************************

  public int row = 0;
  public int col = 0;

  // Island?
  public boolean is_island = false;

//Island Memebers
  public int max_connecting_bridges = 0;
  public int column = 0;

  // It is easier to refer to neighbours via directions.
  public enum Direction {
    EAST, SOUTH, WEST, NORTH;
  }

  // Pairs of a BoardElement and the number of connecting bridges
  public Connection connecting_north = null;
  public Connection connecting_south = null;
  public Connection connecting_east = null;
  public Connection connecting_west = null;

  public BoardElement clone() {
    BoardElement elt = new BoardElement();
    elt.row = row;
    elt.col = col;
    Log.i("BoardElemnet", "Cloning " + "row " + elt.row + " " + "column " + elt.col);

    elt.max_connecting_bridges = max_connecting_bridges;
    elt.is_island = is_island;
    if (connecting_east != null)
      elt.connecting_east = new Connection();
    else
      elt.connecting_east = null;

    if (connecting_north!= null)
      elt.connecting_north = new Connection();
    else
      elt.connecting_north = null;

    if (connecting_south!= null)
      elt.connecting_south = new Connection();
    else
      elt.connecting_south = null;

    if (connecting_west != null)
      elt.connecting_west = new Connection();
    else
      elt.connecting_west = null;

    return elt;
  }

  private int GetConnectionCount(Connection connection){
    if (connection == null) {
      return 0;
    } else {
      return 1;
    }
  }

  // Return the current count of connections connected
  // to this island.
  public int GetCurrentCount() {
    if (!is_island) {
      return 0;
    }
    int s = GetConnectionCount(connecting_east);
    s += GetConnectionCount(connecting_south);
    s += GetConnectionCount(connecting_north);
    s += GetConnectionCount(connecting_west);
    return s;
  }

  void AddConnection(Direction dir, BoardElement dest, int value) {
    Connection connection = null;
    switch (dir) {
      case EAST:
        connecting_east = new Connection();
        connection = connecting_east;
        break;
      case WEST:
        connecting_west = new Connection();
        connection = connecting_west;
        break;
      case SOUTH:
        connecting_south = new Connection();
        connection = connecting_south;
        break;
      case NORTH:
        connecting_north = new Connection();
        connection = connecting_north;
        break;
    }
  }
}; // BoardElement
