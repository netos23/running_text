package ru.fbtw.running_text;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.fbtw.running_text.ui.BillboardStage;

public class App extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		double sizeX = 0.4;
		BillboardStage stage = new BillboardStage(sizeX, 0.05, 0.5 - sizeX / 2, 0.1);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
