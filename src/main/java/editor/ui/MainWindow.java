package editor.ui;

import javafx.scene.layout.BorderPane;

public class MainWindow extends BorderPane {
    public MainWindow() {
        CanvasPane canvas = new CanvasPane();
        ToolbarPanel toolbar = new ToolbarPanel(canvas);
        ShapeLibraryPanel shapeLibrary = new ShapeLibraryPanel();
        PropertyPanel propertyPanel = new PropertyPanel();

        canvas.setPropertyPanel(propertyPanel);
        propertyPanel.setOnShapeChanged(canvas::redraw);

        setTop(toolbar);
        setLeft(shapeLibrary);
        setCenter(canvas);
        setRight(propertyPanel);
    }
}