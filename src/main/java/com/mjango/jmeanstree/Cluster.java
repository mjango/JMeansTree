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
