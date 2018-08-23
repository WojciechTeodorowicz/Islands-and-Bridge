package Island_and_Bridges.Hashi;
import  Island_and_Bridges.Hashi.BoardElement;
import android.annotation.TargetApi;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;

import java.util.Arrays;

import static Island_and_Bridges.Hashi.BoardElement.*;
import static Island_and_Bridges.Hashi.BoardElement.Direction.EAST;
import static Island_and_Bridges.Hashi.BoardElement.Direction.NORTH;
import static Island_and_Bridges.Hashi.BoardElement.Direction.SOUTH;
import static android.graphics.Path.Direction.*;

public class Land {

    private int[][] BRIDGES_TO_BUILD;

    private boolean[][] IS_ISLAND;
    private Direction[][] BRIDGES_ALREADY_BUILT;

    public Land(int[][] bridgesToDo){
        BRIDGES_TO_BUILD = copy(bridgesToDo);

        int numberRows = bridgesToDo.length;
        int numberColumns = bridgesToDo[0].length;
        BRIDGES_ALREADY_BUILT = new Direction[numberRows][numberColumns];
        IS_ISLAND = new boolean[numberRows][numberColumns];
        for(int i=0;i<numberRows;i++) {
            for (int j = 0; j < numberColumns; j++) {
                BRIDGES_ALREADY_BUILT[i][j] = null;
                IS_ISLAND[i][j] = bridgesToDo[i][j] > 0;
            }
        }
    }

    public Land(Land other){
        BRIDGES_TO_BUILD = copy(other.BRIDGES_TO_BUILD);
        int numberRows = BRIDGES_TO_BUILD.length;
        int numberColumns =  BRIDGES_TO_BUILD[0].length;
        BRIDGES_ALREADY_BUILT = new Direction[numberRows][numberColumns];
        IS_ISLAND = new boolean[numberRows][numberColumns];
        for(int i=0;i<numberRows;i++) {
            for (int j = 0; j < numberColumns; j++) {
                BRIDGES_ALREADY_BUILT[i][j] = other.BRIDGES_ALREADY_BUILT[i][j];
                IS_ISLAND[i][j] = other.IS_ISLAND[i][j];
            }
        }
    }

    public int[] next(int row, int column, Direction dir){
        int numberRows = BRIDGES_TO_BUILD.length;
        int numberColumns = BRIDGES_TO_BUILD[0].length;

        // out of bounds
        if(row < 0 || row >=numberRows || column < 0 || column >= numberColumns)
            return null;


        // motion vectors
        int[][] motionVector = {{-1, 0},{0,1},{1,0},{0,-1}};
        int i = Arrays.asList(Direction.values()).indexOf(dir);

        // calculate next
        int[] out = new int[]{row + motionVector[i][0], column + motionVector[i][1]};

        row = out[0];
        column = out[1];

        // out of bounds
        if(row < 0 || row >=numberRows || column < 0 || column >= numberColumns)
            return null;

        // return
        return out;
    }

     public int[] nextIsland(int row, int column, Direction dir){
        int[] tmp = next(row,column,dir);
        if(tmp == null)
            return null;
        while(!IS_ISLAND[tmp[0]][tmp[1]]){
            tmp = next(tmp[0], tmp[1], dir);
            if(tmp == null)
                return null;
        }
        return tmp;
    }

    public boolean canBuildBridge(int row0, int column0, int row1, int column1){
        if(row0 == row1 && column0 > column1){
            return canBuildBridge(row0, column1, row1, column0);
        }
        if(column0 == column1 && row0 > row1){
            return canBuildBridge(row1, column0, row0, column1);
        }
        if(row0 == row1){
            int[] tmp = nextIsland(row0, column0, Direction.EAST);
            if(tmp == null)
                return false;
            if(tmp[0] == row1 || tmp[1] == column1)
                return false;
            if(BRIDGES_TO_BUILD[row0][column0] == 0)
                return false;
            if(BRIDGES_TO_BUILD[row1][column1] == 0)
                return false;
            for (int i = column0; i <= column1 ; i++) {
                if(IS_ISLAND[row0][i])
                    continue;
                if(BRIDGES_ALREADY_BUILT[row0][i] == Direction.NORTH)
                    return false;
            }
        }
        if(column0 == column1){
            int[] tmp = nextIsland(row0, column0, Direction.SOUTH);
            if(tmp == null)
                return false;
            if(tmp[0] == row1 || tmp[1] == column1)
                return false;
            if(BRIDGES_TO_BUILD[row0][column0] == 0 || BRIDGES_TO_BUILD[row1][column1] == 0)
                return false;
            for (int i = row0; i <= row1 ; i++) {
                if(IS_ISLAND[i][column0])
                    continue;
                if(BRIDGES_ALREADY_BUILT[i][column0] == Direction.EAST)
                    return false;
            }
        }
        // default
        return true;
    }

    public int[] lowestTodo(){
        int R = BRIDGES_TO_BUILD.length;
        int C = BRIDGES_TO_BUILD[0].length;

        int[] out = {0, 0};
        for (int i=0;i<R;i++) {
            for (int j = 0; j < C; j++) {
                if(BRIDGES_TO_BUILD[i][j] == 0)
                    continue;
                if (BRIDGES_TO_BUILD[out[0]][out[1]] == 0)
                    out = new int[]{i, j};
                if (BRIDGES_TO_BUILD[i][j] < BRIDGES_TO_BUILD[out[0]][out[1]])
                    out = new int[]{i, j};
            }
        }
        if (BRIDGES_TO_BUILD[out[0]][out[1]] == 0) {
            return null;
        }
        return out;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private int[][] copy(int[][] other){
        int[][] out = new int[other.length][other.length == 0 ? 0 : other[0].length];
        for(int r=0;r<other.length;r++)
            out[r] = Arrays.copyOf(other[r], other[r].length);
        return out;
    }

    public void connect(int row0, int column0, int row1, int column1){
        if(row0 == row1 && column0 > column1){
            connect(row0, column1, row1, column0);
            return;
        }
        if(column0 == column1 && row0 > row1){
            connect(row1, column0, row0, column1);
            return;
        }
        if(!canBuildBridge(row0, column0, row1, column1))
            return;

        BRIDGES_TO_BUILD[row0][column0]--;
        BRIDGES_TO_BUILD[row1][column1]--;

        if(row0 == row1){
            for (int i = column0; i <= column1 ; i++) {
                if(IS_ISLAND[row0][i])
                    continue;
                BRIDGES_ALREADY_BUILT[row0][i] = Direction.EAST;
            }
        }
        if(column0 == column1){
            for (int i = row0; i <= row1 ; i++) {
                if(IS_ISLAND[i][column0])
                    continue;
                BRIDGES_ALREADY_BUILT[i][column0] = Direction.NORTH;
            }
        }
    }
}