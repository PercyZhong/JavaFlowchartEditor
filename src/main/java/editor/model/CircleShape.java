package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import org.json.JSONObject;

public class CircleShape extends FlowchartShape {
    private Circle circle;

    public CircleShape(double x, double y, double width, double height, String label) {
        super(x, y, width, height, label);
        // Circle的x,y是中心点，而FlowchartShape是左上角。这里需要转换。
        // 为了保持和FlowchartShape的x,y,width,height一致，我们将width和height视为直径。
        double radius = Math.min(width, height) / 2;
        circle = new Circle(x + radius, y + radius, radius);
        circle.setFill(color);
        circle.setStroke(Color.BLACK);
    }

    @Override
    public Shape getShape() {
        double radius = Math.min(width, height) / 2;
        circle.setCenterX(x + radius);
        circle.setCenterY(y + radius);
        circle.setRadius(radius);
        circle.setFill(color);
        circle.setStroke(selected ? Color.RED : Color.BLACK);
        return circle;
    }

    @Override
    public boolean contains(double px, double py) {
        return circle.contains(px, py);
    }

    @Override
    public CircleShape clone() throws CloneNotSupportedException {
        return (CircleShape) super.clone();
    }

    @Override
    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        json.put("type", "circle");
        json.put("x", x);
        json.put("y", y);
        json.put("width", width);
        json.put("height", height);
        json.put("label", label);
        json.put("color", color.toString());
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