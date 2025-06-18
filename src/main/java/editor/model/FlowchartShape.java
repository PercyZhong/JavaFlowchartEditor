package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public abstract class FlowchartShape implements Cloneable {
    protected double x, y, width, height;
    protected Color color = Color.WHITE;
    protected String label = "";
    protected boolean selected = false;
    protected List<ConnectionPoint> connectionPoints = new ArrayList<>();
    protected List<ConnectionLine> incomingLines = new ArrayList<>();
    protected List<ConnectionLine> outgoingLines = new ArrayList<>();
    protected String link = "";

    public FlowchartShape(double x, double y, double width, double height, String label) {
        this.x = x; this.y = y; this.width = width; this.height = height; this.label = label;
        setupConnectionPoints();
    }

    protected abstract void setupConnectionPoints();

    public void updateConnectionPoints() {
        for (ConnectionPoint point : connectionPoints) {
            point.updatePosition();
        }
    }

    public void showConnectionPoints() {
        for (ConnectionPoint point : connectionPoints) {
            point.show();
        }
    }

    public void hideConnectionPoints() {
        for (ConnectionPoint point : connectionPoints) {
            point.hide();
        }
    }

    public List<ConnectionPoint> getConnectionPoints() {
        return connectionPoints;
    }

    public void addIncomingLine(ConnectionLine line) {
        incomingLines.add(line);
    }

    public void addOutgoingLine(ConnectionLine line) {
        outgoingLines.add(line);
    }

    public void removeIncomingLine(ConnectionLine line) {
        incomingLines.remove(line);
    }

    public void removeOutgoingLine(ConnectionLine line) {
        outgoingLines.remove(line);
    }

    public void updateConnectedLines() {
        for (ConnectionLine line : incomingLines) {
            line.updatePosition();
        }
        for (ConnectionLine line : outgoingLines) {
            line.updatePosition();
        }
    }

    public abstract Shape getShape();
    public abstract boolean contains(double px, double py);

    // 新增抽象方法，用于将图形数据转换为 JSON 对象
    public abstract JSONObject toJsonObject();

    // getter/setter
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public Color getColor() { return color; }
    public String getLabel() { return label; }
    public boolean isSelected() { return selected; }
    public String getLink() { return link; }

    public void setX(double x) {
        this.x = x;
        updateConnectionPoints();
        updateConnectedLines();
    }
    public void setY(double y) {
        this.y = y;
        updateConnectionPoints();
        updateConnectedLines();
    }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }
    public void setColor(Color color) {
        System.out.println("FlowchartShape.setColor() called. Shape: " + this.label + ", Old Color: " + this.color + ", New Color: " + color);
        this.color = color; 
    }
    public void setLabel(String label) { this.label = label; }
    public void setSelected(boolean selected) { this.selected = selected; }
    public void setLink(String link) { this.link = link; }

    @Override
    public FlowchartShape clone() throws CloneNotSupportedException {
        FlowchartShape cloned = (FlowchartShape) super.clone();
        cloned.link = this.link;
        return cloned;
    }
}