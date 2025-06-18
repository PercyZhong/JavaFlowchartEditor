package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;
import org.json.JSONObject;

public class EllipseShape extends FlowchartShape {
    private Ellipse ellipse;

    public EllipseShape(double x, double y, double width, double height, String label) {
        super(x, y, width, height, label);
        ellipse = new Ellipse(x + width / 2, y + height / 2, width / 2, height / 2);
        ellipse.setFill(color);
        ellipse.setStroke(Color.BLACK);
    }

    public EllipseShape(double x, double y, double width, double height, String label, String link) {
        this(x, y, width, height, label);
        this.link = link;
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

    @Override
    public EllipseShape clone() throws CloneNotSupportedException {
        return (EllipseShape) super.clone();
    }

    @Override
    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        json.put("type", "ellipse");
        json.put("x", x);
        json.put("y", y);
        json.put("width", width);
        json.put("height", height);
        json.put("label", label);
        json.put("color", color.toString());
        json.put("link", link);
        return json;
    }

    @Override
    protected void setupConnectionPoints() {
        connectionPoints.clear();
        // 上
        connectionPoints.add(new ConnectionPoint(this, width / 2, 0));
        // 右
        connectionPoints.add(new ConnectionPoint(this, width, height / 2));
        // 下
        connectionPoints.add(new ConnectionPoint(this, width / 2, height));
        // 左
        connectionPoints.add(new ConnectionPoint(this, 0, height / 2));
    }
}
