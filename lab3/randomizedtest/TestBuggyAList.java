package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {

    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> alnr = new AListNoResizing<>();
        BuggyAList<Integer> ba = new BuggyAList<>();
        alnr.addLast(4);
        ba.addLast(4);
        alnr.addLast(5);
        ba.addLast(5);
        alnr.addLast(6);
        ba.addLast(6);
        assertEquals((long)alnr.removeLast(), 6);
        assertEquals((long)ba.removeLast(), 6);
        assertEquals((long)alnr.removeLast(), 5);
        assertEquals((long)ba.removeLast(), 5);
        assertEquals((long)alnr.removeLast(), 4);
        assertEquals((long)ba.removeLast(), 4);
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int sizeL = L.size();
                int sizeB = B.size();;
            } else if(operationNumber == 2) {
                if(L.size() > 0) {
                    L.removeLast();
                    B.removeLast();
                }
            } else if(operationNumber == 3) {
                if(L.size() > 0) {
                    L.getLast();
                    B.getLast();
                }
            }
        }
    }
}
