package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public abstract class FlowchartShape implements Cloneable {
    protected double x, y, width, height;
    protected Color color = Color.LIGHTBLUE;
    protected String label = "";
    protected boolean selected = false;

    public FlowchartShape(double x, double y, double width, double height, String label) {
        this.x = x; this.y = y; this.width = width; this.height = height; this.label = label;
    }

    public abstract Shape getShape();
    public abstract boolean contains(double px, double py);

    // getter/setter
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public Color getColor() { return color; }
    public String getLabel() { return label; }
    public boolean isSelected() { return selected; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }
    public void setColor(Color color) { this.color = color; }
    public void setLabel(String label) { this.label = label; }
    public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    public FlowchartShape clone() throws CloneNotSupportedException {
        FlowchartShape cloned = (FlowchartShape) super.clone();
        // 对于基本类型，super.clone() 已经足够。
        // 如果有复杂对象，需要在这里进行深拷贝。
        return cloned;
    }
}