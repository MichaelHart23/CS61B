package game2048;

/** Symbolic names for the four sides of a board.
 *  @author P. N. Hilfinger */
public enum Side {
    /** The parameters (COL0, ROW0, DCOL, and DROW) for each of the
     *  symbolic directions, D, below are to be interpreted as follows:
     *     The board's standard orientation has the top of the board
     *     as NORTH, and rows and columns (see Model) are numbered
     *     from its lower-left corner. Consider the board oriented
     *     so that side D of the board is farthest from you. Then
     *        * (COL0*s, ROW0*s) are the standard coordinates of the
     *          lower-left corner of the reoriented board (where s is the
     *          board size), and
     *        * If (c, r) are the standard coordinates of a certain
     *          square on the reoriented board, then (c+DCOL, r+DROW)
     *          are the standard coordinates of the squares immediately
     *          above it on the reoriented board.
     *  The idea behind going to this trouble is that by using the
     *  col() and row() methods below to translate from reoriented to
     *  standard coordinates, one can arrange to use exactly the same code
     *  to compute the result of tilting the board in any particular
     *  direction. */
    /**
     * 1：坐标原点为当前朝向的左下角
     * 2：这些设计都是为了解决一个问题：怎么把朝东坐标系里的一个坐标，转化为朝北坐标系里对应的坐标？
     * 把在该点当前坐标系里相对当前坐标系的变化放到原坐标系后会是什么？
     * 行的变化会不会转化为列的变化？正的变化会不会变为负的变化？
     * 把变化转化完成后，与当前坐标系的坐标原点在原坐标系的坐标加和，就得到了在原坐标系的坐标
     * 其中col0和row0就表示的当前坐标系原点在原坐标系的坐标这一信息，
     * 至于后面的dcol和drow，本质上是从对应旋转后的坐标系旋转回来的逆矩阵的新y hat的组合方式，而x hat的组合方式能通过y hat推出来
     *
     * 本质是坐标系的旋转和平移变换
     * */
    NORTH(0, 0, 0, 1), EAST(0, 1, 1, 0), SOUTH(1, 1, 0, -1),
    WEST(1, 0, -1, 0);

    /** The side that is in the direction (DCOL, DROW) from any square
     *  of the board.  Here, "direction (DCOL, DROW) means that to
     *  move one space in the direction of this Side increases the row
     *  by DROW and the colunn by DCOL.  (COL0, ROW0) are the row and
     *  column of the lower-left square when sitting at the board facing
     *  towards this Side. */
    Side(int col0, int row0, int dcol, int drow) {
        this.row0 = row0;
        this.col0 = col0;
        this.drow = drow;
        this.dcol = dcol;
    }

    /** Returns the side opposite of side S. */
    static Side opposite(Side s) {
        if (s == NORTH) {
            return SOUTH;
        } else if (s == SOUTH) {
            return NORTH;
        } else if (s == EAST) {
            return WEST;
        } else {
            return EAST;
        }
    }

    /** Return the standard column number for square (C, R) on a board
     *  of size SIZE oriented with this Side on top. */
    public int col(int c, int r, int size) {
        return col0 * (size - 1) + c * drow + r * dcol;
    }

    /** Return the standard row number for square (C, R) on a board
     *  of size SIZE oriented with this Side on top. */
    public int row(int c, int r, int size) {
        return row0 * (size - 1) - c * dcol + r * drow;
    }

    /** Parameters describing this Side, as documented in the comment at the
     *  start of this class. */
    private int row0, col0, drow, dcol;

};
