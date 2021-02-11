package ru.fbtw.running_text.core;

import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;

import java.util.function.Consumer;

@FunctionalInterface
public interface RecognizeEvent {
	void handle(SpeechRecognitionAlternative alternative, boolean isFinal);
}
