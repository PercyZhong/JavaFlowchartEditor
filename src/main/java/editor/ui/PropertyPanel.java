package editor.ui;

import editor.model.FlowchartShape;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.List;

public class PropertyPanel extends VBox {
    private final TextField labelField = new TextField();
    private final ColorPicker colorPicker = new ColorPicker();
    private boolean isUpdatingFromShape = false; // 添加标志，用于防止循环更新
    private boolean isMultiSelect = false; // 添加多选标志

    private FlowchartShape currentShape;
    private List<FlowchartShape> selectedShapes; // 添加选中图形列表

    public PropertyPanel() {
        setPadding(new Insets(10));
        setSpacing(10);
        getChildren().addAll(
                new Label("文字标签："), labelField,
                new Label("颜色："), colorPicker
        );

        labelField.setOnAction(e -> updateShape());
        labelField.textProperty().addListener((obs, oldVal, newVal) -> updateShape());
        colorPicker.setOnAction(e -> updateShape());
    }

    private void updateShape() {
        if (isUpdatingFromShape) return;

        if (isMultiSelect && selectedShapes != null) {
            // 多选模式下，更新所有选中图形的属性
            for (FlowchartShape shape : selectedShapes) {
                shape.setLabel(labelField.getText());
                shape.setColor(colorPicker.getValue());
            }
        } else if (currentShape != null) {
            // 单选模式下，只更新当前图形
            currentShape.setLabel(labelField.getText());
            currentShape.setColor(colorPicker.getValue());
        }

        if (onShapeChanged != null) onShapeChanged.run();
    }

    // 供外部调用，显示选中图形属性
    public void showShape(FlowchartShape shape) {
        isUpdatingFromShape = true;
        currentShape = shape;
        isMultiSelect = false;
        selectedShapes = null;

        if (shape != null) {
            labelField.setText(shape.getLabel());
            colorPicker.setValue(shape.getColor());
            labelField.setDisable(false);
            colorPicker.setDisable(false);
        } else {
            labelField.setText("");
            colorPicker.setValue(Color.WHITE);
            labelField.setDisable(true);
            colorPicker.setDisable(true);
        }
        isUpdatingFromShape = false;
    }

    // 新增方法：显示多个选中图形的属性
    public void showSelectedShapes(List<FlowchartShape> shapes) {
        isUpdatingFromShape = true;
        selectedShapes = shapes;
        isMultiSelect = shapes != null && shapes.size() > 1;
        currentShape = shapes != null && !shapes.isEmpty() ? shapes.get(shapes.size() - 1) : null;

        if (shapes != null && !shapes.isEmpty()) {
            // 如果所有选中图形的标签都相同，则显示该标签，否则显示空
            String firstLabel = shapes.get(0).getLabel();
            boolean allSameLabel = shapes.stream().allMatch(s -> s.getLabel().equals(firstLabel));
            labelField.setText(allSameLabel ? firstLabel : "");

            // 如果所有选中图形的颜色都相同，则显示该颜色，否则显示白色
            Color firstColor = shapes.get(0).getColor();
            boolean allSameColor = shapes.stream().allMatch(s -> s.getColor().equals(firstColor));
            colorPicker.setValue(allSameColor ? firstColor : Color.WHITE);

            labelField.setDisable(false);
            colorPicker.setDisable(false);
        } else {
            labelField.setText("");
            colorPicker.setValue(Color.WHITE);
            labelField.setDisable(true);
            colorPicker.setDisable(true);
        }
        isUpdatingFromShape = false;
    }

    private Runnable onShapeChanged;

    public void setOnShapeChanged(Runnable callback) {
        this.onShapeChanged = callback;
    }
}