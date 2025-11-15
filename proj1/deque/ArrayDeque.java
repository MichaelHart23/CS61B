package deque;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int capacity;
    private int size;
    private int startIndex; //双端队列头在哪里
    private int nextLast;
    private int nextFirst;
    private T[] arr;
    private float resizeFactor;

    public ArrayDeque() {
        capacity = 8;
        size = 0;
        startIndex = capacity / 2;
        nextLast = startIndex;
        nextFirst = Math.floorMod(startIndex - 1, capacity);
        arr = (T[]) new Object[capacity];
        resizeFactor = 1.5f;
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int index;
        private int num;
        ArrayDequeIterator() {
            index = startIndex;
            num = 0;
        }
        @Override
        public boolean hasNext() {
            return num < size;
        }

        @Override
        public T next() {
            num++;
            T res = arr[index++];
            if (index == capacity) {
                index = 0;
            }
            return res;
        }
    }

    @Override
    public void addFirst(T x) {
        if (size == capacity) {
            resize((int) (capacity * resizeFactor));
        }
        startIndex = nextFirst;
        arr[nextFirst] = x;
        nextFirst = Math.floorMod((nextFirst - 1), capacity);
        size++;
    }
    @Override
    public void addLast(T x) {
        if (size == capacity) {
            resize((int) (capacity * resizeFactor));
        }
        arr[nextLast] = x;
        nextLast = Math.floorMod((nextLast + 1), capacity);
        size++;
    }

    private void resize(int newCapacity) {
        T[] newArr = (T[]) new Object[newCapacity];
        int newStartIndex = newCapacity / 2 - size / 2;
        for (int i = newStartIndex, j = startIndex, k = 0; k < size; k++) {
            newArr[i] = arr[j];
            if (++i == newCapacity) {
                i = 0;
            }
            if (++j == capacity) {
                j = 0;
            }
        }
        startIndex = newStartIndex;
        capacity = newCapacity;
        arr = newArr;
        nextFirst = Math.floorMod(startIndex - 1, capacity);
        nextLast = Math.floorMod(startIndex + size, capacity);
    }
    @Override
    public void printDeque() {
        String res = new String();
        for (int i = 0, j = 0; i < size; i++) {
            res += String.valueOf(arr[startIndex + j]);
            if (++j == capacity) {
                j = 0;
            }
        }
        System.out.println(res);
    }
    @Override
    public int size() {
        return size;
    }


    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        size--;
        startIndex = Math.floorMod((startIndex + 1), capacity);
        nextFirst = Math.floorMod((nextFirst + 1), capacity);
        T res = arr[nextFirst];
        arr[nextFirst] = null;
        if (size > 16 && size < capacity / 4) {
            resize(capacity / 4);
        }
        return res;
    }
    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        size--;
        nextLast = Math.floorMod((nextLast - 1), capacity);
        T res = arr[nextLast];
        arr[nextLast] = null;
        if (size > 16 && size < capacity / 4) {
            resize(capacity / 4);
        }
        return res;
    }
    @Override
    public T get(int index) {
        if (size == 0 || index < 0 || index >= size) {
            return null;
        }
        int pos = Math.floorMod(startIndex + index, capacity);
        return arr[pos];
    }

    public boolean equals(Object o) {
        if (!(o instanceof Deque<?>) || o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        Deque<?> other = (Deque<?>) o;
        if (other.size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

}
