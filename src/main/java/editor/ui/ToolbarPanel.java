package editor.ui;

import editor.action.Command;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class ToolbarPanel extends ToolBar {

    private CanvasPane canvas;
    private final Map<String, Button> toolButtons = new HashMap<>();

    public ToolbarPanel(CanvasPane canvas) {
        this.canvas = canvas;

        // 文件操作按钮
        Button newBtn = createToolbarButton("new.png", "新建", null);
        // newBtn.setOnAction(e -> {}); // Add handler if needed
        Button openBtn = createToolbarButton("open.png", "打开", null);
        // openBtn.setOnAction(e -> {}); // Add handler if needed
        Button saveBtn = createToolbarButton("save.png", "保存", null);
        // saveBtn.setOnAction(e -> {}); // Add handler if needed

        // 编辑操作按钮
        Button copyBtn = createToolbarButton("copy.png", "复制 (Ctrl+C)", new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        copyBtn.setOnAction(e -> canvas.copySelectedShapes());
        Button pasteBtn = createToolbarButton("paste.png", "粘贴 (Ctrl+V)", new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        pasteBtn.setOnAction(e -> canvas.pasteShapes());
        Button deleteBtn = createToolbarButton("delete.png", "删除 (Del)", new KeyCodeCombination(KeyCode.DELETE));
        deleteBtn.setOnAction(e -> canvas.deleteSelectedShapes());
        Button undoBtn = createToolbarButton("undo.png", "撤销 (Ctrl+Z)", new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        undoBtn.setOnAction(e -> canvas.undo());
        Button redoBtn = createToolbarButton("redo.png", "重做 (Ctrl+Y)", new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        redoBtn.setOnAction(e -> canvas.redo());

        // 工具选择按钮 (基础图形)
        Button selectToolBtn = createToolButton("选择");
        Button rectToolBtn = createToolButton("矩形");
        Button ellipseToolBtn = createToolButton("椭圆");
        Button diamondToolBtn = createToolButton("菱形");
        Button circleToolBtn = createToolButton("圆形");
        Button parallelogramToolBtn = createToolButton("平行四边形");
        Button hexagonToolBtn = createToolButton("六边形");

        toolButtons.put("选择", selectToolBtn);
        toolButtons.put("矩形", rectToolBtn);
        toolButtons.put("椭圆", ellipseToolBtn);
        toolButtons.put("菱形", diamondToolBtn);
        toolButtons.put("圆形", circleToolBtn);
        toolButtons.put("平行四边形", parallelogramToolBtn);
        toolButtons.put("六边形", hexagonToolBtn);

        selectToolBtn.setOnAction(e -> setCurrentTool("选择"));
        rectToolBtn.setOnAction(e -> setCurrentTool("矩形"));
        ellipseToolBtn.setOnAction(e -> setCurrentTool("椭圆"));
        diamondToolBtn.setOnAction(e -> setCurrentTool("菱形"));
        circleToolBtn.setOnAction(e -> setCurrentTool("圆形"));
        parallelogramToolBtn.setOnAction(e -> setCurrentTool("平行四边形"));
        hexagonToolBtn.setOnAction(e -> setCurrentTool("六边形"));

        getItems().addAll(
                newBtn, openBtn, saveBtn, new Separator(),
                copyBtn, pasteBtn, deleteBtn, undoBtn, redoBtn, new Separator(),
                selectToolBtn, rectToolBtn, ellipseToolBtn, diamondToolBtn, circleToolBtn, parallelogramToolBtn, hexagonToolBtn
        );

        // 默认选择"选择"工具
        setCurrentTool("选择");
    }

    private Button createToolbarButton(String iconFileName, String tooltip, KeyCombination accelerator) {
        Image image = new Image(getClass().getResourceAsStream("/icons/" + iconFileName));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        Button button = new Button();
        button.setGraphic(imageView);
        button.setTooltip(new Tooltip(tooltip));
        if (accelerator != null) {
            button.getProperties().put("accelerator", accelerator);
        }
        return button;
    }

    private Button createToolButton(String toolName) {
        Button button = new Button(toolName);
        return button;
    }

    private void setCurrentTool(String tool) {
        this.canvas.setCurrentTool(tool);
        // 高亮当前选中的工具按钮
        for (Map.Entry<String, Button> entry : toolButtons.entrySet()) {
            if (entry.getKey().equals(tool)) {
                entry.getValue().setStyle("-fx-background-color: #3399ff; -fx-text-fill: white;");
            } else {
                entry.getValue().setStyle("");
            }
        }
    }
}