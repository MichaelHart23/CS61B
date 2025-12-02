package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {
    
    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }


    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int capacity;
    private int size;
    double loadFactor;
    private HashSet<K> keys;

    /** Constructors */
    public MyHashMap() {
        capacity = 16;
        buckets = createTable(capacity);
        loadFactor = 0.75;
        keys = new HashSet<>();
    }

    public MyHashMap(int initialSize) {
        capacity = initialSize;
        buckets = createTable(capacity);
        loadFactor = 0.75;
        keys = new HashSet<>();
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        capacity = initialSize;
        buckets = createTable(capacity);
        loadFactor = maxLoad;
        keys = new HashSet<>();
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return  new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] c = new Collection[tableSize];
        for(int i = 0; i < tableSize; i++) {
            c[i] = createBucket();
        }
        return c;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!


    @Override
    public void clear() {
        buckets = createTable(capacity);
        keys = new HashSet<>();
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return keys.contains(key);
    }

    private int getIndex(K key, int len) {
        int rawCode = key.hashCode();
        int hashCode = rawCode >= 0 ? rawCode : -rawCode;

        return hashCode % len;
    }

    @Override
    public V get(K key) {
        if(!keys.contains(key)) {
            return null;
        }

        int index = getIndex(key, capacity);

        Collection<Node> bucket = buckets[index];
        for(Node n : bucket) {
            if(n.key.equals(key)) {
                return n.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private void resize() {
        int newCapacity = capacity * 2;
        Collection<Node>[] newBuckets = createTable(newCapacity);
        HashSet<K> visited = new HashSet<>();
        for(K key : keys) {
            if(visited.contains(key)) {
                continue;
            }
            int index = getIndex(key, capacity);
            for(Node n : buckets[index]) {
                newBuckets[getIndex(n.key, newCapacity)].add(n);
            }
        }
        capacity = newCapacity;
        buckets = newBuckets;
    }

    @Override
    public void put(K key, V value) {
        if((double)size/capacity >= loadFactor) {
            resize();
        }
        int index = getIndex(key, capacity);
        Collection<Node> bucket = buckets[index];
        if(keys.contains(key)) {
            for(Node n : bucket) {
                if(n.key.equals(key)) {
                    n.value = value;
                    return;
                }
            }
        } else {
            bucket.add(createNode(key, value));
            keys.add(key);
            size++;
        }
    }

    @Override
    public Set<K> keySet() {
        return keys;
    }

    @Override
    public V remove(K key) {
        if(!keys.contains(key)) {
            return null;
        }
        V res = null;
        int index = getIndex(key, capacity);
        for(Node n : buckets[index]) {
            if(n.key.equals(key)) {
                res = n.value;
                buckets[index].remove(n);
                size--;
                keys.remove(key);
            }
        }
        return res;
    }

    @Override
    public V remove(K key, V value) {
        if(!keys.contains(key)) {
            return null;
        }
        V res = null;
        int index = getIndex(key, capacity);
        for(Node n : buckets[index]) {
            if(n.key.equals(key) ) {
                if(!n.value.equals(value)) {
                    return n.value;
                }
                res = n.value;
                buckets[index].remove(n);
                size--;
                keys.remove(key);
            }
        }
        return res;
    }


    @Override
    public Iterator<K> iterator() {
        return new MyHashMapIterator();
    }

    private class MyHashMapIterator implements Iterator<K> {
        private Deque<K> unvisited;

        MyHashMapIterator() {
            unvisited = new ArrayDeque<>();
            for(K key : keys) {
                unvisited.add(key);
            }
        }

        @Override
        public boolean hasNext() {
            return !unvisited.isEmpty();
        }

        @Override
        public K next() {
            return unvisited.poll();
        }
    }



}
