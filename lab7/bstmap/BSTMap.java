package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private class Node {
        K key;
        V value;
        Node left, right;
        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    private Node root;
    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        Node node = root;
        int cmpRes = 0;
        while(node != null) {
            cmpRes = key.compareTo(node.key);
            if(cmpRes < 0) {
                node = node.left;
            } else if(cmpRes > 0) {
                node = node.right;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        Node node = root;
        int cmpRes = 0;
        while(node != null) {
            cmpRes = key.compareTo(node.key);
            if(cmpRes < 0) {
                node = node.left;
            } else if(cmpRes > 0) {
                node = node.right;
            } else {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (root == null) {
            root = new Node(key, value);
            size = 1;
            return;
        }
        Node pre = null;
        Node node = root;
        int cmpRes = 0;
        while(node != null) {
            cmpRes = key.compareTo(node.key);
            pre = node;
            if (cmpRes < 0) {
                node = node.left;
            } else if (cmpRes > 0) {
                node = node.right;
            } else {
                node.value = value;
                return;
            }
        }
        size++;
        if(cmpRes < 0) {
            pre.left = new Node(key, value);
        } else if(cmpRes > 0) {
            pre.right = new Node(key, value);
        }
    }

    @Override
    public Set<K> keySet() {
        if(root == null) {
            return null;
        }
        Set<K> set = new HashSet<>();
        //inOrder(root, set);
        Stack<Node> stack = new Stack<>();
        Node node = root;
        while(node != null || !stack.isEmpty()) {
            while(node != null) {
                stack.push(node);
                node = node.left;
            }
            Node n = stack.pop();
            set.add(n.key);
            node = n.right;
        }
        return set;
    }

    private void inOrder(Node node, Set<K> set) {
        if(node == null) return;
        inOrder(node.left, set);
        set.add(node.key);
        inOrder(node.right, set);
    }

    @Override
    public V remove(K key) {
        Node node = root;
        Node pre = root;
        int cmpRes = 0;
        while(node != null) {
            pre = node;
            cmpRes = key.compareTo(node.key);
            if(cmpRes < 0) {
                node = node.left;
            } else if(cmpRes > 0) {
                node = node.right;
            } else {
                break;
            }
        }
        if(node == null) {
            return null;
        }
        V res = node.value;
        if(node.left == null || node.right == null) {
            Node child = node.left == null ? node.right : node.left;
            if(node == root) {
                root = child;
            } else {
                if(pre.left == node) {
                    pre.left = child;
                } else {
                    pre.right = child;
                }
            }
        } else {
            Node temp = node.right;
            Node anotherPre = node;
            while(temp.left != null) {
                anotherPre = temp;
                temp = temp.left;
            }
            if(anotherPre != node) {
                anotherPre.left = null;
            } else {
                node.right = null;
            }
            node.key = temp.key;
            node.value = temp.value;
        }
        size--;
        return res;
    }

    @Override
    public V remove(K key, V value) {
        Node node = root;
        Node pre = root;
        int cmpRes = 0;
        while(node != null) {
            pre = node;
            cmpRes = key.compareTo(node.key);
            if(cmpRes < 0) {
                node = node.left;
            } else if(cmpRes > 0) {
                node = node.right;
            } else {
                break;
            }
        }
        if(node == null) {
            return null;
        }
        V res = node.value;
        if(res != value) {
            return res;
        }
        if(node.left == null || node.right == null) {
            Node child = node.left == null ? node.right : node.left;
            if(node == root) {
                root = child;
            } else {
                if(pre.left == node) {
                    pre.left = child;
                } else {
                    pre.right = child;
                }
            }
        } else {
            Node temp = node.right;
            Node anotherPre = node;
            while(temp.left != null) {
                anotherPre = temp;
                temp = temp.left;
            }
            if(anotherPre != node) {
                anotherPre.left = null;
            } else {
                node.right = null;
            }
            node.key = temp.key;
            node.value = temp.value;
        }
        size--;
        return res;
    }

    public void printInOrder() {
            if(root == null) return;
            Stack<Node> stack = new Stack<>();
            Node node = root;
            while(node != null || !stack.isEmpty()) {
                while(node != null) {
                    stack.push(node);
                    node = node.left;
                }
                Node n = stack.pop();
                System.out.println("Key: " + n.key + " Value: " + n.value);
                node = n.right;
            }
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
}
