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

package com.mjango.jmeanstree.demo;

import com.mjango.jmeanstree.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class ClusterDemo {
    private final Panel panel;
    private final JFrame frame;
    private KMeansTree<Number> tree;

    public ClusterDemo() {
        panel = new Panel(8);
        frame = new JFrame();
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        tree = null;
        generateNewClusterSpace();
    }

    public void generateNewClusterSpace() {
        tree = new KMeansTree<>(new Cluster(2, 6), 10);
        for (int i = 0; i < 20000; i++) {
            tree.add(new Vect(Math.random(), Math.random()));
        }
        tree.calculate();
        panel.setCluster((Cluster) tree.getRoot());
    }

    public void printNearestNeighbor(int x, int y) {
        if (tree == null) {
            return;
        }

        double width = panel.getWidth();
        double height = panel.getHeight();

        double d1 = x / width;
        double d2 = y / height;

        int[] comparisonCount = new int[]{0};
        IVect<Number> nearestNeighbor = tree.getNearestNeighbor(new Vect(d1, d2), comparisonCount);
        System.out.println("x=" + x + ", y=" + y + ", nearest x=" +
                           String.format("%.4f", width * nearestNeighbor.get(0).doubleValue()) +
                           ", nearest y=" +
                           String.format("%.4f", height * nearestNeighbor.get(1).doubleValue()) +
                           ", total comparisons=" + comparisonCount[0]);
    }

    public void show() {
        frame.setSize(640, 480);
        frame.setVisible(true);
    }

    private class Panel extends JPanel {
        private Cluster cluster;
        private Color[] colors;
        private final int glyphSize;
        private final int halfGlyphSize;
        private BufferedImage image;

        public Panel(int glyphSize) {
            this.glyphSize = glyphSize;
            halfGlyphSize = glyphSize / 2;
            cluster = null;
            image = null;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1:
                            printNearestNeighbor(e.getX(), e.getY());
                            break;
                        case MouseEvent.BUTTON3:
                            generateNewClusterSpace();
                            break;
                        default:
                            break;
                    }
                }
            });
            addMouseWheelListener(new MouseAdapter() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (cluster == null) {
                        return;
                    }
                    if (e.getWheelRotation() > 0) {
                        // mouse moved down
                        Cluster parent = cluster.getParent();
                        if (parent != null) {
                            setCluster(parent);
                        }
                    } else {
                        // mouse moved up
                        Cluster nearest = getNearestSubCluster(e.getX(), e.getY());
                        if (nearest != null) {
                            nearest.calculate();
                            setCluster(nearest);
                        }
                    }

                }
            });
        }

        public Cluster getNearestSubCluster(int x, int y) {
            Vect vect = new Vect(((double) x) / getWidth(), ((double) y) / getHeight());
            Cluster nearest = null;
            Double minDistance = null;
            EuclideanDistance distanceCalculator = new EuclideanDistance();
            for (Cluster subCluster : cluster.getSubClusters()) {
                double distance = distanceCalculator.calculateDistance(vect,
                                                                       subCluster.getCentroid());
                if (minDistance == null || distance < minDistance) {
                    minDistance = distance;
                    nearest = subCluster;
                }
            }
            return nearest;
        }

        public void setCluster(Cluster cluster) {
            this.cluster = cluster;
            colors = new Color[cluster.getK()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = randomColor();
            }
            image = null;
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            if (image == null ||
                image.getWidth() != getWidth() ||
                image.getHeight() != getHeight()) {
                image = createKMeansImage(getWidth(), getHeight());
            }
            g.drawImage(image, 0, 0, null);
        }

        private BufferedImage createKMeansImage(int width, int height) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            List<Cluster> subClusters = cluster.getSubClusters();
            for (int i = 0; i < subClusters.size(); i++) {
                g.setColor(colors[i]);
                Cluster cluster = subClusters.get(i);
                IVect<Number> centroid = cluster.getCentroid();
                int x = (int) (centroid.get(0).doubleValue() * width);
                int y = (int) (centroid.get(1).doubleValue() * height);
                g.drawRect(x - halfGlyphSize, y - halfGlyphSize, glyphSize, glyphSize);
                for (IVect<Number> vect : cluster) {
                    x = (int) (vect.get(0).doubleValue() * width);
                    y = (int) (vect.get(1).doubleValue() * height);
                    g.drawLine(x - halfGlyphSize, y, x + halfGlyphSize, y);
                    g.drawLine(x, y - halfGlyphSize, x, y + halfGlyphSize);
                }
            }
            g.dispose();
            return image;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ClusterDemo view = new ClusterDemo();
                view.show();
            }
        });

    }

    private static Color randomColor() {
        return new Color(Color.HSBtoRGB((float) Math.random(), 1, 1));
    }
}
