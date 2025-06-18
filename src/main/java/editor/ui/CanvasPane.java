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
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CanvasPane extends Pane {
    private List<FlowchartShape> shapes = new ArrayList<>();
    private List<FlowchartShape> selectedShapes = new ArrayList<>();
    private List<FlowchartShape> clipboard = new ArrayList<>();
    private List<ConnectionLine> connectionLines = new ArrayList<>();
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
    private boolean showGrid = true; // 是否显示网格
    private boolean snapToGridEnabled = true; // 是否启用网格对齐
    private List<javafx.scene.shape.Line> gridLines = new ArrayList<>(); // 存储网格线

    // 记录拖动前每个选中图形的初始位置
    private List<Double> dragStartX = new ArrayList<>();
    private List<Double> dragStartY = new ArrayList<>();
    private double dragOriginMouseX, dragOriginMouseY;
    private boolean isDraggingShapes = false;

    // 连接线相关变量
    private ConnectionPoint startConnectionPoint = null;
    private ConnectionLine tempConnectionLine = null;

    private boolean isConnecting = false;

    private ConnectionLine selectedLine = null;

    public CanvasPane() {
        setStyle("-fx-background-color: #F8F8F8; -fx-border-color: #D0D0D0; -fx-border-width: 1;");
        setupMouseHandlers();
        setupKeyHandlers();
        setupDragAndDropHandlers(); // New method for drag and drop
        setupGrid();

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
                // 应用网格对齐到拖放位置
                double x = snapToGridEnabled ? snapToGrid(event.getX()) : event.getX();
                double y = snapToGridEnabled ? snapToGrid(event.getY()) : event.getY();

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
        this.requestFocus();
        mousePressedX = event.getX();
        mousePressedY = event.getY();
        isDraggingShapes = false;
        isDrawingNewShape = false;

        // 优先检测是否点击了连接线
        ConnectionLine clickedLine = findConnectionLine(event.getX(), event.getY());
        if (clickedLine != null) {
            // 只允许单选线
            if (selectedLine != null) selectedLine.setSelected(false);
            selectedLine = clickedLine;
            selectedLine.setSelected(true);
            // 取消所有图形选中
            selectedShapes.forEach(s -> { s.setSelected(false); s.hideConnectionPoints(); });
            selectedShapes.clear();
            if (propertyPanel != null) propertyPanel.showLine(selectedLine);
            redraw();
            return;
        }
        // 取消线条选中
        if (selectedLine != null) {
            selectedLine.setSelected(false);
            selectedLine = null;
        }

        // 如果当前工具不是"选择"，则准备绘制新图形
        if (!"选择".equals(currentTool)) {
            isDrawingNewShape = true;
            tempDrawingShape = createTempJavaFXShape(currentTool, mousePressedX, mousePressedY);
            if (tempDrawingShape != null) {
                getChildren().add(tempDrawingShape);
            }
            return;
        }

        // 检查是否点击了连接点（只有点在连接点上才允许开始连线）
        ConnectionPoint cp = findConnectionPoint(event.getX(), event.getY());
        if (cp != null) {
            startConnectionPoint = cp;
            tempConnectionLine = new ConnectionLine();
            tempConnectionLine.setStartPoint(startConnectionPoint);
            tempConnectionLine.setTempEnd(startConnectionPoint.getX(), startConnectionPoint.getY());
            getChildren().addAll(tempConnectionLine.getLine(), tempConnectionLine.getArrow());
            isConnecting = true;
            redraw();
            return;
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
                if (!event.isShiftDown()) { // 如果没有按住Shift，清除之前的选择
                    selectedShapes.forEach(s -> {
                        s.setSelected(false);
                        s.hideConnectionPoints();
                    });
                    selectedShapes.clear();
                }
                selectedShapes.add(clickedShape);
                clickedShape.setSelected(true);
                clickedShape.showConnectionPoints();
            } else if (event.isShiftDown()) { // 按住 Shift 可以取消选择已选中的图形
                clickedShape.setSelected(false);
                clickedShape.hideConnectionPoints();
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
            selectedShapes.forEach(s -> {
                s.setSelected(false);
                s.hideConnectionPoints();
            });
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
            if (selectedShapes.size() > 1) {
                propertyPanel.showSelectedShapes(selectedShapes);
            } else if (!selectedShapes.isEmpty()) {
                propertyPanel.showShape(selectedShapes.get(0));
            } else {
                propertyPanel.showShape(null);
            }
        }
        redraw();
    }

    private void handleMouseDragged(MouseEvent event) {
        double x = event.getX(), y = event.getY();

        // 拖动创建新图形
        if (isDrawingNewShape && tempDrawingShape != null) {
            double startX = mousePressedX;
            double startY = mousePressedY;
            double endX = snapToGridEnabled ? snapToGrid(x) : x;
            double endY = snapToGridEnabled ? snapToGrid(y) : y;

            double minX = Math.min(startX, endX);
            double minY = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            updateTempJavaFXShape(tempDrawingShape, currentTool, minX, minY, width, height);
            return;
        }

        // 拖动选中图形
        if (isDraggingShapes && !selectedShapes.isEmpty()) {
            double dx = snapToGridEnabled ? snapToGrid(x - dragOriginMouseX) : (x - dragOriginMouseX);
            double dy = snapToGridEnabled ? snapToGrid(y - dragOriginMouseY) : (y - dragOriginMouseY);
            for (int i = 0; i < selectedShapes.size(); i++) {
                FlowchartShape s = selectedShapes.get(i);
                s.setX(dragStartX.get(i) + dx);
                s.setY(dragStartY.get(i) + dy);
            }
            redraw();
            return;
        }

        // 处理连接线的拖动（连线模式下实时显示临时线和箭头）
        if (isConnecting && startConnectionPoint != null && tempConnectionLine != null) {
            tempConnectionLine.setTempEnd(x, y);
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

        // 处理连接线的完成
        if (startConnectionPoint != null && tempConnectionLine != null) {
            ConnectionPoint endPoint = findConnectionPoint(x, y);
            if (endPoint != null && endPoint != startConnectionPoint) {
                tempConnectionLine.setEndPoint(endPoint);
                connectionLines.add(tempConnectionLine);
                startConnectionPoint.getParentShape().addOutgoingLine(tempConnectionLine);
                endPoint.getParentShape().addIncomingLine(tempConnectionLine);
            } else {
                getChildren().removeAll(tempConnectionLine.getLine(), tempConnectionLine.getArrow());
            }
            startConnectionPoint = null;
            tempConnectionLine = null;
            isConnecting = false;
            redraw();
            return;
        }

        // 完成新图形的绘制
        if (isDrawingNewShape && tempDrawingShape != null) {
            getChildren().remove(tempDrawingShape); // 移除临时图形
            double startX = mousePressedX;
            double startY = mousePressedY;
            double endX = snapToGridEnabled ? snapToGrid(x) : x;
            double endY = snapToGridEnabled ? snapToGrid(y) : y;

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
            
            // 更新属性面板显示
            if (propertyPanel != null) {
                if (selectedShapes.size() > 1) {
                    propertyPanel.showSelectedShapes(selectedShapes);
                } else if (!selectedShapes.isEmpty()) {
                    propertyPanel.showShape(selectedShapes.get(0));
                } else {
                    propertyPanel.showShape(null);
                }
            }
        }
        isDraggingShapes = false;
        redraw();
    }

    /**
     * 清空画布上的所有图形和选中状态。
     */
    public void clearCanvas() {
        shapes.clear();
        selectedShapes.clear();
        connectionLines.clear();
        undoStack.clear();
        redoStack.clear();
        redraw();
        if (propertyPanel != null) {
            propertyPanel.showShape(null);
            propertyPanel.showLine(null);
        }
    }

    /**
     * 将画布上的所有图形保存为JSON字符串。
     * @return 包含所有图形数据的JSON字符串。
     */
    public String saveShapesToJson() {
        JSONObject root = new JSONObject();
        JSONArray shapeArray = new JSONArray();
        for (FlowchartShape shape : shapes) {
            shapeArray.put(shape.toJsonObject());
        }
        root.put("shapes", shapeArray);
        JSONArray lineArray = new JSONArray();
        for (ConnectionLine line : connectionLines) {
            lineArray.put(line.toJsonObject(shapes));
        }
        root.put("connections", lineArray);
        return root.toString(4);
    }

    /**
     * 从JSON字符串加载图形到画布上。
     * @param jsonString 包含图形数据的JSON字符串。
     */
    public void loadShapesFromJson(String jsonString) {
        clearCanvas();
        JSONObject root = new JSONObject(jsonString);
        JSONArray shapeArray = root.getJSONArray("shapes");
        for (int i = 0; i < shapeArray.length(); i++) {
            JSONObject json = shapeArray.getJSONObject(i);
            String type = json.getString("type");
            double x = json.getDouble("x");
            double y = json.getDouble("y");
            double width = json.getDouble("width");
            double height = json.getDouble("height");
            String label = json.getString("label");
            Color color = Color.valueOf(json.getString("color"));
            FlowchartShape newShape = null;
            switch (type) {
                case "rectangle":
                    newShape = new RectangleShape(x, y, width, height, label);
                    break;
                case "ellipse":
                    newShape = new EllipseShape(x, y, width, height, label);
                    break;
                case "diamond":
                    newShape = new DiamondShape(x, y, width, height, label);
                    break;
                case "parallelogram":
                    newShape = new ParallelogramShape(x, y, width, height, label);
                    break;
                case "circle":
                    newShape = new CircleShape(x, y, width, height, label);
                    break;
                case "hexagon":
                    newShape = new HexagonShape(x, y, width, height, label);
                    break;
            }
            if (newShape != null) {
                newShape.setColor(color);
                shapes.add(newShape);
            }
        }
        // 加载连接线
        if (root.has("connections")) {
            JSONArray lineArray = root.getJSONArray("connections");
            for (int i = 0; i < lineArray.length(); i++) {
                JSONObject lineJson = lineArray.getJSONObject(i);
                ConnectionLine line = ConnectionLine.fromJsonObject(lineJson, shapes);
                connectionLines.add(line);
                // 维护入线/出线
                line.getStartPoint().getParentShape().addOutgoingLine(line);
                if (line.getEndPoint() != null) {
                    line.getEndPoint().getParentShape().addIncomingLine(line);
                }
            }
        }
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
        
        // 绘制所有连接线
        for (ConnectionLine line : connectionLines) {
            getChildren().addAll(line.getLine(), line.getArrow());
        }
        
        // 绘制所有图形
        for (FlowchartShape shape : shapes) {
            Shape fxShape = shape.getShape();
            getChildren().add(fxShape);
            Text text = new Text(shape.getX() + 20, shape.getY() + shape.getHeight() / 2, shape.getLabel());
            getChildren().add(text);
            
            // 绘制连接点
            for (ConnectionPoint point : shape.getConnectionPoints()) {
                if (isConnecting || shape.isSelected()) {
                    point.show();
                    getChildren().add(point.getVisualPoint());
                } else {
                    point.hide();
                }
            }
        }
        
        // 确保临时绘制的图形在最顶层
        if (tempDrawingShape != null && !getChildren().contains(tempDrawingShape)) {
            getChildren().add(tempDrawingShape);
        }
        if (selectionRect.isVisible() && !getChildren().contains(selectionRect)) {
            getChildren().add(selectionRect);
        }
        // 保证临时连线在最顶层
        if (isConnecting && tempConnectionLine != null) {
            if (!getChildren().contains(tempConnectionLine.getLine())) {
                getChildren().add(tempConnectionLine.getLine());
            }
            if (!getChildren().contains(tempConnectionLine.getArrow())) {
                getChildren().add(tempConnectionLine.getArrow());
            }
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
        System.out.println("copySelectedShapes() called. selectedShapes size: " + selectedShapes.size());
        clipboard.clear();
        for (FlowchartShape shape : selectedShapes) {
            try {
                clipboard.add((FlowchartShape) shape.clone());
                System.out.println("Cloned shape: " + shape.getLabel());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                System.err.println("Error cloning shape: " + e.getMessage());
            }
        }
        System.out.println("clipboard size after copy: " + clipboard.size());
    }

    // 粘贴
    public void pasteShapes() {
        System.out.println("pasteShapes() called. clipboard size: " + clipboard.size());
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
                    newShape.setColor(shape.getColor());
                    executeCommand(new AddShapeCommand(shapes, newShape));
                    selectedShapes.add(newShape);
                    newShape.setSelected(true);
                    System.out.println("Pasted new shape: " + newShape.getLabel() + " at (" + newShape.getX() + ", " + newShape.getY() + ")");
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

    // 设置网格
    private void setupGrid() {
        // 清除现有的网格线
        gridLines.forEach(line -> getChildren().remove(line));
        gridLines.clear();

        if (!showGrid) return;

        // 创建垂直线
        for (int x = 0; x < getWidth(); x += GRID_SIZE) {
            javafx.scene.shape.Line line = new javafx.scene.shape.Line(x, 0, x, getHeight());
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(0.5);
            gridLines.add(line);
            getChildren().add(line);
        }

        // 创建水平线
        for (int y = 0; y < getHeight(); y += GRID_SIZE) {
            javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, y, getWidth(), y);
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(0.5);
            gridLines.add(line);
            getChildren().add(line);
        }
    }

    // 切换网格显示
    public void toggleGrid() {
        showGrid = !showGrid;
        setupGrid();
        redraw();
    }

    // 切换网格对齐
    public void toggleSnapToGrid() {
        snapToGridEnabled = !snapToGridEnabled;
    }

    // 重写布局方法以更新网格
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        setupGrid();
    }

    private ConnectionPoint findConnectionPoint(double x, double y) {
        for (FlowchartShape shape : shapes) {
            for (ConnectionPoint point : shape.getConnectionPoints()) {
                double dx = point.getX() - x;
                double dy = point.getY() - y;
                if (point.isVisible() && Math.sqrt(dx * dx + dy * dy) < 12) {
                    return point;
                }
            }
        }
        return null;
    }

    private ConnectionLine findConnectionLine(double x, double y) {
        for (ConnectionLine line : connectionLines) {
            Shape shape = line.getLine();
            if (shape instanceof javafx.scene.shape.Line) {
                javafx.scene.shape.Line l = (javafx.scene.shape.Line) shape;
                if (pointToSegmentDistance(x, y, l.getStartX(), l.getStartY(), l.getEndX(), l.getEndY()) < 8) return line;
            } else if (shape instanceof Polyline) {
                Polyline poly = (Polyline) shape;
                ObservableList<Double> pts = poly.getPoints();
                for (int i = 0; i < pts.size() - 2; i += 2) {
                    double x1 = pts.get(i), y1 = pts.get(i + 1);
                    double x2 = pts.get(i + 2), y2 = pts.get(i + 3);
                    if (pointToSegmentDistance(x, y, x1, y1, x2, y2) < 8) return line;
                }
            } else if (shape instanceof QuadCurve) {
                QuadCurve curve = (QuadCurve) shape;
                // 近似用端点和控制点分段检测
                if (pointToSegmentDistance(x, y, curve.getStartX(), curve.getStartY(), curve.getControlX(), curve.getControlY()) < 8) return line;
                if (pointToSegmentDistance(x, y, curve.getControlX(), curve.getControlY(), curve.getEndX(), curve.getEndY()) < 8) return line;
            }
        }
        return null;
    }

    private double pointToSegmentDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0) return Math.hypot(px - x1, py - y1);
        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * dx, projY = y1 + t * dy;
        return Math.hypot(px - projX, py - projY);
    }

    public void exportAsPng(File file) {
        WritableImage image = this.snapshot(new SnapshotParameters(), null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}