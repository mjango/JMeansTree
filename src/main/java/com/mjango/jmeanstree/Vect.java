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
