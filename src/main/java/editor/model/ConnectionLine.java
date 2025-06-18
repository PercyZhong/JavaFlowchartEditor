package editor.model;

import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Shape;

public class ConnectionLine {
    private Shape line; // 可为Line、Polyline、QuadCurve
    private final Polygon arrow;
    private ConnectionPoint startPoint;
    private ConnectionPoint endPoint;
    private Double tempEndX = null, tempEndY = null;
    private static final double ARROW_SIZE = 10.0;

    public enum LineType { STRAIGHT, POLYLINE, CURVE }
    private LineType lineType = LineType.STRAIGHT;
    private Color color = Color.BLACK;
    private double strokeWidth = 2.0;
    private boolean arrowEnabled = true;
    private boolean selected = false;

    public ConnectionLine() {
        // 默认直线
        line = new Line();
        line.setStroke(color);
        line.setStrokeWidth(strokeWidth);
        // 创建箭头
        arrow = new Polygon();
        arrow.getPoints().addAll(
            0.0, 0.0,
            -ARROW_SIZE, -ARROW_SIZE/2,
            -ARROW_SIZE, ARROW_SIZE/2
        );
        arrow.setFill(color);
        arrow.setStroke(color);
        arrow.setStrokeWidth(1.0);
    }

    public void setLineType(LineType type) {
        if (this.lineType == type) return;
        this.lineType = type;
        // 切换线型
        Shape oldLine = this.line;
        if (type == LineType.STRAIGHT) {
            this.line = new Line();
        } else if (type == LineType.POLYLINE) {
            this.line = new Polyline();
        } else {
            this.line = new QuadCurve();
        }
        // 继承样式
        this.line.setStroke(color);
        this.line.setStrokeWidth(strokeWidth);
        // 保持选中高亮
        if (selected) {
            this.line.setStroke(Color.DODGERBLUE);
            this.line.setStrokeWidth(strokeWidth + 2);
        }
        updatePosition();
    }
    public LineType getLineType() { return lineType; }

    public void setStartPoint(ConnectionPoint point) {
        this.startPoint = point;
        updatePosition();
    }
    public void setEndPoint(ConnectionPoint point) {
        this.endPoint = point;
        this.tempEndX = null;
        this.tempEndY = null;
        updatePosition();
    }

    public void setTempEnd(double x, double y) {
        this.tempEndX = x;
        this.tempEndY = y;
        updatePosition();
    }

    public void updatePosition() {
        if (startPoint == null) return;
        double sx = startPoint.getX(), sy = startPoint.getY();
        double ex, ey;
        if (endPoint != null) {
            ex = endPoint.getX();
            ey = endPoint.getY();
        } else if (tempEndX != null && tempEndY != null) {
            ex = tempEndX;
            ey = tempEndY;
        } else {
            ex = sx;
            ey = sy;
        }
        if (lineType == LineType.STRAIGHT && line instanceof Line) {
            Line l = (Line) line;
            l.setStartX(sx);
            l.setStartY(sy);
            l.setEndX(ex);
            l.setEndY(ey);
        } else if (lineType == LineType.POLYLINE && line instanceof Polyline) {
            Polyline poly = (Polyline)line;
            poly.getPoints().clear();
            double mx = (sx + ex) / 2;
            poly.getPoints().addAll(sx, sy, mx, sy, mx, ey, ex, ey);
        } else if (lineType == LineType.CURVE && line instanceof QuadCurve) {
            QuadCurve curve = (QuadCurve)line;
            curve.setStartX(sx);
            curve.setStartY(sy);
            double ctrlX = (sx + ex) / 2;
            double ctrlY = Math.min(sy, ey) - 40;
            curve.setControlX(ctrlX);
            curve.setControlY(ctrlY);
            curve.setEndX(ex);
            curve.setEndY(ey);
        }
        // 箭头
        if ((endPoint != null || (tempEndX != null && tempEndY != null)) && arrowEnabled) {
            double angle = Math.atan2(ey - sy, ex - sx) * 180 / Math.PI;
            arrow.setTranslateX(ex);
            arrow.setTranslateY(ey);
            arrow.setRotate(angle);
        }
    }

    public Shape getLine() { return line; }
    public Polygon getArrow() { return arrow; }
    public ConnectionPoint getStartPoint() { return startPoint; }
    public ConnectionPoint getEndPoint() { return endPoint; }
    public void setColor(Color color) {
        this.color = color;
        line.setStroke(color);
        arrow.setFill(color);
        arrow.setStroke(color);
    }
    public Color getColor() { return color; }
    public void setStrokeWidth(double width) {
        this.strokeWidth = width;
        line.setStrokeWidth(width);
        arrow.setStrokeWidth(width);
    }
    public double getStrokeWidth() { return strokeWidth; }
    public void setArrowEnabled(boolean enabled) {
        this.arrowEnabled = enabled;
        arrow.setVisible(enabled);
    }
    public boolean isArrowEnabled() { return arrowEnabled; }
    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            line.setStrokeWidth(strokeWidth + 2);
            line.setStroke(Color.DODGERBLUE);
            arrow.setStroke(Color.DODGERBLUE);
            arrow.setFill(Color.DODGERBLUE);
        } else {
            line.setStrokeWidth(strokeWidth);
            line.setStroke(color);
            arrow.setStroke(color);
            arrow.setFill(color);
        }
    }
    public boolean isSelected() { return selected; }
} 