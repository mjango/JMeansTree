package com.mjango.jmeanstree;

/**
 *
 */
public interface IVect<T> extends Iterable<T> {
    int getDimensions();

    T get(int index);
}
