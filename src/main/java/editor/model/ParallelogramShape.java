package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import org.json.JSONObject;

public class ParallelogramShape extends FlowchartShape {
    private Polygon parallelogram;

    public ParallelogramShape(double x, double y, double width, double height, String label) {
        super(x, y, width, height, label);
        parallelogram = new Polygon(
            x, y,
            x + width * 0.75, y,
            x + width, y + height,
            x + width * 0.25, y + height
        );
        parallelogram.setFill(color);
        parallelogram.setStroke(Color.BLACK);
    }

    @Override
    public Shape getShape() {
        parallelogram.getPoints().setAll(
            x, y,
            x + width * 0.75, y,
            x + width, y + height,
            x + width * 0.25, y + height
        );
        parallelogram.setFill(color);
        parallelogram.setStroke(selected ? Color.RED : Color.BLACK);
        return parallelogram;
    }

    @Override
    public boolean contains(double px, double py) {
        return parallelogram.contains(px, py);
    }

    @Override
    public ParallelogramShape clone() throws CloneNotSupportedException {
        return (ParallelogramShape) super.clone();
    }

    @Override
    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        json.put("type", "parallelogram");
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
        // 四个顶点
    double x0 = width * 0.25, y0 = 0;
    double x1 = width, y1 = 0;
    double x2 = width * 0.75, y2 = height;
    double x3 = 0, y3 = height;
    // 上边中点
    connectionPoints.add(new ConnectionPoint(this, (x0 + x1) / 2, (y0 + y1) / 2));
    // 右边中点
    connectionPoints.add(new ConnectionPoint(this, (x1 + x2) / 2, (y1 + y2) / 2));
    // 下边中点
    connectionPoints.add(new ConnectionPoint(this, (x2 + x3) / 2, (y2 + y3) / 2));
    // 左边中点
    connectionPoints.add(new ConnectionPoint(this, (x3 + x0) / 2, (y3 + y0) / 2));
    }
} 