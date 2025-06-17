package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;

public class EllipseShape extends FlowchartShape {
    private Ellipse ellipse;

    public EllipseShape(double x, double y, double width, double height, String label) {
        super(x, y, width, height, label);
        ellipse = new Ellipse(x + width / 2, y + height / 2, width / 2, height / 2);
        ellipse.setFill(color);
        ellipse.setStroke(Color.BLACK);
    }

    @Override
    public Shape getShape() {
        // 更新图形位置和状态
        ellipse.setCenterX(x + width / 2);
        ellipse.setCenterY(y + height / 2);
        ellipse.setRadiusX(width / 2);
        ellipse.setRadiusY(height / 2);
        ellipse.setFill(color);
        ellipse.setStroke(selected ? Color.RED : Color.BLACK);
        return ellipse;
    }

    @Override
    public boolean contains(double px, double py) {
        return ellipse.contains(px, py); // 使用 JavaFX 的 contains 方法，位置会更精确
    }
}
