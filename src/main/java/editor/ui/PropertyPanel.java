package editor.ui;

import editor.model.FlowchartShape;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PropertyPanel extends VBox {
    private final TextField labelField = new TextField();
    private final ColorPicker colorPicker = new ColorPicker();
    private boolean isUpdatingFromShape = false; // 添加标志，用于防止循环更新

    private FlowchartShape currentShape;

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
        if (currentShape != null && !isUpdatingFromShape) { // 只有在不是从图形更新时才执行
            currentShape.setLabel(labelField.getText());
            currentShape.setColor(colorPicker.getValue());
            if (onShapeChanged != null) onShapeChanged.run();
        }
    }

    // 供外部调用，显示选中图形属性
    public void showShape(FlowchartShape shape) {
        this.currentShape = shape;
        if (shape != null) {
            isUpdatingFromShape = true; // 开始更新，防止触发 updateShape
            labelField.setText(shape.getLabel());
            colorPicker.setValue(shape.getColor());
            isUpdatingFromShape = false; // 结束更新
            setDisable(false);
        } else {
            isUpdatingFromShape = true; // 开始更新，防止触发 updateShape
            labelField.setText("");
            colorPicker.setValue(Color.WHITE);
            isUpdatingFromShape = false; // 结束更新
            setDisable(true);
        }
    }

    // 画布刷新回调
    private Runnable onShapeChanged;
    public void setOnShapeChanged(Runnable r) { this.onShapeChanged = r; }
}