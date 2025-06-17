package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class RectangleShape extends FlowchartShape {
    private Rectangle rect;  // 持久化形状实例

    public RectangleShape(double x, double y, double width, double height, String label) {
        super(x, y, width, height, label);
        rect = new Rectangle(x, y, width, height);
        rect.setFill(color);
        rect.setStroke(Color.BLACK);
    }

    @Override
    public Shape getShape() {
        // 更新图形的位置、大小、颜色
        rect.setX(x);
        rect.setY(y);
        rect.setWidth(width);
        rect.setHeight(height);
        rect.setFill(color);
        rect.setStroke(selected ? Color.RED : Color.BLACK);
        return rect;
    }

    @Override
    public boolean contains(double px, double py) {
        return rect.contains(px, py); // 使用 JavaFX 自带的 contains 方法，准确可靠
    }
}
