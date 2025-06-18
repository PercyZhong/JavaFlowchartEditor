package editor.ui;

import editor.model.FlowchartShape;
import editor.model.ConnectionLine;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class PropertyPanel extends VBox {
    private final TextField labelField = new TextField();
    private final TextField linkField = new TextField();
    private final ColorPicker shapeColorPicker = new ColorPicker();
    private final ColorPicker lineColorPicker = new ColorPicker();
    private boolean isUpdatingFromShape = false; // 添加标志，用于防止循环更新
    private boolean isMultiSelect = false; // 添加多选标志

    private FlowchartShape currentShape;
    private List<FlowchartShape> selectedShapes; // 添加选中图形列表

    private ConnectionLine currentLine;
    private ComboBox<String> lineTypeBox = new ComboBox<>();
    private Spinner<Double> strokeWidthSpinner = new Spinner<>();
    private CheckBox arrowCheckBox = new CheckBox("显示箭头");

    private VBox shapeSection = new VBox();
    private VBox lineSection = new VBox();

    public PropertyPanel() {
        setPadding(new Insets(10));
        setSpacing(10);
        shapeSection.setSpacing(10);
        lineSection.setSpacing(10);
        // 图形属性区控件
        shapeSection.getChildren().addAll(
            new Label("文字标签："), labelField,
            new Label("颜色："), shapeColorPicker,
            new Label("链接："), linkField
        );
        // 线条属性区控件
        lineSection.getChildren().addAll(
            new Label("线型："), lineTypeBox,
            new Label("颜色："), lineColorPicker,
            new Label("粗细："), strokeWidthSpinner,
            arrowCheckBox
        );
        getChildren().addAll(shapeSection, lineSection);
        // 初始全部禁用
        setShapeControlsEnabled(false);
        setLineControlsEnabled(false);
        labelField.setText("");
        shapeColorPicker.setValue(Color.WHITE);
        lineTypeBox.getItems().setAll("直线", "折线", "曲线");
        lineTypeBox.setValue(null);
        strokeWidthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 10, 2, 1));
        arrowCheckBox.setSelected(true);
        // 事件绑定
        labelField.setOnAction(e -> updateShape());
        labelField.textProperty().addListener((obs, oldVal, newVal) -> updateShape());
        shapeColorPicker.setOnAction(e -> updateShape());
        lineTypeBox.setOnAction(e -> {
            if (currentLine != null && lineTypeBox.getValue() != null) {
                String val = lineTypeBox.getValue();
                if (val.equals("直线")) currentLine.setLineType(ConnectionLine.LineType.STRAIGHT);
                else if (val.equals("折线")) currentLine.setLineType(ConnectionLine.LineType.POLYLINE);
                else currentLine.setLineType(ConnectionLine.LineType.CURVE);
            }
        });
        lineColorPicker.setOnAction(e -> {
            if (currentLine != null) currentLine.setColor(lineColorPicker.getValue());
        });
        strokeWidthSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentLine != null) currentLine.setStrokeWidth(newVal);
        });
        arrowCheckBox.setOnAction(e -> {
            if (currentLine != null) currentLine.setArrowEnabled(arrowCheckBox.isSelected());
        });
        linkField.setPromptText("http(s)://...");
        linkField.setOnAction(e -> updateShape());
        linkField.textProperty().addListener((obs, oldVal, newVal) -> updateShape());
    }

    private void setShapeControlsEnabled(boolean enabled) {
        labelField.setDisable(!enabled);
        shapeColorPicker.setDisable(!enabled);
        linkField.setDisable(!enabled);
    }
    private void setLineControlsEnabled(boolean enabled) {
        lineTypeBox.setDisable(!enabled);
        lineColorPicker.setDisable(!enabled);
        strokeWidthSpinner.setDisable(!enabled);
        arrowCheckBox.setDisable(!enabled);
    }

    private void updateShape() {
        if (isUpdatingFromShape) return;

        if (isMultiSelect && selectedShapes != null) {
            for (FlowchartShape shape : selectedShapes) {
                shape.setLabel(labelField.getText());
                shape.setColor(shapeColorPicker.getValue());
                shape.setLink(linkField.getText());
            }
        } else if (currentShape != null) {
            currentShape.setLabel(labelField.getText());
            currentShape.setColor(shapeColorPicker.getValue());
            currentShape.setLink(linkField.getText());
        }

        if (onShapeChanged != null) onShapeChanged.run();
    }

    public void showShape(FlowchartShape shape) {
        this.currentShape = shape;
        this.selectedShapes = null;
        isMultiSelect = false;
        if (shape == null) {
            labelField.setText("");
            shapeColorPicker.setValue(Color.WHITE);
            linkField.setText("");
            setShapeControlsEnabled(false);
            return;
        }
        setShapeControlsEnabled(true);
        isUpdatingFromShape = true;
        labelField.setText(shape.getLabel());
        shapeColorPicker.setValue(shape.getColor());
        linkField.setText(shape.getLink());
        isUpdatingFromShape = false;
    }

    public void showSelectedShapes(List<FlowchartShape> shapes) {
        this.selectedShapes = shapes;
        this.currentShape = null;
        isMultiSelect = true;
        if (shapes == null || shapes.isEmpty()) {
            labelField.setText("");
            shapeColorPicker.setValue(Color.WHITE);
            linkField.setText("");
            setShapeControlsEnabled(false);
            return;
        }
        setShapeControlsEnabled(true);
        isUpdatingFromShape = true;
        labelField.setText("");
        shapeColorPicker.setValue(Color.WHITE);
        linkField.setText("");
        isUpdatingFromShape = false;
    }

    public void showLine(ConnectionLine line) {
        this.currentLine = line;
        if (line == null) {
            lineTypeBox.setValue(null);
            lineColorPicker.setValue(Color.BLACK);
            strokeWidthSpinner.getValueFactory().setValue(2.0);
            arrowCheckBox.setSelected(true);
            setLineControlsEnabled(false);
            return;
        }
        setLineControlsEnabled(true);
        lineTypeBox.setValue(line.getLineType() == ConnectionLine.LineType.STRAIGHT ? "直线" :
                             line.getLineType() == ConnectionLine.LineType.POLYLINE ? "折线" : "曲线");
        lineColorPicker.setValue(line.getColor());
        strokeWidthSpinner.getValueFactory().setValue(line.getStrokeWidth());
        arrowCheckBox.setSelected(line.isArrowEnabled());
    }

    public void clearShape() {
        labelField.setText("");
        shapeColorPicker.setValue(Color.WHITE);
        linkField.setText("");
        setShapeControlsEnabled(false);
    }
    public void clearLine() {
        lineTypeBox.setValue(null);
        lineColorPicker.setValue(Color.BLACK);
        strokeWidthSpinner.getValueFactory().setValue(2.0);
        arrowCheckBox.setSelected(true);
        setLineControlsEnabled(false);
    }

    private Runnable onShapeChanged;

    public void setOnShapeChanged(Runnable callback) {
        this.onShapeChanged = callback;
    }
}