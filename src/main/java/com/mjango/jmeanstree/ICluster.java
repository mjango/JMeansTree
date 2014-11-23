package com.mjango.jmeanstree;

import java.util.List;

/**
 *
 */
public interface ICluster<T> extends Iterable<IVect<T>> {
    void add(IVect<T> vect);

    ICluster<T> getParent();

    int size();

    IVect<T> get(int index);

    IVect<T> getCentroid();

    int getK();

    List<? extends ICluster<T>> getSubClusters();

    List<? extends ICluster<T>> calculate();
}
