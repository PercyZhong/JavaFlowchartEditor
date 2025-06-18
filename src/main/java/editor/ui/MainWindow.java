package editor.ui;

import editor.action.AddShapeCommand;
import editor.model.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MainWindow extends BorderPane {
    private CanvasPane canvas;
    private Stage primaryStage;

    public MainWindow(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.canvas = new CanvasPane();
        ToolbarPanel toolbar = new ToolbarPanel(canvas, this);
        ShapeLibraryPanel shapeLibrary = new ShapeLibraryPanel(canvas);
        PropertyPanel propertyPanel = new PropertyPanel();

        canvas.setPropertyPanel(propertyPanel);
        propertyPanel.setOnShapeChanged(canvas::redraw);

        setTop(toolbar);
        setLeft(shapeLibrary);
        setCenter(canvas);
        setRight(propertyPanel);
    }

    public void newFile() {
        canvas.clearCanvas();
    }

    public void saveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存流程图");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(canvas.saveShapesToJson());
            } catch (IOException e) {
                e.printStackTrace();
                // Handle error (e.g., show an alert to the user)
            }
        }
    }

    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开流程图");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            try {
                String jsonString = new String(Files.readAllBytes(file.toPath()));
                canvas.loadShapesFromJson(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle error
            }
        }
    }

    // Method to create FlowchartShape from JSON (used by loadShapesFromJson in CanvasPane)
    // This method is now in CanvasPane, but keeping it here for clarity if needed elsewhere
    public static FlowchartShape createShapeFromJson(JSONObject json) {
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
                // Circle的x,y是中心点，加载时需要调整
                newShape = new CircleShape(x, y, width, height, label);
                break;
            case "hexagon":
                newShape = new HexagonShape(x, y, width, height, label);
                break;
        }
        if (newShape != null) {
            newShape.setColor(color);
        }
        return newShape;
    }
}