package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Random;
import java.util.*;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 28;
    private static final int HEIGHT = 30;
    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    private static class Hexagon {
        public int x;
        public int y;
        public int size;
        public Hexagon() {}

        public Hexagon(int X, int Y, int Size) {
            x = X;
            y = Y;
            size = Size;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Hexagon h = (Hexagon) o;
            return x == h.x && y == h.y && size == h.size;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, size);
        }
    }


    private static boolean outOfBound(Hexagon h) {
        int leftMost = h.x - h.size + 1;
        int rightMost = leftMost + h.size + 2 * (h.size - 1) - 1;
        int upMost = h.y;
        int downMost = h.y - 2 * h.size + 1;
        return leftMost < 0 || rightMost >= WIDTH || upMost >= HEIGHT || downMost < 0;
    }
    private static void drawALine(TETile[][] tiles, int x,int y, int len, TETile t) {
        if(y < 0 || y >= HEIGHT) return;
        if(x >= WIDTH) return;
        if(x < 0) {
            len += x;
            x = 0;
        }
        if(len <= 0) return;
        for(int i = x; i < x + len; i++) {
            tiles[i][y] = t;
        }
    }
    private static void addHexagon(TETile[][] tiles, Hexagon h, TETile t) {
        //或许out of bound的也照画不误，只画在边界内的就可以了, 但对于这个demo来说不适用
        if(outOfBound(h)) return;

        //绘制上半部分
        int i = 0;
        while(i < h.size) {
            drawALine(tiles, h.x - i, h.y - i, h.size + 2 * i, t);
            i++;
        }
        //绘制下半部分
        int j = 0;
        while(j < h.size) {
            drawALine(tiles, h.x - i + j + 1, h.y - i - j, h.size + 2 * (i - j - 1), t);
            j++;
        }
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(8);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.GRASS;
            case 3: return Tileset.WATER;
            case 4: return Tileset.LOCKED_DOOR;
            case 5: return Tileset.UNLOCKED_DOOR;
            case 6: return Tileset.SAND;
            case 7: return Tileset.MOUNTAIN;
            case 8: return Tileset.TREE;
            default: return Tileset.FLOWER;
        }
    }

    private static ArrayList<Hexagon> getSurroundingHexagon(Hexagon h) {
        ArrayList<Hexagon> res = new ArrayList<>();
        res.add(new Hexagon(h.x, h.y + 2 * h.size, h.size)); //正上方
        res.add(new Hexagon(h.x, h.y - 2 * h.size, h.size)); //正下方
        res.add(new Hexagon(h.x - h.size - (h.size - 1), h.y + h.size, h.size)); //左上
        res.add(new Hexagon(h.x - h.size - (h.size - 1), h.y - h.size, h.size)); //左下
        res.add(new Hexagon(h.x + (2 * h.size - 1), h.y + h.size, h.size)); //右上
        res.add(new Hexagon(h.x + (2 * h.size - 1), h.y - h.size, h.size)); //右下
        return res;
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        Hexagon hexagon = new Hexagon(12, HEIGHT - 1, 3);
        HashSet<Hexagon> visited = new HashSet<>();
        Deque<Hexagon> q = new ArrayDeque<>();
        q.add(hexagon);
        while(!q.isEmpty()) {
            Hexagon h = q.pollFirst();
            addHexagon(world, h, randomTile());
            visited.add(h);
            ArrayList<Hexagon> surround = getSurroundingHexagon(h);
            for(Hexagon it : surround) {
                if(visited.contains(it) || outOfBound(it)) continue;
                q.add(it);
            }
        }
        ter.renderFrame(world);
    }
}
