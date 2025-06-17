package editor.ui;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.input.TransferMode;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ClipboardContent;

public class ShapeLibraryPanel extends VBox {
    private CanvasPane canvasPane;

    public ShapeLibraryPanel(CanvasPane canvasPane) {
        this.canvasPane = canvasPane;
        setSpacing(10);
        setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");
        
        getChildren().add(new Label("图形库"));
        
        // 开始/结束（椭圆）
        Button startEndBtn = createShapeButton("开始/结束", new Ellipse(20, 15), "椭圆");
        
        // 输入/输出（平行四边形）
        Button ioBtn = createShapeButton("输入/输出", createParallelogram(), "平行四边形");
        
        // 处理（矩形）
        Button processBtn = createShapeButton("处理", new Rectangle(40, 30), "矩形");
        
        // 判定（菱形）
        Button decisionBtn = createShapeButton("判定", createDiamond(), "菱形");
        
        getChildren().addAll(startEndBtn, ioBtn, processBtn, decisionBtn);
    }

    private Button createShapeButton(String text, javafx.scene.shape.Shape shape, String shapeType) {
        Button button = new Button(text);
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(Color.BLACK);
        button.setGraphic(shape);
        button.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5;");
        
        button.setOnDragDetected(event -> {
            Dragboard db = button.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(shapeType);
            db.setContent(content);
            event.consume();
        });
        return button;
    }

    private Polygon createParallelogram() {
        Polygon parallelogram = new Polygon(
            0, 0,
            30, 0,
            40, 30,
            10, 30
        );
        return parallelogram;
    }

    private Polygon createDiamond() {
        Polygon diamond = new Polygon(
            20, 0,
            40, 15,
            20, 30,
            0, 15
        );
        return diamond;
    }
}