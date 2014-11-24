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

public class KMeansTree<T> {
    private final ICluster<T> root;
    private final int maxDepth;

    public KMeansTree(ICluster<T> root, int maxDepth) {
        this.root = root;
        this.maxDepth = maxDepth;
    }

    public void add(IVect<T> vect) {
        root.add(vect);
    }

    public void calculate() {
        calculate(root, 1, maxDepth);
    }

    public int getK() {
        return root.getK();
    }

    public int getDimensions() {
        return root.getDimensions();
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public ICluster<T> getRoot() {
        return root;
    }

    public IVect<T> getNearestNeighbor(IVect<T> vect) {
        return root.getNearestNeighbor(vect);
    }

    public IVect<T> getNearestNeighbor(IVect<T> vect, int[] comparisonCount) {
        return root.getNearestNeighbor(vect, comparisonCount);
    }

    private void calculate(ICluster<T> cluster, int currentDepth, int maxDepth) {
        if (currentDepth < maxDepth) {
            List<? extends ICluster<T>> subClusters = cluster.calculate();
            if (subClusters != null) {
                for (ICluster<T> subCluster : subClusters) {
                    calculate(subCluster, currentDepth + 1, maxDepth);
                }
            }
        }
    }
}
