package com.cordelta.barr.david;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.WeakHashMap;

public class Node {
    private static WeakHashMap<Node, WeakReference<Node>> map =
            new WeakHashMap<Node, WeakReference<Node>>();

    public final String token;
    public final Node left, right, middle;
    public final int hash;

    private Node(Node left, Node middle, Node right, String token) {
        this.left = left;
        this.middle = middle;
        this.right = right;
        this.token = token.intern();
        int hash = token.hashCode();
        hash = 31 * hash + (left != null ? left.hash : 0);
        hash = 31 * hash + (right != null ? right.hash : 0);
        hash = 31 * hash + (middle != null ? middle.hash : 0);
        this.hash = hash * (int) 2654435761l;
    }

    private Node intern() {
        if (map.containsKey(this))
            return map.get(this).get();
        map.put(this, new WeakReference<Node>(this));
        return this;
    }

    public static int count() {
        return map.size();
    }

    public Node left(Node left) {
        return new Node(left, middle, right, token).intern();
    }

    public Node right(Node right) {
        return new Node(left, middle, right, token).intern();
    }

    public Node middle(Node middle) {
        return new Node(left, middle, right, token.intern());
    }

    public Node search(Iterator<String> key) {
        if (!key.hasNext()) return null;
        Node result = this;
        String token = key.next();
        while (result != null) {
            int cmp = token.compareTo(result.token);
            if (cmp == 0 && !key.hasNext())
                break;
            if (cmp == 0)
                token = key.next();
            result = cmp < 0 ? result.left : cmp == 0 ? result.middle : result.right;
        }
        return result;
    }

    public static Node first(Node root) {
        Node result = root;
        while (result != null && result.left != null)
            result = result.left;
        return result;
    }

    public static Node next(Node root, Node node) {
        int cmp;
        Node result = first(node.right);
        if (result == null)
            while (root != null && (cmp = node.token.compareTo(root.token)) != 0)
                if (cmp < 0) {
                    result = root;
                    root = root.left;
                } else
                    root = root.right;
        return result;
    }

    private Node balance() {
        if (left != null && left.hash > hash)
            return left.right(left(left.right));
        if (right != null && right.hash > hash)
            return right.left(right(right.left));
        return this;
    }

    public static Node insert(Node root, String token, Iterator<String> key) {
        if (token == null && !key.hasNext())
            return root;
        if (token == null)
            token = key.next();
        if (root == null)
            return new Node(null, insert(null, null, key), null, token).intern();
        int cmp = token.compareTo(root.token);
        if (cmp == 0 && !key.hasNext())
            return root;
        if (cmp == 0)
            return root.middle(insert(root.middle, null, key));
        if (cmp < 0)
            return root.left(insert(root.left, token, key)).balance();
        return root.right(insert(root.right, token, key)).balance();
    }

    public static Node remove(Node root, String token, Iterator<String> key) {
        if (token == null && !key.hasNext()) return root;
        if (token == null) token = key.next();
        if (root == null) {
            return null;
        }
        int cmp = token.compareTo(root.token);
        if (cmp == 0 && !key.hasNext()) {
            if (root.left == null) {
                if (root.right == null)
                    return null;
            } else if (root.right == null || root.left.hash > root.right.hash)
                return root.left.right(remove(root.left(root.left.right), token, key));
            return root.right.left(remove(root.right(root.right.left), token, key));
        }
        if (cmp == 0)
            return root.middle(remove(root.middle, null, key));
        if (cmp < 0)
            return root.left(remove(root.left, token, key));
        return root.right(remove(root.right, token, key));
    }

    private Node graft(String token, Iterator<String> key, Node source) {
        int cmp = token.compareTo(this.token);
        if (cmp == 0 && !key.hasNext()) {
            return middle(source.middle);
        }
        if (cmp == 0) {
            return middle(graft(middle, null, key, source));
        } else if (cmp < 0) {
            return left(graft(left, token, key, source)).balance();
        } else {
            return right(graft(right, token, key, source)).balance();
        }
    }

    public static Node graft(Node root, String token, Iterator<String> key, Node source) {
        if (token == null && !key.hasNext()) return source.middle;
        if (token == null) token = key.next();
        if (root == null) {
            return new Node(null, graft(null, null, key, source), null, token).intern();
        }
        return root.graft(token, key, source);
    }

    public interface DeltaHandler {
        public void delta(String path, Node a, Node b);
    }

    public static void diff(String path, Node aRoot, Node bRoot, DeltaHandler handler) {
        Node a = first(aRoot);
        Node b = first(bRoot);
        while (a != null || b != null) {
            int cmp = a == null ? 1 : b == null ? -1 : a.token.compareTo(b.token);
            if (cmp < 0 && a != null) {
                handler.delta(path + "/" + a.token, a, null);
                diff(path + "/" + (a.token), a.middle, b == null ? null : b.middle, handler);
                a = next(aRoot, a);
                continue;
            }
            if (cmp > 0 && b != null) {
                handler.delta(path + "/" + b.token, null, b);
                diff(path + "/" + b.token, a == null ? null : a.middle, b.middle, handler);
                b = next(bRoot, b);
                continue;
            }
            handler.delta(path + "/" + (a != null ? a.token : b.token), a, b);
            diff(path + "/" + (a != null ? a.token : b.token),
                    a == null ? null : a.middle,
                    b == null ? null : b.middle,
                    handler);
            a = next(aRoot, a);
            b = next(bRoot, b);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node node = (Node) o;

        if (hash != node.hash) return false;
        if (left != null ? !left.equals(node.left) : node.left != null) return false;
        if (middle != null ? !middle.equals(node.middle) : node.middle != null) return false;
        if (right != null ? !right.equals(node.right) : node.right != null) return false;
        if (token != null ? !token.equals(node.token) : node.token != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        toString(0, this, sb);
        return sb.toString();
    }

    private static void toString(int depth, Node that, StringBuffer sb) {
        if (that == null) {
            return;
        }
        toString(depth, that.left, sb);
        for (int i = 0; i < depth; ++i) sb.append(' ');
        sb.append('\'').append(that.token).append("'\n");
        toString(depth + 1, that.middle, sb);
        toString(depth, that.right, sb);
    }
}