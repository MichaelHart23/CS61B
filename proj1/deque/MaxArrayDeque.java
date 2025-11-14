package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{
    private Comparator<T> defaultComparator;

    public MaxArrayDeque(Comparator<T> c) {
        defaultComparator = c;
    }

    public T max() {
        if(defaultComparator == null) {
            return null;
        }
        return max(defaultComparator);
    }

    public T max(Comparator<T> c) {
        if(size == 0) {
            return null;
        }
        T max_item = get(0);
        for(int i = 1; i < size; i++) {
            T item = get(i);
            if(c.compare(max_item, item) < 0) {
                max_item = item;
            }
        }
        return max_item;
    }
}
