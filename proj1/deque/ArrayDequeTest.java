package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    public void RandomAddRemoveTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        for(int i = 0; i < 5000; i++) {
            int randomOperation = StdRandom.uniform(0,4);
            if(randomOperation == 0) { //add
                ad.addFirst(i);
            } else if(randomOperation == 1) {
                ad.addLast(i);
            } else if(randomOperation == 2) {
                ad.removeFirst();
            } else if(randomOperation == 3) {
                ad.removeLast();
            }
        }
    }

    @Test
    public void resizeTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ArrayDeque<Integer> forTest = new ArrayDeque<>();
        for(int i = 0; i < 10000; i++) {
            if(ad.size == ad.capacity) { //resize
                ad.addLast(i); //resize
                assertEquals((long)ad.removeLast(), i);
                assertTrue(ad.equals(forTest));
            } else {
                ad.addLast(i);
                forTest.addLast(i);
            }
        }
        for(int i = 0; i < 10000; i++) {
            if(ad.size - 1 == ad.capacity / 4) {
                int k = ad.removeLast(); //resize
                ad.addLast(k);
                assertTrue(ad.equals(forTest));
            } else {
                ad.removeLast();
                forTest.removeLast();
            }
        }
    }
}
