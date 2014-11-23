package com.mjango.jmeanstree;

import java.util.Arrays;
import java.util.Iterator;

/**
 *
 */
public class Vect implements IVect<Number> {
    private final double[] data;
    private final int hashCode;

    public Vect(double... data) {
        this.data = new double[data.length];
        hashCode = Arrays.hashCode(data);
        System.arraycopy(data, 0, this.data, 0, getDimensions());
    }

    @Override
    public Number get(int index) {
        return data[index];
    }

    @Override
    public int getDimensions() {
        return data.length;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public Iterator<Number> iterator() {
        return new Iterator<Number>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < data.length;
            }

            @Override
            public Number next() {
                return data[index++];
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Vect && Arrays.equals(data, ((Vect) o).data);
    }

    @Override
    public String toString() {
        return "Vect{" +
               "data=" + Arrays.toString(data) +
               '}';
    }
}
