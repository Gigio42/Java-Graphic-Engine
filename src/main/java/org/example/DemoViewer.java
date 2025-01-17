package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class DemoViewer {
    private static List<Triangle> tris;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // Slider horizontal
        JSlider horizontalSlider = new JSlider(0, 360, 180);
        pane.add(horizontalSlider, BorderLayout.SOUTH);

        // Slider vertical
        JSlider verticalSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(verticalSlider, BorderLayout.EAST);

        // Inflate button
        JButton inflateButton = new JButton("Inflate");
        inflateButton.addActionListener(e -> {
            tris = inflate(tris);
            frame.repaint();
        });
        pane.add(inflateButton, BorderLayout.NORTH);

        // Scaling factor
        double scale = 100.0;

        // Cube
        tris = new ArrayList<>();
        tris.add(new Triangle(new Vertex(1 * scale, 1 * scale, 1 * scale), new Vertex(-1 * scale, 1 * scale, 1 * scale), new Vertex(-1 * scale, -1 * scale, 1 * scale), Color.WHITE));
        tris.add(new Triangle(new Vertex(1 * scale, 1 * scale, 1 * scale), new Vertex(-1 * scale, -1 * scale, 1 * scale), new Vertex(1 * scale, -1 * scale, 1 * scale), Color.WHITE));

        tris.add(new Triangle(new Vertex(1 * scale, 1 * scale, -1 * scale), new Vertex(-1 * scale, 1 * scale, -1 * scale), new Vertex(-1 * scale, -1 * scale, -1 * scale), Color.RED));
        tris.add(new Triangle(new Vertex(1 * scale, 1 * scale, -1 * scale), new Vertex(-1 * scale, -1 * scale, -1 * scale), new Vertex(1 * scale, -1 * scale, -1 * scale), Color.RED));

        tris.add(new Triangle(new Vertex(1 * scale, 1 * scale, 1 * scale), new Vertex(-1 * scale, 1 * scale, 1 * scale), new Vertex(-1 * scale, 1 * scale, -1 * scale), Color.GREEN));
        tris.add(new Triangle(new Vertex(1 * scale, 1 * scale, 1 * scale), new Vertex(-1 * scale, 1 * scale, -1 * scale), new Vertex(1 * scale, 1 * scale, -1 * scale), Color.GREEN));

        tris.add(new Triangle(new Vertex(1 * scale, -1 * scale, 1 * scale), new Vertex(-1 * scale, -1 * scale, 1 * scale), new Vertex(-1 * scale, -1 * scale, -1 * scale), Color.BLUE));
        tris.add(new Triangle(new Vertex(1 * scale, -1 * scale, 1 * scale), new Vertex(-1 * scale, -1 * scale, -1 * scale), new Vertex(1 * scale, -1 * scale, -1 * scale), Color.BLUE));

        tris.add(new Triangle(new Vertex(1 * scale, 1 * scale, 1 * scale), new Vertex(1 * scale, -1 * scale, 1 * scale), new Vertex(1 * scale, -1 * scale, -1 * scale), Color.YELLOW));
        tris.add(new Triangle(new Vertex(1 * scale, 1 * scale, 1 * scale), new Vertex(1 * scale, -1 * scale, -1 * scale), new Vertex(1 * scale, 1 * scale, -1 * scale), Color.YELLOW));

        tris.add(new Triangle(new Vertex(-1 * scale, 1 * scale, 1 * scale), new Vertex(-1 * scale, -1 * scale, 1 * scale), new Vertex(-1 * scale, -1 * scale, -1 * scale), Color.CYAN));
        tris.add(new Triangle(new Vertex(-1 * scale, 1 * scale, 1 * scale), new Vertex(-1 * scale, -1 * scale, -1 * scale), new Vertex(-1 * scale, 1 * scale, -1 * scale), Color.CYAN));

        // Display
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Buffered image
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                // Depth buffer
                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

                double heading = Math.toRadians(horizontalSlider.getValue());
                Matrix3 headingTransform = new Matrix3(new double[]{
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                });
                double pitch = Math.toRadians(verticalSlider.getValue());
                Matrix3 pitchTransform = new Matrix3(new double[]{
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                });
                Matrix3 transform = headingTransform.multiply(pitchTransform);

                g2.setColor(Color.ORANGE);

                for (Triangle t : tris) {
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);

                    v1.x += getWidth() / 2;
                    v1.y += getHeight() / 2;
                    v2.x += getWidth() / 2;
                    v2.y += getHeight() / 2;
                    v3.x += getWidth() / 2;
                    v3.y += getHeight() / 2;

                    // Define vectors ab and ac
                    Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
                    Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);

                    // calculate normal vector
                    Vertex norm = new Vertex(
                            ab.y * ac.z - ab.z * ac.y,
                            ab.z * ac.x - ab.x * ac.z,
                            ab.x * ac.y - ab.y * ac.x
                    );
                    double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                    norm.x /= normalLength;
                    norm.y /= normalLength;
                    norm.z /= normalLength;

                    // compute dot product of light direction and normal vector
                    double angleCos = Math.abs(norm.z);

                    // compute rectangular bounds for triangle
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

                    // Calculate barycentric coordinates to determine if point is inside triangle
                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                            double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                            double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;

                            // If point is inside triangle, draw pixel
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                                int zIndex = y * img.getWidth() + x;
                                // If pixel is closer to camera, draw pixel. Color pixel with shade
                                if (zBuffer[zIndex] < depth) {
                                    Color shadedColor = getShade(t.color, angleCos);
                                    img.setRGB(x, y, shadedColor.getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }

                    // Set stroke to make the edge line thicker
                    g2.setStroke(new BasicStroke(1));

                    // Draw the edges of the triangle
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();
                    g2.draw(path);
                }

                // Draw the filled triangle
                g2.drawImage(img, 0, 0, null);
            }
        };

        // Listeners
        horizontalSlider.addChangeListener(e -> renderPanel.repaint());
        verticalSlider.addChangeListener(e -> renderPanel.repaint());

        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static List<Triangle> inflate(List<Triangle> tris) {
        List<Triangle> result = new ArrayList<>();
        for (Triangle t : tris) {
            Vertex m1 = new Vertex((t.v1.x + t.v2.x) / 2, (t.v1.y + t.v2.y) / 2, (t.v1.z + t.v2.z) / 2);
            Vertex m2 = new Vertex((t.v2.x + t.v3.x) / 2, (t.v2.y + t.v3.y) / 2, (t.v2.z + t.v3.z) / 2);
            Vertex m3 = new Vertex((t.v1.x + t.v3.x) / 2, (t.v1.y + t.v3.y) / 2, (t.v1.z + t.v3.z) / 2);
            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }
        for (Triangle t : result) {
            for (Vertex v : new Vertex[]{t.v1, t.v2, t.v3}) {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= l;
                v.y /= l;
                v.z /= l;
            }
        }
        return result;
    }

    public static Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1 / 2.4);
        int green = (int) Math.pow(greenLinear, 1 / 2.4);
        int blue = (int) Math.pow(blueLinear, 1 / 2.4);

        return new Color(red, green, blue);
    }
}

class Vertex {
    double x, y, z;

    Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

class Triangle {
    Vertex v1, v2, v3;
    Color color;

    Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }
}

class Matrix3 {
    double[] values;

    Matrix3(double[] values) {
        this.values = values;
    }

    Matrix3 multiply(Matrix3 other) {
        double[] result = new double[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                for (int i = 0; i < 3; i++) {
                    result[row * 3 + col] += this.values[row * 3 + i] * other.values[i * 3 + col];
                }
            }
        }
        return new Matrix3(result);
    }

    Vertex transform(Vertex in) {
        return new Vertex(
                in.x * values[0] + in.y * values[3] + in.z * values[6],
                in.x * values[1] + in.y * values[4] + in.z * values[7],
                in.x * values[2] + in.y * values[5] + in.z * values[8]
        );
    }
}
