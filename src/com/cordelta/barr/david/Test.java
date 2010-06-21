package com.cordelta.barr.david;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

public class Test {

    private static Sequence<String> sequence(String key) {
        return new Sequence<String>(Arrays.asList(key.substring(1).split("/")).iterator());
    }

    public static void main(String[] args) {
        Node root = null;

        LinkedList<File> q = new LinkedList<File>();
        q.offer(new File("/Users/david/Documents/svn-dump-fast-export"));
        while (!q.isEmpty()) {
            File f = q.poll();
            File[] files = f.listFiles();
            if (files != null) q.addAll(Arrays.asList(files));
            root = Node.insert(root, sequence(f.getPath()));
        }

        root = Node.graft(root, sequence("/Users/david/Documents/svn-dump-fast-export.old"),
                Node.search(root, sequence("/Users/david/Documents/svn-dump-fast-export")));

        root = Node.graft(root, sequence("/Users/david/Documents/svn-dump-fast-export/refs"),
                Node.search(root, sequence("/Users/david/Documents/svn-dump-fast-export/.git/refs")));
        root = Node.remove(root, sequence("/Users/david/Documents/svn-dump-fast-export/.git"));

        Node.diff("",
                Node.search(root, sequence("/Users/david/Documents/svn-dump-fast-export.old")).middle,
                Node.search(root, sequence("/Users/david/Documents/svn-dump-fast-export")).middle,
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

//        Random r = new Random();
//        for (int i = 0; i < 10000; ++i) {
//            root = Node.insert(root, sequence("/" + (i / 100) + "/" + r.nextInt()));
//            if (i % 100 == 99) {
//                root = Node.graft(root, sequence("/" + (i / 100 + 1)),
//                        Node.search(root, sequence("/" + (i / 100))));
//            }
//        }

        System.gc();

        System.out.println("Number of Nodes: " + Node.count());

//        q.offer(new File("/Users/david/Documents/svn-dump-fast-export"));
//        while (!q.isEmpty()) {
//            File f = q.poll();
//            File[] files = f.listFiles();
//            if (files != null) q.addAll(Arrays.asList(files));
//            System.out.println(Node.search(root, sequence(f.getPath())) != null);
//        }

        System.out.println(root);
    }
}
