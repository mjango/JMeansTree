package com.mjango.jmeanstree;

/**
 *
 */
public class EuclideanDistance implements IDistanceCalculator<Number> {
    @Override
    public double calculateDistance(IVect<Number> v1, IVect<Number> v2) {
        double distance = 0;
        if (v1 == null || v2 == null) {
            return distance;
        }
        int size = Math.min(v1.getDimensions(), v2.getDimensions());
        for (int i = 0; i < size; i++) {
            double diff = v1.get(i).doubleValue() - v2.get(i).doubleValue();
            distance += diff * diff;
        }
        return Math.sqrt(distance);
    }
}
