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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Cluster extends ACluster<Number> {
    public Cluster(int dimensions, int k) {
        super(dimensions, k, new EuclideanDistance());
    }

    public Cluster(Cluster parent, int dimensions, int k) {
        super(parent, dimensions, k, new EuclideanDistance());
    }

    @Override
    public Cluster getParent() {
        return (Cluster) parent;
    }

    @Override
    public List<Cluster> getSubClusters() {
        List<Cluster> children = new ArrayList<>();
        for (ICluster<Number> child : subClusters) {
            children.add((Cluster) child);
        }
        return children;
    }

    @Override
    public Vect getCentroid() {

        if (centroid == null) {
            double[] data = new double[dimensions];
            for (IVect<Number> vect : vects) {
                for (int i = 0; i < dimensions; i++) {
                    data[i] += vect.get(i).doubleValue();
                }
            }
            int size = size();
            for (int i = 0; i < dimensions; i++) {
                data[i] /= size;
            }
            centroid = new Vect(data);
        }
        return (Vect) centroid;
    }

    @Override
    protected Cluster createSubCluster() {
        return new Cluster(this, dimensions, k);
    }
}
