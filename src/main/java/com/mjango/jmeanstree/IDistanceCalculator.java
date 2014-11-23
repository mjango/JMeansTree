package com.mjango.jmeanstree;

/**
 *
 */
public interface IDistanceCalculator<T> {
    double calculateDistance(IVect<T> v1, IVect<T> v2);
}
