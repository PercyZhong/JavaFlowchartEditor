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
} 