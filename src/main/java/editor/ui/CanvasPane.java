package editor.ui;

import editor.model.*;
import editor.action.*;
import javafx.scene.layout.Pane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.input.TransferMode;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ClipboardContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CanvasPane extends Pane {
    private List<FlowchartShape> shapes = new ArrayList<>();
    private List<FlowchartShape> selectedShapes = new ArrayList<>();
    private List<FlowchartShape> clipboard = new ArrayList<>();
    private String currentTool = "选择"; // 重新引入，用于顶部工具栏选择的图形类型
    private double dragOffsetX, dragOffsetY;
    private double selectStartX, selectStartY;
    private Rectangle selectionRect = new Rectangle();
    private boolean isSelecting = false;
    private PropertyPanel propertyPanel;
    private double mousePressedX, mousePressedY;

    // 拖动创建图形相关的变量
    private boolean isDrawingNewShape = false;
    private javafx.scene.shape.Shape tempDrawingShape; // 临时绘制的 JavaFX 图形

    // 撤销重做栈
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    private static final int GRID_SIZE = 20;
    // 记录拖动前每个选中图形的初始位置
    private List<Double> dragStartX = new ArrayList<>();
    private List<Double> dragStartY = new ArrayList<>();
    private double dragOriginMouseX, dragOriginMouseY;
    private boolean isDraggingShapes = false;

    public CanvasPane() {
        setStyle("-fx-background-color: #F8F8F8; -fx-border-color: #D0D0D0; -fx-border-width: 1;");
        setupMouseHandlers();
        setupKeyHandlers();
        setupDragAndDropHandlers(); // New method for drag and drop

        selectionRect.setStroke(Color.BLUE);
        selectionRect.setFill(Color.LIGHTBLUE.deriveColor(1, 1, 1, 0.3));
        selectionRect.getStrokeDashArray().addAll(5.0, 5.0);
        selectionRect.setVisible(false);
        getChildren().add(selectionRect);
    }

    public void setCurrentTool(String tool) {
        this.currentTool = tool;
        isDrawingNewShape = false; // 重置绘图状态
        if (tempDrawingShape != null) {
            getChildren().remove(tempDrawingShape);
            tempDrawingShape = null;
        }
    }

    private void setupMouseHandlers() {
        this.setOnMousePressed(this::handleMousePressed);
        this.setOnMouseDragged(this::handleMouseDragged);
        this.setOnMouseReleased(this::handleMouseReleased);
    }

    private void setupKeyHandlers() {
        this.setFocusTraversable(true);
        this.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                copySelectedShapes();
            } else if (event.isControlDown() && event.getCode() == KeyCode.V) {
                pasteShapes();
            } else if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                undo();
            } else if (event.isControlDown() && event.getCode() == KeyCode.Y) {
                redo();
            } else if (event.getCode() == KeyCode.DELETE) {
                deleteSelectedShapes();
            }
        });
    }

    private void setupDragAndDropHandlers() {
        this.setOnDragOver(event -> {
            // 仅接受来自 ShapeLibraryPanel 的拖放
            if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String shapeType = db.getString();
                double x = event.getX();
                double y = event.getY();

                FlowchartShape newShape = null;
                switch (shapeType) {
                    case "矩形":
                        newShape = new RectangleShape(x, y, 100, 60, "处理");
                        break;
                    case "椭圆":
                        newShape = new EllipseShape(x, y, 100, 60, "开始/结束");
                        break;
                    case "菱形":
                        newShape = new DiamondShape(x, y, 100, 60, "判定");
                        break;
                    case "平行四边形":
                        newShape = new ParallelogramShape(x, y, 100, 60, "输入/输出");
                        break;
                }

                if (newShape != null) {
                    executeCommand(new AddShapeCommand(shapes, newShape));
                    selectedShapes.clear();
                    selectedShapes.add(newShape);
                    newShape.setSelected(true);
                    if (propertyPanel != null) propertyPanel.showShape(newShape);
                    redraw();
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void setPropertyPanel(PropertyPanel panel) {
        this.propertyPanel = panel;
    }

    private void handleMousePressed(MouseEvent event) {
        mousePressedX = event.getX();
        mousePressedY = event.getY();
        isDraggingShapes = false;
        isDrawingNewShape = false; // 默认不处于绘制新图形状态

        // 如果当前工具不是"选择"，则准备绘制新图形
        if (!"选择".equals(currentTool)) {
            isDrawingNewShape = true;
            tempDrawingShape = createTempJavaFXShape(currentTool, mousePressedX, mousePressedY);
            if (tempDrawingShape != null) {
                getChildren().add(tempDrawingShape);
            }
            return; // 不再执行下面的选择和拖动逻辑
        }

        // 先处理点击选中逻辑 (只有当 currentTool 是"选择"时才执行)
        FlowchartShape clickedShape = null;
        for (int i = shapes.size() - 1; i >= 0; i--) { // 从上层开始检测
            FlowchartShape s = shapes.get(i);
            if (s.contains(event.getX(), event.getY())) {
                clickedShape = s;
                break;
            }
        }

        if (clickedShape != null) {
            if (!selectedShapes.contains(clickedShape)) {
                selectedShapes.forEach(s -> s.setSelected(false));
                selectedShapes.clear();
                selectedShapes.add(clickedShape);
                clickedShape.setSelected(true);
            } else if (event.isShiftDown()) { // 按住 Shift 可以取消选择已选中的图形
                clickedShape.setSelected(false);
                selectedShapes.remove(clickedShape);
            }
            isDraggingShapes = true; // 准备拖动已选中的图形
            dragOriginMouseX = event.getX();
            dragOriginMouseY = event.getY();
            dragStartX.clear();
            dragStartY.clear();
            for (FlowchartShape s : selectedShapes) {
                dragStartX.add(s.getX());
                dragStartY.add(s.getY());
            }
        } else { // 点击空白处，清除所有选中
            selectedShapes.forEach(s -> s.setSelected(false));
            selectedShapes.clear();
            isSelecting = true;
            selectStartX = event.getX();
            selectStartY = event.getY();
            selectionRect.setX(selectStartX);
            selectionRect.setY(selectStartY);
            selectionRect.setWidth(0);
            selectionRect.setHeight(0);
            selectionRect.setVisible(true);
        }
        if (propertyPanel != null) {
            propertyPanel.showShape(selectedShapes.isEmpty() ? null : selectedShapes.get(selectedShapes.size() - 1));
        }
        redraw();
    }

    private void handleMouseDragged(MouseEvent event) {
        double x = event.getX(), y = event.getY();

        // 拖动创建新图形
        if (isDrawingNewShape && tempDrawingShape != null) {
            double startX = mousePressedX;
            double startY = mousePressedY;
            double endX = x;
            double endY = y;

            double minX = Math.min(startX, endX);
            double minY = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            updateTempJavaFXShape(tempDrawingShape, currentTool, minX, minY, width, height);
            return; // 不再执行下面的拖动/框选逻辑
        }

        // 拖动选中图形
        if (isDraggingShapes && !selectedShapes.isEmpty()) {
            double dx = snapToGrid(x - dragOriginMouseX);
            double dy = snapToGrid(y - dragOriginMouseY);
            for (int i = 0; i < selectedShapes.size(); i++) {
                FlowchartShape s = selectedShapes.get(i);
                s.setX(dragStartX.get(i) + dx);
                s.setY(dragStartY.get(i) + dy);
            }
            redraw();
            return;
        }
        // 只有拖动距离大于一定阈值时才进入框选
        if (!isSelecting && Math.abs(x - selectStartX) > 5 && Math.abs(y - selectStartY) > 5) {
            isSelecting = true;
            selectionRect.setVisible(true);
        }
        if (isSelecting) {
            double minX = Math.min(selectStartX, x);
            double minY = Math.min(selectStartY, y);
            double w = Math.abs(x - selectStartX);
            double h = Math.abs(y - selectStartY);
            selectionRect.setX(minX);
            selectionRect.setY(minY);
            selectionRect.setWidth(w);
            selectionRect.setHeight(h);
        }
        redraw();
    }

    private void handleMouseReleased(MouseEvent event) {
        double x = event.getX(), y = event.getY();

        // 完成新图形的绘制
        if (isDrawingNewShape && tempDrawingShape != null) {
            getChildren().remove(tempDrawingShape); // 移除临时图形
            double startX = mousePressedX;
            double startY = mousePressedY;
            double endX = x;
            double endY = y;

            double actualX = Math.min(startX, endX);
            double actualY = Math.min(startY, endY);
            double actualWidth = Math.abs(endX - startX);
            double actualHeight = Math.abs(endY - startY);

            // 确保最小尺寸
            double minSize = 20.0; // 定义最小尺寸
            if (actualWidth < minSize) actualWidth = minSize;
            if (actualHeight < minSize) actualHeight = minSize;

            FlowchartShape newShape = null;
            String label = currentTool; // 设置标签为当前工具的名称

            // 根据 currentTool 创建相应的 FlowchartShape
            switch (currentTool) {
                case "矩形":
                    newShape = new RectangleShape(actualX, actualY, actualWidth, actualHeight, label);
                    break;
                case "椭圆":
                    newShape = new EllipseShape(actualX, actualY, actualWidth, actualHeight, label);
                    break;
                case "菱形":
                    newShape = new DiamondShape(actualX, actualY, actualWidth, actualHeight, label);
                    break;
                case "平行四边形":
                    newShape = new ParallelogramShape(actualX, actualY, actualWidth, actualHeight, label);
                    break;
                case "圆形":
                    // 对于圆形，宽度和高度取最小值以确保是正圆
                    double radius = Math.min(actualWidth, actualHeight) / 2;
                    newShape = new CircleShape(actualX + radius, actualY + radius, radius * 2, radius * 2, label); // CircleShape 构造函数可能需要调整
                    break;
                case "六边形":
                    newShape = new HexagonShape(actualX, actualY, actualWidth, actualHeight, label); // HexagonShape 构造函数可能需要调整
                    break;
            }

            if (newShape != null) {
                executeCommand(new AddShapeCommand(shapes, newShape));
                selectedShapes.clear();
                selectedShapes.add(newShape);
                newShape.setSelected(true);
                if (propertyPanel != null) propertyPanel.showShape(newShape);
            }
            isDrawingNewShape = false;
            tempDrawingShape = null;
            currentTool = "选择"; // 绘制完成后，自动切换回选择工具
            redraw();
            return;
        }

        // 以下是原有的选择和拖动逻辑
        if (isSelecting) {
            double minX = selectionRect.getX();
            double minY = selectionRect.getY();
            double maxX = minX + selectionRect.getWidth();
            double maxY = minY + selectionRect.getHeight();
            selectedShapes.clear();
            for (FlowchartShape s : shapes) {
                if (s.getX() >= minX && s.getY() >= minY &&
                    s.getX() + s.getWidth() <= maxX && s.getY() + s.getHeight() <= maxY) {
                    selectedShapes.add(s);
                    s.setSelected(true);
                } else {
                    s.setSelected(false);
                }
            }
            selectionRect.setVisible(false);
            isSelecting = false;
        }
        isDraggingShapes = false;
        redraw();
    }

    // 根据工具类型创建临时 JavaFX Shape
    private javafx.scene.shape.Shape createTempJavaFXShape(String toolType, double x, double y) {
        javafx.scene.shape.Shape tempShape = null;
        switch (toolType) {
            case "矩形":
                tempShape = new Rectangle(x, y, 0, 0);
                break;
            case "椭圆":
                tempShape = new Ellipse(x, y, 0, 0);
                break;
            case "菱形":
                // 菱形需要特殊处理点，这里先简化，之后在 updateTempJavaFXShape 中精确设置
                tempShape = new Polygon(x, y, x, y, x, y, x, y);
                break;
            case "平行四边形":
                // 平行四边形需要特殊处理点
                tempShape = new Polygon(x, y, x, y, x, y, x, y);
                break;
            case "圆形":
                tempShape = new Circle(x, y, 0);
                break;
            case "六边形":
                tempShape = new Polygon(); // 初始化为空多边形
                break;
        }
        if (tempShape != null) {
            tempShape.setFill(Color.LIGHTBLUE.deriveColor(1, 1, 1, 0.5));
            tempShape.setStroke(Color.BLUE);
            tempShape.getStrokeDashArray().addAll(5.0, 5.0);
        }
        return tempShape;
    }

    // 更新临时 JavaFX Shape 的大小和位置
    private void updateTempJavaFXShape(javafx.scene.shape.Shape shape, String toolType, double x, double y, double width, double height) {
        if (shape instanceof Rectangle) {
            Rectangle rect = (Rectangle) shape;
            rect.setX(x);
            rect.setY(y);
            rect.setWidth(width);
            rect.setHeight(height);
        } else if (shape instanceof Ellipse) {
            Ellipse ellipse = (Ellipse) shape;
            ellipse.setCenterX(x + width / 2);
            ellipse.setCenterY(y + height / 2);
            ellipse.setRadiusX(width / 2);
            ellipse.setRadiusY(height / 2);
        } else if (shape instanceof Polygon) {
            Polygon polygon = (Polygon) shape;
            switch (toolType) {
                case "菱形":
                    polygon.getPoints().setAll(
                        x + width / 2, y,
                        x + width, y + height / 2,
                        x + width / 2, y + height,
                        x, y + height / 2
                    );
                    break;
                case "平行四边形":
                    polygon.getPoints().setAll(
                        x, y,
                        x + width * 0.75, y,
                        x + width, y + height,
                        x + width * 0.25, y + height
                    );
                    break;
                case "六边形":
                    // 绘制六边形，中心在 (x + width/2, y + height/2)
                    double centerX = x + width / 2;
                    double centerY = y + height / 2;
                    double hexRadius = Math.min(width, height) / 2;
                    polygon.getPoints().clear();
                    for (int i = 0; i < 6; i++) {
                        double angle = Math.toRadians(60 * i);
                        polygon.getPoints().add(centerX + hexRadius * Math.cos(angle));
                        polygon.getPoints().add(centerY + hexRadius * Math.sin(angle));
                    }
                    break;
            }
        } else if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            double radius = Math.min(width, height) / 2;
            circle.setCenterX(x + radius);
            circle.setCenterY(y + radius);
            circle.setRadius(radius);
        }
    }

    public void redraw() {
        getChildren().removeIf(n -> (n instanceof Shape && n != tempDrawingShape && n != selectionRect) || n instanceof Text);
        for (FlowchartShape shape : shapes) {
            Shape fxShape = shape.getShape();
            getChildren().add(fxShape);
            Text text = new Text(shape.getX() + 20, shape.getY() + shape.getHeight() / 2, shape.getLabel());
            getChildren().add(text);
        }
        // 确保临时绘制的图形在最顶层
        if (tempDrawingShape != null && !getChildren().contains(tempDrawingShape)) {
            getChildren().add(tempDrawingShape);
        }
        if (selectionRect.isVisible() && !getChildren().contains(selectionRect)) {
            getChildren().add(selectionRect);
        }
    }

    private void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // 执行新命令后清空 redo 栈
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            redraw();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
            redraw();
        }
    }

    // 复制
    public void copySelectedShapes() {
        clipboard.clear();
        for (FlowchartShape shape : selectedShapes) {
            try {
                // 假设 FlowchartShape 实现了 Cloneable 接口并重写了 clone 方法
                clipboard.add((FlowchartShape) shape.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    // 粘贴
    public void pasteShapes() {
        if (!clipboard.isEmpty()) {
            selectedShapes.forEach(s -> s.setSelected(false)); // 清除当前选中
            selectedShapes.clear();
            for (FlowchartShape shape : clipboard) {
                // 粘贴时稍微偏移位置
                FlowchartShape newShape = null;
                if (shape instanceof RectangleShape) {
                    newShape = new RectangleShape(shape.getX() + 10, shape.getY() + 10, shape.getWidth(), shape.getHeight(), shape.getLabel());
                } else if (shape instanceof EllipseShape) {
                    newShape = new EllipseShape(shape.getX() + 10, shape.getY() + 10, shape.getWidth(), shape.getHeight(), shape.getLabel());
                } else if (shape instanceof DiamondShape) {
                    newShape = new DiamondShape(shape.getX() + 10, shape.getY() + 10, shape.getWidth(), shape.getHeight(), shape.getLabel());
                } else if (shape instanceof ParallelogramShape) {
                    newShape = new ParallelogramShape(shape.getX() + 10, shape.getY() + 10, shape.getWidth(), shape.getHeight(), shape.getLabel());
                } else if (shape instanceof CircleShape) {
                    newShape = new CircleShape(shape.getX() + 10, shape.getY() + 10, shape.getWidth(), shape.getHeight(), shape.getLabel());
                } else if (shape instanceof HexagonShape) {
                    newShape = new HexagonShape(shape.getX() + 10, shape.getY() + 10, shape.getWidth(), shape.getHeight(), shape.getLabel());
                }

                if (newShape != null) {
                    executeCommand(new AddShapeCommand(shapes, newShape));
                    selectedShapes.add(newShape);
                    newShape.setSelected(true);
                }
            }
            redraw();
            if (propertyPanel != null) propertyPanel.showShape(selectedShapes.isEmpty() ? null : selectedShapes.get(selectedShapes.size() - 1));
        }
    }

    // 删除选中的图形
    public void deleteSelectedShapes() {
        if (!selectedShapes.isEmpty()) {
            // 从 shapes 列表中移除选中的图形
            shapes.removeAll(selectedShapes);
            selectedShapes.clear(); // 清空选中列表
            redraw(); // 重新绘制画布
            if (propertyPanel != null) propertyPanel.showShape(null); // 清空属性面板
        }
    }

    // 辅助方法：将坐标吸附到网格
    private double snapToGrid(double value) {
        return Math.round(value / GRID_SIZE) * GRID_SIZE;
    }
}