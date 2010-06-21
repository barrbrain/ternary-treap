package com.cordelta.barr.david;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class Test {

    private static Iterator<String> iterator(String key) {
        return Arrays.asList(key.substring(1).split("/")).iterator();
    }

    public static void main(String[] args) {
        Node root = null;

        LinkedList<File> q = new LinkedList<File>();
        q.offer(new File("/Users/david/Documents/svn-dump-fast-export"));
        while (!q.isEmpty()) {
            File f = q.poll();
            File[] files = f.listFiles();
            if (files != null) q.addAll(Arrays.asList(files));
            root = Node.insert(root, null, iterator(f.getPath()));
        }

        root = Node.graft(root, null, iterator("/Users/david/Documents/svn-dump-fast-export.old"),
                Node.search(root, iterator("/Users/david/Documents/svn-dump-fast-export")));

        root = Node.graft(root, null, iterator("/Users/david/Documents/svn-dump-fast-export/refs"),
                Node.search(root, iterator("/Users/david/Documents/svn-dump-fast-export/.git/refs")));
        root = Node.remove(root, null, iterator("/Users/david/Documents/svn-dump-fast-export/.git"));

        Node.diff("",
                Node.search(root, iterator("/Users/david/Documents/svn-dump-fast-export.old")).middle,
                Node.search(root, iterator("/Users/david/Documents/svn-dump-fast-export")).middle,
                new Node.DeltaHandler() {

                    public void delta(String path, Node a, Node b) {
                        if (a == null) {
                            System.out.println("A " + path);
                        } else if (b == null) {
                            System.out.println("D " + path);
                        } else {
                            System.out.println("R " + path);
                        }
                    }
                });

        for (int i = 0; i < 10000; ++i) {
        }

        System.gc();

        System.out.println("Number of Nodes: " + Node.count());

        q.offer(new File("/Users/david/Documents/svn-dump-fast-export"));
        while (!q.isEmpty()) {
            File f = q.poll();
            File[] files = f.listFiles();
            if (files != null) q.addAll(Arrays.asList(files));
            System.out.println(Node.search(root, iterator(f.getPath())) != null);
        }

        System.out.println(root);
    }
}
