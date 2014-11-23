package com.mjango.jmeanstree.demo;

import com.mjango.jmeanstree.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 */
public class ClusterDemo {
    private final Panel panel;
    private final JFrame frame;

    public ClusterDemo() {
        panel = new Panel(8);
        frame = new JFrame();
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        generateNewClusterSpace();
    }

    public void generateNewClusterSpace() {
        final Cluster cluster = new Cluster(2, 10);
        for (int i = 0; i < 20000; i++) {
            cluster.add(new Vect(Math.random(), Math.random()));
        }
        cluster.calculate();
        panel.setCluster(cluster);
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
                    generateNewClusterSpace();
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
