package editor.ui;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class ShapeLibraryPanel extends VBox {
    public ShapeLibraryPanel() {
        setSpacing(10);
        getChildren().add(new Label("图形库"));
        getChildren().addAll(
            new Button("开始/结束"),
            new Button("输入/输出"),
            new Button("处理"),
            new Button("判定")
        );
    }
}