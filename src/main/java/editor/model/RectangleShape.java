package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.json.JSONObject;

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
        System.out.println("RectangleShape.getShape() called for " + this.label + ", using color: " + this.color);
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

    @Override
    public RectangleShape clone() throws CloneNotSupportedException {
        return (RectangleShape) super.clone();
    }

    @Override
    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        json.put("type", "rectangle");
        json.put("x", x);
        json.put("y", y);
        json.put("width", width);
        json.put("height", height);
        json.put("label", label);
        json.put("color", color.toString()); // Color to String
        return json;
    }
}
