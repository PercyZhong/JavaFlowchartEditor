package editor.ui;

import javafx.scene.control.ToolBar;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class ToolbarPanel extends ToolBar {
    private final Map<String, Button> toolButtons = new HashMap<>();
    private String currentTool = "矩形";

    public ToolbarPanel(CanvasPane canvas) {
        Button selectBtn = new Button("选择");
        Button rectBtn = new Button("矩形");
        Button diamondBtn = new Button("菱形");
        Button ellipseBtn = new Button("椭圆");

        toolButtons.put("选择", selectBtn);
        toolButtons.put("矩形", rectBtn);
        toolButtons.put("菱形", diamondBtn);
        toolButtons.put("椭圆", ellipseBtn);

        selectBtn.setOnAction(e -> {
            setCurrentTool("选择", canvas);
        });
        rectBtn.setOnAction(e -> {
            setCurrentTool("矩形", canvas);
        });
        diamondBtn.setOnAction(e -> {
            setCurrentTool("菱形", canvas);
        });
        ellipseBtn.setOnAction(e -> {
            setCurrentTool("椭圆", canvas);
        });

        getItems().addAll(selectBtn, rectBtn, diamondBtn, ellipseBtn);

        // 默认高亮
        setCurrentTool("矩形", canvas);
    }

    private void setCurrentTool(String tool, CanvasPane canvas) {
        this.currentTool = tool;
        canvas.setCurrentTool(tool);
        // 高亮当前按钮
        for (Map.Entry<String, Button> entry : toolButtons.entrySet()) {
            if (entry.getKey().equals(tool)) {
                entry.getValue().setStyle("-fx-background-color: #3399ff; -fx-text-fill: white;");
            } else {
                entry.getValue().setStyle("");
            }
        }
    }
}