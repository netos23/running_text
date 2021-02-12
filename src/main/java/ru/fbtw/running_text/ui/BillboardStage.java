package ru.fbtw.running_text.ui;

import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.fbtw.running_text.core.recognition.StreamRecognizer;
import ru.fbtw.running_text.core.translition.Translator;


public class BillboardStage {

	private Stage stage;
	private Label textView;
	private double sizeX, sizeY;
	private double posX, posY;

	public BillboardStage(double sizeX, double sizeY, double posX, double posY) throws Exception {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.posX = posX;
		this.posY = posY;

		stage = new Stage(StageStyle.TRANSPARENT);
		stage.setOpacity(0.5);

		Rectangle2D screenSize = Screen.getPrimary().getBounds();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		stage.setX(width * posX);
		stage.setY(height * (1 - posY));
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);

		textView = new Label("Some example text");
		HBox hBox = new HBox(textView);
		hBox.getStyleClass().add("container");

		Scene scene = new Scene(hBox, width * sizeX, height * sizeY);
		scene.getStylesheets().add("billboard.css");


		stage.setScene(scene);
		stage.show();

		Thread thread = new Thread(this::start);
		thread.setDaemon(true);
		thread.start();

	}

	private void start() {
		try {
			StreamRecognizer.infiniteStreamingRecognize("en-US", this::handleRecognition);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleRecognition(SpeechRecognitionAlternative alternative, boolean isFinal) {
		/*String transcript = alternative.getTranscript();
		String translate = Translator.translate(transcript, "en", "ru");
		Platform.runLater(() -> textView.setText(translate));*/
		String transcript = alternative.getTranscript();
		Platform.runLater(() -> textView.setText(transcript));
	}


}
