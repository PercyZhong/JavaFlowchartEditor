package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class HexagonShape extends FlowchartShape {
    private Polygon hexagon;

    public HexagonShape(double x, double y, double width, double height, String label) {
        super(x, y, width, height, label);
        hexagon = new Polygon();
        updateHexagonPoints();
        hexagon.setFill(color);
        hexagon.setStroke(Color.BLACK);
    }

    private void updateHexagonPoints() {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double radius = Math.min(width, height) / 2;

        hexagon.getPoints().clear();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i); // 六边形每个角间隔60度
            hexagon.getPoints().add(centerX + radius * Math.cos(angle));
            hexagon.getPoints().add(centerY + radius * Math.sin(angle));
        }
    }

    @Override
    public Shape getShape() {
        updateHexagonPoints(); // 每次获取图形时更新顶点，以反映位置和大小变化
        hexagon.setFill(color);
        hexagon.setStroke(selected ? Color.RED : Color.BLACK);
        return hexagon;
    }

    @Override
    public boolean contains(double px, double py) {
        return hexagon.contains(px, py);
    }

    @Override
    public HexagonShape clone() throws CloneNotSupportedException {
        return (HexagonShape) super.clone();
    }
} 