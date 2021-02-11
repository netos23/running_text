package ru.fbtw.running_text;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.fbtw.running_text.core.StreamRecognizer;

public class App extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		Label label = new Label("Hello, World!");
		VBox vBox = new VBox(label);
		Scene scene = new Scene(vBox);

		primaryStage.setScene(scene);
		primaryStage.setAlwaysOnTop(true);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setX(650);
		primaryStage.setY(1000);
		primaryStage.show();
	}

	public static void main(String[] args) {
		//launch(args);
		try {
			StreamRecognizer.infiniteStreamingRecognize("ru");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
