package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: Michael Hart
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        if(row < 0 || row >= size() || col < 0 || col >= size()) { //对于越界的请求，返回null
            return null;
        }
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        board.setViewingPerspective(side);
        for(int col = 0; col < board.size(); col++) {
            if(tilt_one(col)) {
                changed = true;
            }
        }

        board.setViewingPerspective(Side.NORTH); //恢复朝向

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }
    /*
    * 总体是一个快慢指针的二重循环，慢的是“top_position_can_be_moved”，其记录了tile最多可以上移到哪里
    * 快的就是row和r，当找到一个非空tile时，row记录，然后去找另一个非空tile，找到用r记录，找不到r就变为-1
    * 若row和r能merge，则merge，并更新一次r；若不能，则r不变
    * 之后把merge与否的row指向的tile move到“top_position_can_be_moved”，更新row为r，继续循环
    */
    private boolean tilt_one(int col) {
        boolean changed = false;
        int top_position_can_be_moved = size() - 1; //记录可以被移动到的最上层的位置
        for(int row = top_position_can_be_moved; row >= 0; ) { //从顶到底开始遍历
            if(tile(col, row) != null) { //找到一个非空tile
                int r = row - 1;
                while(tile(col, r) == null && r >= 0) { //遍历找到下一个非空tile
                    r--;
                }
                if(tile(col,r) != null && tile(col,r).value() == tile(col,row).value()) { //如果能merge，就merge
                    score += 2 * tile(col,row).value(); //更新分数
                    board.move(col,row,tile(col,r)); //merge
                    //System.out.printf("merged row: %d and row: %d\n", r, row); //for debug
                    r--; //若merge，则再次更新r
                    changed = true; //标记状态已经改变
                }
                if(top_position_can_be_moved != row) { //位置不同
                    //把merge或没merge的tile放到可以放的最上面位置，只有当位置不同才move
                    board.move(col, top_position_can_be_moved, tile(col,row));
                    changed = true; //标记状态已经改变
                    //System.out.printf("moved form row: %d to row: %d\n", row, top_position_can_be_moved);//for debug
                }
                top_position_can_be_moved--; //更新“可以被移动到的最上层的位置”
                row = r; //更新row
            }
            else {
                row--;
            }
        }
        return changed;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        for(int i = 0; i < b.size(); i++) {
            for(int j = 0; j < b.size(); j++) {
                if(b.tile(i,j) == null){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        for(int i = 0; i < b.size(); i++) {
            for(int j = 0; j < b.size(); j++) {
                Tile t = b.tile(i,j);
                if(t != null && t.value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        if(emptySpaceExists(b)) {
            return true;
        }
        Tile tr = null, td = null, t = null;
        for(int i = 0; i < b.size(); i++) {
            for(int j = 0; j < b.size(); j++) {
                t = b.tile(i, j);
                if(i != b.size() - 1) {
                    td = b.tile(i + 1, j);
                    if(t.value() == td.value()) {
                        return true;
                    }
                }
                if(j != b.size() - 1) {
                    tr = b.tile(i, j + 1);
                    if(t.value() == tr.value()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
