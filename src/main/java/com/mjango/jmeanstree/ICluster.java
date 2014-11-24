/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Marcus Jang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mjango.jmeanstree;

import java.util.List;

/**
 * Interface for a cluster of {@link com.mjango.jmeanstree.IVect}.  IVects assigned to IClusters
 * either manually, or based a distance measurement as determined by an
 * {@link com.mjango.jmeanstree.IDistanceCalculator}.
 */
public interface ICluster<T> extends Iterable<IVect<T>> {
    /**
     * Add an IVect to this cluster.
     *
     * @param vect IVect instance to add.
     */
    void add(IVect<T> vect);

    /**
     * Get this ICluster's parent, if one exists.  The root cluster in a K-Means tree does not have
     * a parent.
     *
     * @return this ICluster's parent, or <code>null</code> if it is a root cluster.
     */
    ICluster<T> getParent();

    /**
     * Get the number of IVects in this cluster.
     *
     * @return number of IVects in this cluster.
     */
    int size();

    /**
     * Get an IVect from this cluster.
     *
     * @param index index of IVect.
     * @return IVect at the given index.
     */
    IVect<T> get(int index);

    /**
     * Get a vector's nearest neighbor from this cluster (or one of its sub-clusters).
     *
     * @param vect vector of interest.
     * @return the vector's nearest neighbor.
     */
    IVect<T> getNearestNeighbor(IVect<T> vect);

    /**
     * Get a vector's nearest neighbor from this cluster (or one of its sub-clusters).
     *
     * @param vect         vector of interest.
     * @param compareCount a 1-element int array to track number of comparisons used in finding
     *                     the nearest neighbor.
     * @return the vector's nearest neighbor.
     */
    IVect<T> getNearestNeighbor(IVect<T> vect, int[] compareCount);

    /**
     * Get an IVect representing the centroid of this cluster.
     *
     * @return centroid of this cluster.
     */
    IVect<T> getCentroid();

    /**
     * Get K, or the expected number of sub-clusters.
     *
     * @return K.
     */
    int getK();

    /**
     * Get the vector dimensions.
     *
     * @return the vector dimensions.
     */
    int getDimensions();

    /**
     * Get the sub-clusters calculated from this cluster.  If the sub-clusters have not yet been
     * calculated, this will return an empty list.
     *
     * @return the sub-clusters from this cluster.
     */
    List<? extends ICluster<T>> getSubClusters();

    /**
     * Calculate the sub-clusters for this cluster.
     *
     * @return the calculated sub-clusters from this cluster.
     */
    List<? extends ICluster<T>> calculate();
}
