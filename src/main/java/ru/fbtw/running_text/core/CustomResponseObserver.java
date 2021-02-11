package ru.fbtw.running_text.core;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeResponse;
import com.google.protobuf.Duration;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CustomResponseObserver
		implements ResponseObserver<StreamingRecognizeResponse> {

	public static final int STREAMING_LIMIT = 290000; // ~5 minutes

	private final Consumer<StreamController> controllerConsumer;
	private final RecognizeEvent event;

	private ArrayList<StreamingRecognizeResponse> responses;
	private int lastResultEndTime;
	private boolean lastResultFinished;

	public CustomResponseObserver(Consumer<StreamController> controllerConsumer, RecognizeEvent event) {
		this.controllerConsumer = controllerConsumer;
		this.event = event;

		responses = new ArrayList<>();
		lastResultEndTime = 0;
	}

	@Override
	public void onStart(StreamController streamController) {
		controllerConsumer.accept(streamController);
	}

	@Override
	public void onResponse(StreamingRecognizeResponse response) {
		responses.add(response);
		StreamingRecognitionResult result = response.getResultsList().get(0);

		Duration resultEndTime = result.getResultEndTime();
		this.lastResultEndTime =
				(int) resultEndTime.getSeconds() * 1000 + resultEndTime.getNanos() / 1000000;

		SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
		lastResultFinished = result.getIsFinal();
		if (result.getIsFinal()) {
			System.out.printf(
					"%s [confidence: %.2f]\n",
					alternative.getTranscript(),
					alternative.getConfidence());
		} else {
			System.out.printf(
					" %s", alternative.getTranscript());
		}
		/*if (result.getIsFinal()) {
			// если результат финальный

			alternative.getTranscript();
			alternative.getConfidence();
			isFinalEndTime = resultEndTimeInMS;
			lastTranscriptWasFinal = true;
		} else {
			// если результат не окончателен

			lastTranscriptWasFinal = false;
		}*/
	}

	@Override
	public void onError(Throwable throwable) {

	}

	@Override
	public void onComplete() {

	}

	public int getLastResultEndTime() {
		return lastResultEndTime;
	}

	public boolean isLastResultFinished() {
		return lastResultFinished;
	}

	public void setLastResultEndTime(int time) {
		lastResultEndTime = time;
	}
}
