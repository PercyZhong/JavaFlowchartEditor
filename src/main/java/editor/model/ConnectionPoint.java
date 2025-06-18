package editor.model;

import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class ConnectionPoint {
    private static final double POINT_RADIUS = 5.0;
    private final Circle visualPoint;
    private final FlowchartShape parentShape;
    private final double relativeX;
    private final double relativeY;
    private boolean isVisible = false;

    public ConnectionPoint(FlowchartShape parentShape, double relativeX, double relativeY) {
        this.parentShape = parentShape;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        
        // 创建连接点的视觉表示
        this.visualPoint = new Circle(POINT_RADIUS);
        this.visualPoint.setFill(Color.WHITE);
        this.visualPoint.setStroke(Color.BLUE);
        this.visualPoint.setStrokeWidth(1.5);
        this.visualPoint.setVisible(false);
        
        updatePosition();
    }

    public void updatePosition() {
        double x = parentShape.getX() + relativeX;
        double y = parentShape.getY() + relativeY;
        visualPoint.setCenterX(x);
        visualPoint.setCenterY(y);
    }

    public void show() {
        isVisible = true;
        visualPoint.setVisible(true);
    }

    public void hide() {
        isVisible = false;
        visualPoint.setVisible(false);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public Circle getVisualPoint() {
        return visualPoint;
    }

    public double getX() {
        return visualPoint.getCenterX();
    }

    public double getY() {
        return visualPoint.getCenterY();
    }

    public FlowchartShape getParentShape() {
        return parentShape;
    }
} 