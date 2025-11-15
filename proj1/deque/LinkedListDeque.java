package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class Node {
        T value;
        Node prev;
        Node next;
        Node() {
            value = null;
            prev = null;
            next = null;
        }
        Node(T v, Node p, Node n) {
            value = v;
            prev = p;
            next = n;
        }
    }

    private Node sentinel;
    private int size;

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node n;
        LinkedListDequeIterator() {
            n = sentinel.next;
        }

        @Override
        public boolean hasNext() {
            return n != sentinel;
        }

        @Override
        public T next() {
            T item = n.value;
            n = n.next;
            return item;
        }
    }


    public LinkedListDeque() {
        size = 0;
        sentinel = new Node();
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
    }
    @Override
    public void addFirst(T x) {
        Node node = new Node(x, sentinel, sentinel.next);
        sentinel.next.prev = node;
        sentinel.next = node;
        size++;
    }
    @Override
    public void addLast(T x) {
        Node node = new Node(x, sentinel.prev, sentinel);
        sentinel.prev.next = node;
        sentinel.prev = node;
        size++;
    }

    @Override
    public int size() {
        return size;
    }
    @Override
    public void printDeque() {
        String res = new String();
        Node n;
        for (n = sentinel.next; n != sentinel; n = n.next) {
            res += String.valueOf(n.value);
            res += ' ';
        }
        System.out.println(res);
    }
    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        Node n = sentinel.next;
        sentinel.next = n.next;
        sentinel.next.prev = sentinel;
        size--;
        return n.value;
    }
    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        Node n = sentinel.prev;
        sentinel.prev = n.prev;
        sentinel.prev.next = sentinel;
        size--;
        return n.value;
    }
    @Override
    public T get(int index) {
        if (size == 0 || index >= size || index < 0) {
            return null;
        }
        Node n;
        for(n = sentinel.next; n != sentinel && index != 0; n = n.next, index--) {}
        return n.value;
    }

    private T getRecursive(int index) {
        if(size == 0 || index >= size || index < 0) {
            return null;
        }
        Node n = sentinel.next;
        return getRecursiveHelper(n, index).value;
    }

    private Node getRecursiveHelper(Node n, int index) {
        if (index == 0) {
            return n;
        }
        n = n.next;
        index--;
        return getRecursiveHelper(n, index);
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
