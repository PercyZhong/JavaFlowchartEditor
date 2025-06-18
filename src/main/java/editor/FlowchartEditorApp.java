package editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import editor.ui.MainWindow;

public class FlowchartEditorApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow(primaryStage);
        Scene scene = new Scene(mainWindow, 1200, 800);
        primaryStage.setTitle("流程图编辑器");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}