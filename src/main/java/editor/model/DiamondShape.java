package editor.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class DiamondShape extends FlowchartShape {
    private Polygon diamond;

    public DiamondShape(double x, double y, double width, double height, String label) {
        super(x, y, width, height, label);
        // 初始化时创建 Polygon 实例，并设置初始坐标
        diamond = new Polygon(
            x + width / 2, y,
            x + width, y + height / 2,
            x + width / 2, y + height,
            x, y + height / 2
        );
        diamond.setFill(color);
        diamond.setStroke(Color.BLACK);
    }

    @Override
    public Shape getShape() {
        // 刷新菱形的四个顶点坐标
        double cx = x + width / 2;
        double cy = y + height / 2;
        double leftX = x;
        double rightX = x + width;
        double topY = y;
        double bottomY = y + height;

        diamond.getPoints().setAll(
            cx, topY,      // 顶点
            rightX, cy,    // 右顶点
            cx, bottomY,   // 底部顶点
            leftX, cy      // 左顶点
        );
        // 更新填充色与边框色
        diamond.setFill(color);
        diamond.setStroke(selected ? Color.RED : Color.BLACK);
        return diamond;
    }

    @Override
    public boolean contains(double px, double py) {
        // 直接使用 Polygon 自带的 contains 方法
        return diamond.contains(px, py);
    }
}
