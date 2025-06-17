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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CanvasPane extends Pane {
    private List<FlowchartShape> shapes = new ArrayList<>();
    private List<FlowchartShape> selectedShapes = new ArrayList<>();
    private List<FlowchartShape> clipboard = new ArrayList<>();
    private String currentTool = "矩形";
    private double dragOffsetX, dragOffsetY;
    private double selectStartX, selectStartY;
    private Rectangle selectionRect = new Rectangle();
    private boolean isSelecting = false;
    private PropertyPanel propertyPanel;
    private double mousePressedX, mousePressedY;

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
        setStyle("-fx-background-color: #f8f8f8;");
        setPrefSize(900, 800);

        selectionRect.setFill(Color.web("#3399ff", 0.2));
        selectionRect.setStroke(Color.web("#3399ff"));
        selectionRect.setVisible(false);
        getChildren().add(selectionRect);

        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseReleased(this::handleMouseReleased);

        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.C) copySelected();
            if (e.isControlDown() && e.getCode() == KeyCode.V) paste();
            if (e.isControlDown() && e.getCode() == KeyCode.Z) undo();
            if (e.isControlDown() && e.getCode() == KeyCode.Y) redo();
        });

        redraw();
    }

    public void setCurrentTool(String tool) {
        this.currentTool = tool;
    }

    public void setPropertyPanel(PropertyPanel panel) {
        this.propertyPanel = panel;
    }

    private void handleMousePressed(MouseEvent event) {
        requestFocus();
        double x = event.getX(), y = event.getY();
        mousePressedX = x;
        mousePressedY = y;
        boolean ctrl = event.isControlDown();
        boolean hit = false;
        for (FlowchartShape shape : shapes) {
            if (shape.contains(x, y)) {
                hit = true;
                if (ctrl) {
                    if (selectedShapes.contains(shape)) {
                        selectedShapes.remove(shape);
                        shape.setSelected(false);
                    } else {
                        selectedShapes.add(shape);
                        shape.setSelected(true);
                    }
                } else {
                    selectedShapes.clear();
                    selectedShapes.add(shape);
                    for (FlowchartShape s : shapes) s.setSelected(false);
                    shape.setSelected(true);
                }
                // 记录拖动起点
                dragOriginMouseX = x;
                dragOriginMouseY = y;
                dragStartX.clear();
                dragStartY.clear();
                for (FlowchartShape s : selectedShapes) {
                    dragStartX.add(s.getX());
                    dragStartY.add(s.getY());
                }
                isDraggingShapes = true;
                break;
            }
        }
        if (!hit) {
            // 框选准备
            selectStartX = x;
            selectStartY = y;
            selectionRect.setX(x);
            selectionRect.setY(y);
            selectionRect.setWidth(0);
            selectionRect.setHeight(0);
            selectionRect.setVisible(false);
            selectedShapes.clear();
            for (FlowchartShape s : shapes) s.setSelected(false);
            isDraggingShapes = false;
        }
        redraw();
    }

    private void handleMouseDragged(MouseEvent event) {
        double x = event.getX(), y = event.getY();
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
        System.out.println("currentTool = [" + currentTool + "]");
        System.out.println("\"矩形\".equals(currentTool): " + "矩形".equals(currentTool));
        System.out.println("\"椭圆\".equals(currentTool): " + "椭圆".equals(currentTool));
        System.out.println("\"菱形\".equals(currentTool): " + "菱形".equals(currentTool));
        double x = event.getX(), y = event.getY();
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
            // 框选时不新建图形
        } else if (selectedShapes.isEmpty()) {
            // 判断是否为单击（而不是拖动）
            if (!"选择".equals(currentTool) && Math.abs(x - mousePressedX) < 5 && Math.abs(y - mousePressedY) < 5) {
                // 新建图形
                FlowchartShape shape = null;
                if ("矩形".equals(currentTool)) {
                    shape = new RectangleShape(x, y, 100, 60, "矩形");
                } else if ("椭圆".equals(currentTool)) {
                    shape = new EllipseShape(x, y, 100, 60, "椭圆");
                } else if ("菱形".equals(currentTool)) {
                    shape = new DiamondShape(x, y, 100, 60, "菱形");
                }
                System.out.println("shape after if-else = " + shape);
                if (shape != null) {
                    System.out.println("shape = " + shape);
                    executeCommand(new AddShapeCommand(shapes, shape));
                    selectedShapes.clear();
                    selectedShapes.add(shape);
                    shape.setSelected(true);
                    if (propertyPanel != null) propertyPanel.showShape(shape);
                }
            }
        }
        isDraggingShapes = false;
        redraw();
    }

    public void redraw() {
        getChildren().removeIf(n -> n instanceof Shape || n instanceof Text);
        for (FlowchartShape shape : shapes) {
            Shape fxShape = shape.getShape();
            getChildren().add(fxShape);
            Text text = new Text(shape.getX() + 20, shape.getY() + shape.getHeight() / 2, shape.getLabel());
            getChildren().add(text);
        }
        if (selectionRect.isVisible() && !getChildren().contains(selectionRect)) {
            getChildren().add(selectionRect);
        }
    }

    // 复制
    private void copySelected() {
        clipboard.clear();
        for (FlowchartShape s : selectedShapes) {
            clipboard.add(cloneShape(s));
        }
    }

    // 粘贴
    private void paste() {
        for (FlowchartShape s : clipboard) {
            FlowchartShape newShape = cloneShape(s);
            newShape.setX(newShape.getX() + 20);
            newShape.setY(newShape.getY() + 20);
            executeCommand(new AddShapeCommand(shapes, newShape));
        }
        redraw();
    }

    // 克隆
    private FlowchartShape cloneShape(FlowchartShape s) {
        if (s instanceof RectangleShape) {
            RectangleShape r = (RectangleShape) s;
            RectangleShape copy = new RectangleShape(r.getX(), r.getY(), r.getWidth(), r.getHeight(), r.getLabel());
            copy.setColor(r.getColor());
            return copy;
        } else if (s instanceof EllipseShape) {
            EllipseShape e = (EllipseShape) s;
            EllipseShape copy = new EllipseShape(e.getX(), e.getY(), e.getWidth(), e.getHeight(), e.getLabel());
            copy.setColor(e.getColor());
            return copy;
        } else if (s instanceof DiamondShape) {
            DiamondShape d = (DiamondShape) s;
            DiamondShape copy = new DiamondShape(d.getX(), d.getY(), d.getWidth(), d.getHeight(), d.getLabel());
            copy.setColor(d.getColor());
            return copy;
        }
        return null;
    }

    // 撤销重做
    public void executeCommand(Command cmd) {
        System.out.println("executeCommand called");
        System.out.println("shapes.size() = " + shapes.size());
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
        redraw();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
            redraw();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
            redraw();
        }
    }

    // 网格吸附
    private double snapToGrid(double value) {
        return Math.round(value / GRID_SIZE) * GRID_SIZE;
    }
}