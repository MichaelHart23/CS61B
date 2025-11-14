package deque;

public class LinkedListDeque<T> {
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
    int size;
    public LinkedListDeque() {
        size = 0;
        sentinel = new Node();
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
    }

    public void addFirst(T x) {
        Node node = new Node(x, sentinel, sentinel.next);
        sentinel.next.prev = node;
        sentinel.next = node;
        size++;
    }

    public void addLast(T x) {
        Node node = new Node(x, sentinel.prev, sentinel);
        sentinel.prev.next = node;
        sentinel.prev = node;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        String res = new String();
        Node n;
        for(n = sentinel.next; n != sentinel; n = n.next) {
            res += String.valueOf(n.value);
            res += ' ';
        }
        System.out.println(res);
    }

    public T removeFirst() {
        if(size == 0) {
            return null;
        }
        Node n = sentinel.next;
        sentinel.next = n.next;
        sentinel.next.prev = sentinel;
        size--;
        return n.value;
    }

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

    public T get(int index) {
        if(size == 0 || index >= size || index < 0) {
            return null;
        }
        Node n;
        for(n = sentinel.next; n != sentinel && index != 0; n = n.next, index--) {
            ;
        }
        return n.value;
    }

    public T getRecursive(int index) {
        if(size == 0 || index >= size || index < 0) {
            return null;
        }
        Node n = sentinel.next;
        return getRecursiveHelper(n, index).value;
    }

    private Node getRecursiveHelper(Node n, int index) {
        if(index == 0) {
            return n;
        }
        n = n.next;
        index--;
        return getRecursiveHelper(n, index);
    }

    public boolean equals(Object o) {
        if(!(o instanceof LinkedListDeque<?>) || o == null) {
            return false;
        }
        if(this == o) {
            return true;
        }
        LinkedListDeque<?> other = (LinkedListDeque<?>) o;
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
