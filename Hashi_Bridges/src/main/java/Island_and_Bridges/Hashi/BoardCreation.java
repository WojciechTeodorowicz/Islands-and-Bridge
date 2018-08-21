

package Island_and_Bridges.Hashi;

import android.annotation.TargetApi;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import static junit.framework.Assert.*;
//This class Creates the map by random using a 2d array
// the user can choose bettween 3 diffrent levels

public class BoardCreation {
  // This class member is used for random initialization purposes.
  static private final Random random = new Random();
  public int[][] debug_board_state_easy = new int[6][6];
  // The difficulty levels.
  private static final int EASY = 0;
  static public final int MEDIUM = 1;
  static public final int HARD = 2;
  static public final int EMPTY = 0;
  private static int ConnectionFingerprint(BoardElement start, BoardElement end) {
    int x = start.row * 100 + start.col;
    int y = end.row * 100 + end.col;
    // Swap to get always the same fingerprint independent whether we are called
    // start-end or end-start
    if (x > y ) {
      int temp = x;
      x = y;
      y = temp;
    }
    Log.d("", String.format("%d %d" , x ,y));
    return x ^ y;

  }

  public class State {
    // The elements of the board are stored in this array.
    // A value defined by "EMPTY" means that its not set yet.
    public BoardElement [][] board_elements = null;

    public int [][] cell_occupied = null;

    // The width of the board. We only assume squared boards.
    public int board_width=0;


    public State(int width) {
      board_width = width;
      board_elements = new BoardElement[width][width];
      cell_occupied = new int[width][width];
    }

    public State CloneWithoutConnections() {
      State newstate = new State(board_width);
      if (board_elements != null) {
        newstate.board_elements = new BoardElement[board_elements.length][board_elements.length];
        for (int i = 0; i < board_elements.length; ++i) {
          for (int j = 0; j < board_elements.length; ++j) {
            if (board_elements[i][j] == null)
                  continue;
            newstate.board_elements[i][j] = board_elements[i][j].clone();
	  }
	}
      }
      if (cell_occupied != null) {
          assert board_elements != null;
          newstate.cell_occupied = new int[board_elements.length][board_elements.length];
	for (int i = 0; i < board_elements.length; ++i) {
        System.arraycopy(cell_occupied[i], 0, newstate.cell_occupied[i], 0, board_elements.length);
	}
      }
      return newstate;
    }

    public void AddToBridgeCache(BoardElement first, BoardElement second) {
      if (first == null || second == null) { return; }
      final int fingerprint = ConnectionFingerprint(first, second);
      Log.d(getClass().getName(),
          String.format("Fingerprint of this bridge %d", fingerprint));
      // mark the end points as occupied.
      cell_occupied[first.row][first.col] = fingerprint;
      cell_occupied[second.row][second.col] = fingerprint;

      int dcol = second.col - first.col;
      int drow = second.row - first.row;

      if (first.row == second.row) {
	for (int i = (int) (first.col + Math.signum(dcol)); i != second.col; i += Math.signum(dcol)) {
	  cell_occupied[first.row][i] = fingerprint;

	}
      } else {
	assert first.col == second.col;
	for (int i = (int) (first.row + Math.signum(drow)); i != second.row; i+= Math.signum(drow)) {
	  cell_occupied[i][first.col] = fingerprint;


	}
      }
    }
  } // end of state

  private State current_state, old_state;
  static private final int WIDTH_HARD = 10;
  static private final int WIDTH_MEDIUM = 7;
  static private final int WIDTH_EASY = 4;

  private void NewGame(int hardness) {
    switch(hardness) {
      case EASY:
	Log.d(getClass().getName(), "Initializing new easy game");
	InitializeEasy();
	old_state = getCurrentState().CloneWithoutConnections();
	break;
    }
    switch(hardness) {
          case MEDIUM:
              Log.d(getClass().getName(), "Initializing new Medium game");
              InitializeMedium();
              old_state = getCurrentState().CloneWithoutConnections();
              break;
      }
      switch(hardness) {
          case HARD:
              Log.d(getClass().getName(), "Initializing new Hard game");
              InitializeHard();
              old_state = getCurrentState().CloneWithoutConnections();
              break;
      }
  }

  public void ResetGame() {
    if (old_state != null) {
      Log.d(getClass().getName(), "Setting board_elements to old_elements");
      setCurrentState(old_state.CloneWithoutConnections());
    } else {
      Log.d(getClass().getName(), "old_lements are zero");
    }
  }

  public BoardCreation(int hardness) {

      NewGame(hardness);
  }

  public boolean TryAddNewBridge(BoardElement start, BoardElement end, int count) {
    assertEquals(count, 1);
    assert (start != null);
    assert (end != null);
    final int fingerprint = ConnectionFingerprint(start, end);

    Log.d(getClass().getName(),
	String.format("considering (%d,%d) and (%d,%d)", start.row,start.col, end.row,end.col));
    if (start.row == end.row && start.col == end.col) {
      Log.d(getClass().getName(), "Same nodes selected!");
      return false;
    }
    assert count > 0;

    int dcol = end.col - start.col;
    int drow = end.row - start.row;

    // It must be a vertical or horizontal bridge:
    if (Math.abs(dcol) > 0 && Math.abs(drow) > 0) {
      Log.d(getClass().getName(), "not a horizontal or vertical bridge.");
      return false;
    }

    // First we check whether start and end elements can take the specified bridge counts.
    int count_start = start.GetCurrentCount();
    int count_end = end.GetCurrentCount();

    if (count_start  + count > start.max_connecting_bridges ||
	count_end + count > end.max_connecting_bridges) {
      Log.d(getClass().getName(), "Sums on start or end would be too large.");
      return false;
    }

    Log.d(getClass().getName(),
     String.format("Sums:%d @ (%d,%d)  and %d @ (%d,%d)",
       count_start, start.row, start.col,
       count_end, end.row, end.col));

    Connection start_connection = null;
    Connection end_connection = null;

    // Next we check whether we are crossing any lines.
    if (start.row == end.row) {
      for (int i = (int) (start.col + Math.signum(dcol)); i != end.col; i += Math.signum(dcol)) {
	if (getCurrentState().cell_occupied[start.row][i] > 0 &&
            getCurrentState().cell_occupied[start.row][i] != fingerprint) {
	  Log.d(getClass().getName(), "Crossing an occupied cell.");
	  return false;
	}
      }
      assert start.col != end.col;
      if (start.col > end.col) {
	start.connecting_east = GetOrCreateConnection(end, start.connecting_east);
	end.connecting_west = GetOrCreateConnection(start, end.connecting_west);
	start_connection = start.connecting_east;
	end_connection = end.connecting_west;
      } else {
	start.connecting_west = GetOrCreateConnection(end, start.connecting_west);
	end.connecting_east = GetOrCreateConnection(start, end.connecting_east);
	start_connection = start.connecting_west;
	end_connection = end.connecting_east;
      }
    } else {
      assert start.col == end.col;
      for (int i = (int) (start.row + Math.signum(drow)); i != end.row ; i += Math.signum(drow)) {
	if (getCurrentState().cell_occupied[i][start.col] > 0 &&
            getCurrentState().cell_occupied[i][start.col] != fingerprint) {
	  Log.d(getClass().getName(), "Crossing an occupied cell.");
	  return false;
	}
      }
      if (start.row > end.row ) {
	start.connecting_north = GetOrCreateConnection(end, start.connecting_north);
	end.connecting_south = GetOrCreateConnection(start, end.connecting_south);
	start_connection = start.connecting_north;
	end_connection = end.connecting_south;
      } else {
	start.connecting_south= GetOrCreateConnection(end, start.connecting_south);
	end.connecting_north = GetOrCreateConnection(start, end.connecting_north);
	start_connection = start.connecting_south;
	end_connection = end.connecting_north;
      }
    }
    start_connection.destination = end;
    end_connection.destination = start;
    start_connection.second += count;
    end_connection.second += count;

    getCurrentState().AddToBridgeCache(start, end);

    Log.d(getClass().getName(),
        String.format("New bridge added. Sums:%d @ (%d,%d)  and %d @ (%d,%d)",
         count_start, start.row,start.col,
         count_end, end.row,end.col));
    return true;
  }

  private Connection GetOrCreateConnection(
      BoardElement end,
      Connection connection) {
    if (connection!= null) { return connection; }
    return new Connection();
  }
    private boolean withinGrid(int colNum, int rowNum) {

        if((colNum < 0) || (rowNum <0) ) {
            return false;    //false if row or col are negative
        }
        if((colNum >= 10) || (rowNum >= 10)) {
            return false;    //false if row or col are > 75
        }
        return true;
    }
    //TODO: DFS ALGORITHM TO CHECK CONNECTIONS PROBABLY USING A STACK
  @TargetApi(Build.VERSION_CODES.N)
  private void InitializeEasy() {
      Random rand = new Random();

      setCurrentState(new State(WIDTH_EASY));
      for (int row = 0; row < debug_board_state_easy.length; row++) {
          for (int column = 0; column < debug_board_state_easy[row].length; column++) {
              debug_board_state_easy[row][column] = Integer.valueOf(rand.nextInt(5));

          }

      }


      for (int row = 0; row < debug_board_state_easy.length; row++) {
          for (int column = 0; column < debug_board_state_easy[row].length; column++) {
              System.out.print(debug_board_state_easy[row][column] + " ");
          }
            System.out.println();

      }
      this.search();
      for (int row = 0; row < WIDTH_EASY; ++row) {
          for (int column = 0; column < WIDTH_EASY; ++column) {


                  getCurrentState().board_elements[row][column] = new BoardElement();
                  getCurrentState().board_elements[row][column].max_connecting_bridges = Integer.valueOf(debug_board_state_easy[row][column]);
                  getCurrentState().board_elements[row][column].row = row;
                  getCurrentState().board_elements[row][column].col = column;

                  if (getCurrentState().board_elements[row][column].max_connecting_bridges > 0) {
                      getCurrentState().board_elements[row][column].is_island = true;
                  }

          }
      }

  }

    private void InitializeMedium() {
        Random rand = new Random();
        String[][] debug_board_state = new String[7][7];
        boolean[][] visited = new boolean[debug_board_state.length][debug_board_state[0].length];
        setCurrentState(new State(WIDTH_MEDIUM));
        for (int row = 0; row < debug_board_state.length; row++) {
            for (int column = 0; column < debug_board_state[row].length; column++) {
                debug_board_state[row][column] = String.valueOf(rand.nextInt(5));


            }
        }

        for (int row = 0; row < debug_board_state.length; row++) {
            for (int column = 0; column < debug_board_state[row].length; column++) {
                System.out.print(debug_board_state[row][column] + " ");
            }
            System.out.println();

        }
        for (int row = 0; row < WIDTH_MEDIUM; ++row) {
            for (int column = 0; column < WIDTH_MEDIUM; ++column) {
                getCurrentState().board_elements[row][column] = new BoardElement();
                getCurrentState().board_elements[row][column].max_connecting_bridges = Integer.parseInt(debug_board_state[row][column]);
                getCurrentState().board_elements[row][column].row = row;
                getCurrentState().board_elements[row][column].col = column;

                if (getCurrentState().board_elements[row][column].max_connecting_bridges > 0) {
                    getCurrentState().board_elements[row][column].is_island = true;
                }
            }
        }
    }
    private void InitializeHard() {
        Random rand = new Random();

        String[][] debug_board_state = new String[10][10];
        setCurrentState(new State(WIDTH_HARD));
        for (int row = 0; row < debug_board_state.length; row++) {
            for (int column = 0; column < debug_board_state[row].length; column++) {
                debug_board_state[row][column] = String.valueOf(rand.nextInt(5));

            }
        }

        for (int row = 0; row < debug_board_state.length; row++) {
            for (int column = 0; column < debug_board_state[row].length; column++) {
                System.out.print(debug_board_state[row][column] + " ");
            }
            System.out.println();

        }
        for (int row = 0; row < WIDTH_HARD; ++row) {
            for (int column = 0; column < WIDTH_HARD; ++column) {
                getCurrentState().board_elements[row][column] = new BoardElement();
                getCurrentState().board_elements[row][column].max_connecting_bridges = Integer.parseInt(debug_board_state[row][column]);
                getCurrentState().board_elements[row][column].row = row;
                getCurrentState().board_elements[row][column].col = column;

                if (getCurrentState().board_elements[row][column].max_connecting_bridges > 0) {
                    getCurrentState().board_elements[row][column].is_island = true;
                }
            }
        }
    }
    void search() {

        Map<Point, List<Path.Direction>> remainingOptions = new HashMap<>();

        Stack<Land> gameTree = new Stack<>();
        gameTree.push(new Land(debug_board_state_easy));

        while (true) {

            Land state = gameTree.peek();
            int[] p = state.lowestTodo();
            if (p == null)
                System.out.println("solution found");

            // move to next game state
            int r = p[0];
            int c = p[1];
            System.out.println("expanding game state for node at (" + r + ", " + c + ")");

            List<Path.Direction> ds;
            if (remainingOptions.containsKey(new Point(r, c)))
                ds = remainingOptions.get(new Point(r, c));
            else {
                ds = new ArrayList<>();
                for (Path.Direction dir : Path.Direction.values()) {
                    int[] tmp = state.nextIsland(r, c, dir);
                    if (tmp == null)
                        continue;
                    if (state.canBuildBridge(r, c, tmp[0], tmp[1]))
                        ds.add(dir);
                }
                remainingOptions.put(new Point(r, c), ds);
            }

            // if the node can no longer be expanded, and backtracking is not possible we quit
            if (ds.isEmpty() && gameTree.isEmpty()) {
                System.out.println("no valid configuration found");
                return;
            }

            // if the node can no longer be expanded, we need to backtrack
            if (ds.isEmpty()) {
                gameTree.pop();
                remainingOptions.remove(new Point(r, c));
                System.out.println("going back to previous decision");
                continue;
            }
            Log.e("gameTree", "WE ARE CRASHING HERE FUCK THIS SHIT");
            Path.Direction dir = ds.remove(0);
            System.out.println("connecting " + dir.name());
            remainingOptions.put(new Point(r, c), ds);
            Land nextState = new Land(state);
            int[] tmp = state.nextIsland(r, c, dir);
            nextState.connect(r, c, tmp[0], tmp[1]);
            gameTree.push(nextState);
        }
    }

  private void setCurrentState(State new_state) {
    this.current_state = new_state;
  }

  public State getCurrentState() {
    return current_state;
  }
}

