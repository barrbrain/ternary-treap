package com.cordelta.barr.david;

import java.util.Iterator;

public class Sequence<T> {
    private final Iterator<T> iterator;
    private T value;

    public Sequence(Iterator<T> iterator) {
        this.iterator = iterator;
        if (iterator.hasNext()) value = iterator.next();
    }

    public T value() {
        return value;
    }

    public Sequence<T> next() {
        value = iterator.hasNext() ? iterator.next() : null;
        return this;
    }
}
