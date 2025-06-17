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

    private FlowchartShape currentShape;

    public PropertyPanel() {
        setPadding(new Insets(10));
        setSpacing(10);
        getChildren().addAll(
                new Label("文字标签："), labelField,
                new Label("颜色："), colorPicker
        );

        // labelField.setOnAction(e -> {
        //     if (currentShape != null) {
        //         currentShape.setLabel(labelField.getText());
        //         if (onShapeChanged != null) onShapeChanged.run();
        //     }
        // });
        // colorPicker.setOnAction(e -> {
        //     if (currentShape != null) {
        //         currentShape.setColor(colorPicker.getValue());
        //         if (onShapeChanged != null) onShapeChanged.run();
        //     }
        // });
        labelField.setOnAction(e -> updateShape());
        labelField.textProperty().addListener((obs, oldVal, newVal) -> updateShape());
        colorPicker.setOnAction(e -> updateShape());
    }
    private void updateShape() {
        if (currentShape != null) {
            currentShape.setLabel(labelField.getText());
            currentShape.setColor(colorPicker.getValue());
            if (onShapeChanged != null) onShapeChanged.run();
        }
    }

    // 供外部调用，显示选中图形属性
    public void showShape(FlowchartShape shape) {
        this.currentShape = shape;
        if (shape != null) {
            labelField.setText(shape.getLabel());
            colorPicker.setValue(shape.getColor());
            setDisable(false);
        } else {
            labelField.setText("");
            colorPicker.setValue(Color.WHITE);
            setDisable(true);
        }
    }

    // 画布刷新回调
    private Runnable onShapeChanged;
    public void setOnShapeChanged(Runnable r) { this.onShapeChanged = r; }
}