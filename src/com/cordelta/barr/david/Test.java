package com.cordelta.barr.david;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class Test {

    private static Iterator<String> iterator(String key) {
        return Arrays.asList(key.split("/")).iterator();
    }

    public static void main(String[] args) {
        Node root = null;

        LinkedList<File> q = new LinkedList<File>();
        q.offer(new File("/tmp"));
        while (!q.isEmpty()) {
            File f = q.poll();
            File[] files = f.listFiles();
            if (files != null) q.addAll(Arrays.asList(files));
            root = Node.insert(root, null, iterator(f.getPath()));
        }

//        for (int i = 0; i < 10000; ++i) {
//            root = Node.insert(root, null, iterator("" + i));
//        }

        System.gc();

        System.out.println("Number of Nodes: " + Node.count());

        q.offer(new File("/tmp"));
        while (!q.isEmpty()) {
            File f = q.poll();
            File[] files = f.listFiles();
            if (files != null) q.addAll(Arrays.asList(files));
            assert root != null;
            System.out.println(root.search(iterator(f.getPath())));
        }

        System.out.println(root);
    }
}
