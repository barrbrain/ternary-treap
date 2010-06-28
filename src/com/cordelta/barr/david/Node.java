package com.cordelta.barr.david;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private static final uint32_t NULL = new uint32_t.immutable(Integer.MAX_VALUE);

    public static Node search(Node rootNode, Sequence<String> key) {
        if (key.value() == null)
            return rootNode;
        uint32_t root = uint32_t.copy(trpn_offset(rootNode));
        while (!NULL.equals(root)) {
            int cmp = key.value().compareTo(trp_token_get(root));
            if (cmp == 0 && key.next().value() == null)
                break;
            root.value(cmp < 0 ? trp_left_get(root) :
                    cmp == 0 ? trp_middle_get(root) :
                            trp_right_get(root));
        }
        return trpn_pointer(root);
    }

    private static void balance(uint32_t node) {
        uint32_t left = trp_left_get(node);
        if (!NULL.equals(left) &&
                trp_prio_get(left) > trp_prio_get(node)) {
            trpn_rotate_right(node);
            return;
        }
        uint32_t right = trp_right_get(node);
        if (!NULL.equals(right) &&
                trp_prio_get(right) > trp_prio_get(node)) {
            trpn_rotate_left(node);
        }
    }

    public static Node insert(Node root, Sequence<String> key) {
        uint32_t offset = new uint32_t().value(trpn_offset(root));
        insert(offset, key);
        return trpn_pointer(offset);
    }

    public static void insert(uint32_t root, Sequence<String> key) {
        if (key.value() == null)
            return;
        if (NULL.equals(root)) {
            root.value(base.size());
            base.add(new Node(NULL, NULL, NULL, key.value(), base.size()));
        }
        int cmp = key.value().compareTo(trp_token_get(root));
        if (cmp == 0) {
            uint32_t middle = trp_middle_get(root).copy();
            insert(middle, key.next());
            trp_middle_set(root, middle);
            return;
        }
        if (cmp < 0) {
            uint32_t left = trp_left_get(root).copy();
            insert(left, key);
            trp_left_set(root, left);
            balance(root);
            return;
        }
        uint32_t right = trp_right_get(root).copy();
        insert(right, key);
        trp_right_set(root, right);
        balance(root);
    }

    private static void removeRoot(uint32_t root) {
        if (NULL.equals(trp_left_get(root))) {
            if (NULL.equals(trp_right_get(root))) {
                root.value(NULL);
                return;
            }
        } else if (NULL.equals(trp_right_get(root)) ||
                trp_prio_get(trp_left_get(root)) >
                        trp_prio_get(trp_right_get(root))) {
            trpn_rotate_right(root);
            removeRoot(trp_right_get(root));
            return;
        }
        trpn_rotate_left(root);
        removeRoot(trp_left_get(root));
    }

    public static Node remove(Node root, Sequence<String> key) {
        uint32_t offset = uint32_t.copy(trpn_offset(root));
        remove(offset, key);
        return trpn_pointer(offset);
    }

    public static void remove(uint32_t root, Sequence<String> key) {
        if (key.value() == null || NULL.equals(root)) {
            root.value(NULL);
            return;
        }
        int cmp = key.value().compareTo(trp_token_get(root));
        if (cmp == 0 && key.next().value() == null) {
            removeRoot(root);
            return;
        }
        if (cmp == 0) {
            uint32_t middle = trp_middle_get(root).copy();
            remove(middle, key);
            trp_middle_set(root, middle);
            return;
        }
        if (cmp < 0) {
            uint32_t left = trp_left_get(root).copy();
            remove(left, key);
            trp_left_set(root, left);
            return;
        }
        uint32_t right = trp_right_get(root).copy();
        remove(right, key);
        trp_right_set(root, right);
    }

    public static Node graft(Node root, Sequence<String> key, Node source) {
        uint32_t offset = uint32_t.copy(trpn_offset(root));
        graft(offset, key, uint32_t.copy(trpn_offset(source)));
        return trpn_pointer(offset);
    }

    public static void graft(uint32_t root, Sequence<String> key, uint32_t source) {
        if (key.value() == null) {
            root.value(trp_middle_get(source));
            return;
        }
        if (NULL.equals(root)) {
            root.value(base.size());
            base.add(new Node(NULL, NULL, NULL, key.value(), root.value()));
        }
        int cmp = key.value().compareTo(trp_token_get(root));
        if (cmp == 0) {
            uint32_t middle = trp_middle_get(root).copy();
            graft(middle, key.next(), source);
            trp_middle_set(root, middle);
            return;
        }
        if (cmp < 0) {
            uint32_t left = trp_left_get(root).copy();
            graft(left, key, source);
            trp_left_set(root, left);
            return;
        }
        uint32_t right = trp_right_get(root).copy();
        graft(right, key, source);
        trp_right_set(root, right);
    }

    private static uint32_t first(uint32_t root) {
        uint32_t result = root.copy();
        while (!NULL.equals(result) && !NULL.equals(trp_left_get(result)))
            result.value(trp_left_get(result));
        return result;
    }

    private static uint32_t next(uint32_t root, uint32_t node) {
        int cmp;
        uint32_t result = first(trp_right_get(node));
        if (NULL.equals(result))
            while (!NULL.equals(root) &&
                    (cmp = trp_token_get(node).compareTo(trp_token_get(root))) != 0)
                if (cmp < 0) {
                    result.value(root);
                    root = trp_left_get(root);
                } else
                    root = trp_right_get(root);
        return result;
    }

    public static void diff(String path, Node aRoot, Node bRoot, DeltaHandler handler) {
        diff(path, uint32_t.copy(trpn_offset(aRoot)), uint32_t.copy(trpn_offset(bRoot)), handler);
    }

    public static void diff(String path, uint32_t aRoot, uint32_t bRoot, DeltaHandler handler) {
        uint32_t a = first(aRoot);
        uint32_t b = first(bRoot);
        while (!NULL.equals(a) || !NULL.equals(b)) {
            int cmp = NULL.equals(a) ? 1 : NULL.equals(b) ? -1 : trp_token_get(a).compareTo(trp_token_get(b));
            String deltaPath = path + "/" + (cmp <= 0 ? trp_token_get(a) : trp_token_get(b));
            handler.delta(deltaPath, cmp > 0 ? null : trpn_pointer(a), cmp < 0 ? null : trpn_pointer(b));
            if (cmp >= 0 && !NULL.equals(trp_middle_get(b)))
                diff(deltaPath, cmp > 0 || NULL.equals(a) ? NULL : trp_middle_get(a), trp_middle_get(b), handler);
            if (cmp <= 0)
                a = next(aRoot, a);
            if (cmp >= 0)
                b = next(bRoot, b);
        }
    }

    public interface DeltaHandler {
        public void delta(String path, Node a, Node b);
    }

    public final String token;
    public uint32_t left, right, middle;
    private final int offset;

    private Node(uint32_t left, uint32_t middle, uint32_t right, String token, int offset) {
        this.left = left.copy();
        this.middle = middle.copy();
        this.right = right.copy();
        this.token = token.intern();
        this.offset = offset;
    }

    public static int count() {
        return base.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node node = (Node) o;

        if (left != null ? !left.equals(node.left) : node.left != null) return false;
        if (middle != null ? !middle.equals(node.middle) : node.middle != null) return false;
        if (right != null ? !right.equals(node.right) : node.right != null) return false;
        if (token != null ? !token.equals(node.token) : node.token != null) return false;

        return true;
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
        toString(depth, trpn_pointer(that.left), sb);
        for (int i = 0; i < depth; ++i) sb.append(' ');
        sb.append('\'').append(that.token).append("'\n");
        toString(depth + 1, trpn_pointer(that.middle), sb);
        toString(depth, trpn_pointer(that.right), sb);
    }

    private static List<Node> base = new ArrayList<Node>();

    private static Node trpn_pointer(int offset) {
        return offset > base.size() ? null : base.get(offset);
    }

    private static Node trpn_pointer(uint32_t offset) {
        return trpn_pointer(offset.value());
    }

    private static int trpn_offset(Node node) {
        return node == null ? Integer.MAX_VALUE : node.offset;
    }

    private static int committed;

    public static void commit() {
        committed = base.size();
    }

    private static void trpn_modify(uint32_t offset) {
        if (offset.value() < committed) {
            Node node = trpn_pointer(offset);
            offset.value(base.size());
            base.add(new Node(node.left, node.middle, node.right, node.token, offset.value()));
        }
    }

    /* Left accessors. */
    private static uint32_t trp_left_get(uint32_t offset) {
        return trp_left_get(offset.value());
    }

    private static uint32_t trp_left_get(int offset) {
        Node node = trpn_pointer(offset);
        return node == null ? NULL : node.left;
    }

    private static void trp_left_set(uint32_t offset, uint32_t left) {
        trp_left_set(offset, left.value());
    }

    private static void trp_left_set(uint32_t offset, int left) {
        trpn_modify(offset);
        trpn_pointer(offset).left.value(left);
    }


    /* Right accessors. */
    private static uint32_t trp_right_get(uint32_t offset) {
        return trp_right_get(offset.value());
    }

    private static uint32_t trp_right_get(int offset) {
        Node node = trpn_pointer(offset);
        return node == null ? NULL : node.right;
    }


    private static void trp_right_set(uint32_t offset, uint32_t right) {
        trp_right_set(offset, right.value());
    }

    private static void trp_right_set(uint32_t offset, int right) {
        trpn_modify(offset);
        trpn_pointer(offset).right.value(right);
    }

    /* Middle accessors. */
    private static uint32_t trp_middle_get(uint32_t offset) {
        return trp_middle_get(offset.value());
    }

    private static uint32_t trp_middle_get(int offset) {
        Node node = trpn_pointer(offset);
        return node == null ? NULL : node.middle;
    }

    private static void trp_middle_set(uint32_t offset, uint32_t middle) {
        trpn_modify(offset);
        trpn_pointer(offset).middle.value(middle);
    }

    /* Token accessor. */
    private static String trp_token_get(uint32_t offset) {
        return trp_token_get(offset.value());
    }

    private static String trp_token_get(int offset) {
        return trpn_pointer(offset).token;
    }

    /* Priority accessors. */
    private static final int KNUTH_GOLDEN_RATIO_32BIT = (int) 2654435761l;

    private static int trp_prio_get(uint32_t offset) {
        return KNUTH_GOLDEN_RATIO_32BIT * offset.value();
    }

    /* Node initializer. */
    private static void trp_node_new(uint32_t offset) {
        trp_left_set(offset, NULL);
        trp_right_set(offset, NULL);
    }

    /* Internal utility macros. */
    private static void trpn_first(uint32_t root) {
        if (root.value() != NULL.value()) {
            uint32_t tmp;
            while (!NULL.equals(tmp = trp_left_get(root)))
                root.value(tmp);
        }
    }

    private static void trpn_rotate_left(uint32_t node) {
        uint32_t right = new uint32_t().value(trp_right_get(node));
        trp_right_set(node, trp_left_get(right));
        trp_left_set(right, node);
        node.value(right);
    }

    private static void trpn_rotate_right(uint32_t node) {
        uint32_t left = new uint32_t().value(trp_left_get(node));
        trp_left_set(node, trp_right_get(left));
        trp_right_set(left, node);
        node.value(left);
    }
}