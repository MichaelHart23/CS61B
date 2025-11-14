package deque;
import java.lang.Math;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    int capacity;
    int size;
    int startIndex; //双端队列头在哪里
    int nextLast;
    int nextFirst;
    T[] arr;
    float resizeFactor;

    public ArrayDeque() {
        capacity = 8;
        size = 0;
        startIndex = capacity / 2;
        nextLast = startIndex;
        nextFirst = Math.floorMod(startIndex - 1, capacity);
        arr = (T[])new Object[capacity];
        resizeFactor = 1.5f;
    }

    public ArrayDeque(ArrayDeque<T> another) {
        capacity = another.capacity;
        size = another.size;
        startIndex = another.startIndex;
        nextFirst = another.nextFirst;
        nextLast = another.nextLast;
        resizeFactor = another.resizeFactor;
        arr = (T[])new Object[capacity];
        for(int i = 0; i < size; i++) {
            arr[(startIndex + i) % capacity] = another.arr[(startIndex + i) % capacity];
        }
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
            return num == size;
        }

        @Override
        public T next() {
            num++;
            T res = arr[index++];
            if(index == capacity) {
                index = 0;
            }
            return res;
        }
    }

    @Override
    public void addFirst(T x) {
        if(size == capacity) {
            resize((int)(capacity * resizeFactor));
        }
        startIndex = nextFirst;
        arr[nextFirst] = x;
        nextFirst = Math.floorMod((nextFirst - 1), capacity);
        size++;
    }
    @Override
    public void addLast(T x) {
        if(size == capacity) {
            resize((int)(capacity * resizeFactor));
        }
        arr[nextLast] = x;
        nextLast = Math.floorMod((nextLast + 1), capacity);
        size++;
    }

    private void resize(int new_capacity) {
        T[] new_arr = (T[])new Object[new_capacity];
        int new_startIndex = new_capacity / 2 - size / 2;
        for(int i = new_startIndex, j = startIndex, k = 0; k < size; k++) {
            new_arr[i] = arr[j];
            if(++i == new_capacity) {
                i = 0;
            }
            if(++j == capacity) {
                j = 0;
            }
        }
        startIndex = new_startIndex;
        capacity = new_capacity;
        arr = new_arr;
        nextFirst = Math.floorMod(startIndex - 1, capacity);
        nextLast = Math.floorMod(startIndex + size, capacity);
    }
    @Override
    public void printDeque() {
        String res = new String();
        for(int i = 0, j = 0; i < size; i++) {
            res += String.valueOf(arr[startIndex + j]);
            if(++j == capacity) {
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
        if(size == 0) {
            return null;
        }
        size--;
        startIndex = Math.floorMod((startIndex + 1), capacity);
        nextFirst = Math.floorMod((nextFirst + 1), capacity);
        T res = arr[nextFirst];
        arr[nextFirst] = null;
        if(size > 16 && size < capacity / 4) {
            resize(capacity/4);
        }
        return res;
    }
    @Override
    public T removeLast() {
        if(size == 0) {
            return null;
        }
        size--;
        nextLast = Math.floorMod((nextLast - 1), capacity);
        T res = arr[nextLast];
        arr[nextLast] = null;
        if(size > 16 && size < capacity / 4) {
            resize(capacity/4);
        }
        return res;
    }
    @Override
    public T get(int index) {
        if(size == 0 || index < 0 || index >= size) {
            return null;
        }
        int pos = Math.floorMod(startIndex + index, capacity);
        return arr[pos];
    }

    public T getRecursive(int index) {
        throw new UnsupportedOperationException("No need to implement getRecursive for proj 1b");
    }

    public boolean equals(Object o) {
        if(!(o instanceof ArrayDeque<?>) || o == null) {
            return false;
        }
        if(this == o) {
            return true;
        }
        ArrayDeque<?> other = (ArrayDeque<?>) o;
        if(other.size != size) {
            return false;
        }
        for(int i = 0; i < size; i++) {
            if(!get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

}
