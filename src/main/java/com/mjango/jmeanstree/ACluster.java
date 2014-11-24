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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ACluster<T> implements ICluster<T> {
    protected static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    protected final ACluster<T> parent;
    protected final IDistanceCalculator<T> distanceCalculator;
    protected final List<IVect<T>> vects;
    protected final int k;
    protected final int dimensions;
    protected final List<ICluster<T>> subClusters;
    protected final AtomicInteger iterations;
    protected IVect<T> centroid;
    protected AtomicBoolean calculated;

    public ACluster(int dimensions, int k, IDistanceCalculator<T> distanceCalculator) {
        this(null, dimensions, k, distanceCalculator);
    }

    public ACluster(ACluster<T> parent,
                    int dimensions,
                    int k,
                    IDistanceCalculator<T> distanceCalculator) {
        this.parent = parent;
        this.distanceCalculator = distanceCalculator;
        this.vects = new ArrayList<>();
        subClusters = new CopyOnWriteArrayList<>();
        iterations = new AtomicInteger();
        this.k = k;
        this.dimensions = dimensions;
        centroid = null;
        this.calculated = new AtomicBoolean(false);
    }

    @Override
    public synchronized void add(IVect<T> vect) {
        if (vect == null) {
            throw new IllegalArgumentException("Vect must not be null");
        }
        if (vect.getDimensions() != dimensions) {
            throw new IllegalArgumentException("Vect must have " + dimensions + " dimensions");
        }
        centroid = null;
        vects.add(vect);
        calculated.set(false);
    }

    @Override
    public ICluster<T> getParent() {
        return parent;
    }

    @Override
    public synchronized int size() {
        return vects.size();
    }

    @Override
    public synchronized IVect<T> get(int index) {
        return vects.get(index);
    }

    @Override
    public IVect<T> getNearestNeighbor(IVect<T> vect) {
        return getNearestNeighbor(vect, new int[]{0});
    }

    @Override
    public IVect<T> getNearestNeighbor(IVect<T> vect, int[] compareCount) {
        Double minDistance = null;

        if (!subClusters.isEmpty()) {

            // There are sub-clusters, so find the sub-cluster with the nearest centroid, then
            // return its nearest neighbor.
            ICluster<T> nearestSubCluster = null;
            for (ICluster<T> subCluster : subClusters) {
                double distance = distanceCalculator.calculateDistance(
                        vect,
                        subCluster.getCentroid());
                compareCount[0]++;
                if (minDistance == null || distance < minDistance) {
                    minDistance = distance;
                    nearestSubCluster = subCluster;
                }
            }

            // Calling getNearestNeighbor on the nearest sub-cluster will eventually drill down
            // to the maximum depth, at which point that sub-cluster's actual members will be
            // compared.
            return nearestSubCluster != null ?
                   nearestSubCluster.getNearestNeighbor(vect, compareCount) :
                   null;
        } else {
            // There aren't any sub-clusters, so find the vect's actual nearest neighbor.
            IVect<T> nearestNeighbor = null;
            for (IVect<T> memberVect : vects) {
                double distance = distanceCalculator.calculateDistance(vect, memberVect);
                compareCount[0]++;
                if (minDistance == null || distance < minDistance) {
                    minDistance = distance;
                    nearestNeighbor = memberVect;
                }
            }
            return nearestNeighbor;
        }
    }

    @Override
    public Iterator<IVect<T>> iterator() {
        return new Iterator<IVect<T>>() {
            int index = 0;

            @Override
            public synchronized boolean hasNext() {
                return index < size();
            }

            @Override
            public synchronized IVect<T> next() {
                return vects.get(index++);
            }
        };
    }

    @Override
    public List<? extends ICluster<T>> getSubClusters() {
        return new ArrayList<>(subClusters);
    }

    @Override
    public int getK() {
        return k;
    }

    public int getIterations() {
        return iterations.get();
    }

    public int getDimensions() {
        return dimensions;
    }

    @Override
    public List<? extends ICluster<T>> calculate() {
        return calculate(true);
    }

    protected abstract ICluster<T> createSubCluster();

    protected List<? extends ICluster<T>> calculate(boolean updateCalculated) {
        if (calculated.get()) {
            return getSubClusters();
        }

        if (k > size()) {
            return null;
        }
        List<IVect<T>> means = new ArrayList<>();
        if (subClusters.isEmpty()) {
            synchronized (this) {
                for (int i = 0; i < k && i < vects.size(); i++) {
                    means.add(vects.get(i));
                }
            }
        } else {
            for (ICluster<T> cluster : subClusters) {
                means.add(cluster.getCentroid());
            }
        }

        subClusters.clear();
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        List<Future<NearestMean>> futures = new ArrayList<>();
        synchronized (this) {
            for (IVect<T> vect : vects) {
                futures.add(executor.submit(new NearestMeanTask(vect, means)));
            }
        }
        executor.shutdown();

        Map<IVect<T>, ICluster<T>> clusterMap = new HashMap<>();
        try {
            for (Future<NearestMean> future : futures) {
                NearestMean nearestMean = future.get();
                ICluster<T> cluster = clusterMap.get(nearestMean.nearestMean);
                if (cluster == null) {
                    cluster = createSubCluster();
                    clusterMap.put(nearestMean.nearestMean, cluster);
                }
                cluster.add(nearestMean.vect);

            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        boolean stable = true;
        for (IVect<T> mean : means) {
            ICluster<T> cluster = clusterMap.get(mean);
            if (cluster != null) {
                subClusters.add(cluster);
                IVect<T> clusterMean = cluster.getCentroid();
                if (!mean.equals(clusterMean)) {
                    stable = false;
                }
            }
        }
        iterations.incrementAndGet();
        if (!stable) {
            calculate(false);
        }
        if (updateCalculated) {
            calculated.set(true);
        }
        return getSubClusters();
    }

    protected class NearestMeanTask implements Callable<NearestMean> {
        private final IVect<T> vect;
        private final List<IVect<T>> means;

        public NearestMeanTask(IVect<T> vect, List<IVect<T>> means) {
            this.vect = vect;
            this.means = means;
        }

        @Override
        public NearestMean call() throws Exception {
            IVect<T> nearestMean = null;
            Double minDistance = null;
            for (IVect<T> mean : means) {
                double distance = distanceCalculator.calculateDistance(vect, mean);
                if (minDistance == null || distance < minDistance) {
                    minDistance = distance;
                    nearestMean = mean;
                }
            }
            return new NearestMean(vect, nearestMean);
        }
    }

    protected class NearestMean {
        public final IVect<T> vect;
        public final IVect<T> nearestMean;

        public NearestMean(IVect<T> vect, IVect<T> nearestMean) {
            this.vect = vect;
            this.nearestMean = nearestMean;
        }
    }
}
