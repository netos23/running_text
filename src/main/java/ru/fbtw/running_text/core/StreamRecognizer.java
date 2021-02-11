package ru.fbtw.running_text.core;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import ru.fbtw.running_text.core.exception.MicrophoneNotSupportedException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static ru.fbtw.running_text.core.CustomResponseObserver.STREAMING_LIMIT;

public class StreamRecognizer {
	private static volatile BlockingQueue<byte[]> sharedQueue = new LinkedBlockingQueue();
	private static TargetDataLine targetDataLine;
	private static StreamController streamController;

	private static boolean newStream;
	private static ArrayList<ByteString> audioInput = new ArrayList<ByteString>();
	private static ArrayList<ByteString> lastAudioInput = new ArrayList<ByteString>();
	private static ByteString tempByteString;

	private static double bridgingOffset = 0;
	private static int finalRequestEndTime;


	public static void infiniteStreamingRecognize(String languageCode) throws Exception {
		// Задание формата аудио
		AudioFormat audioFormat =
				new AudioFormat(16000, 16, 1, true, false);
		DataLine.Info targetInfo =
				new DataLine.Info(
						TargetDataLine.class,
						audioFormat); // Set the system information to read from the microphone audio
		// stream

		if (!AudioSystem.isLineSupported(targetInfo)) {
			throw new MicrophoneNotSupportedException();
		}
		// Target data line captures the audio stream the microphone produces.
		targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
		targetDataLine.open(audioFormat);

		MicBuffer buffer = new MicBuffer(targetDataLine, sharedQueue);
		Thread micThread = new Thread(buffer);
		micThread.setDaemon(true);

		try (SpeechClient client = SpeechClient.create()) {
			// Обозреватель ответа
			CustomResponseObserver responseObserver =
					new CustomResponseObserver(controller -> streamController = controller,
							(alternative, isFinal) -> {
							});

			// Стрим отправки запросов
			ClientStream<StreamingRecognizeRequest> clientStream =
					client.streamingRecognizeCallable().splitCall(responseObserver);

			RecognitionConfig recognitionConfig =
					RecognitionConfig.newBuilder()
							.setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
							.setLanguageCode(languageCode)
							.setSampleRateHertz(16000)
							.build();

			StreamingRecognitionConfig streamingRecognitionConfig =
					StreamingRecognitionConfig.newBuilder()
							.setConfig(recognitionConfig)
							.setInterimResults(true)
							.build();

			StreamingRecognizeRequest request =
					StreamingRecognizeRequest.newBuilder()
							.setStreamingConfig(streamingRecognitionConfig)
							.build(); // The first request in a streaming call has to be a config


			clientStream.send(request);

			try {
				micThread.start();

				long startTime = System.currentTimeMillis();

				while (true) {

					long estimatedTime = System.currentTimeMillis() - startTime;

					if (estimatedTime >= STREAMING_LIMIT) {

						clientStream.closeSend();
						streamController.cancel(); // remove Observer

						if (responseObserver.getLastResultEndTime() > 0
								&& responseObserver.isLastResultFinished()) {
							finalRequestEndTime = responseObserver.getLastResultEndTime();
						}
						responseObserver.setLastResultEndTime(0);

						lastAudioInput = null;
						lastAudioInput = audioInput;
						audioInput = new ArrayList<ByteString>();

						/*if (!lastTranscriptWasFinal) {
							System.out.print('\n');
						}*/

						newStream = true;

						clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

						request =
								StreamingRecognizeRequest.newBuilder()
										.setStreamingConfig(streamingRecognitionConfig)
										.build();

						startTime = System.currentTimeMillis();

					} else {

						if ((newStream) && (lastAudioInput.size() > 0)) {
							// if this is the first audio from a new request
							// calculate amount of unfinalized audio from last request
							// resend the audio to the speech client before incoming audio
							double chunkTime = STREAMING_LIMIT / lastAudioInput.size();
							// ms length of each chunk in previous request audio arrayList
							if (chunkTime != 0) {
								if (bridgingOffset < 0) {
									// bridging Offset accounts for time of resent audio
									// calculated from last request
									bridgingOffset = 0;
								}
								if (bridgingOffset > finalRequestEndTime) {
									bridgingOffset = finalRequestEndTime;
								}
								int chunksFromMs =
										(int) Math.floor((finalRequestEndTime - bridgingOffset) / chunkTime);
								// chunks from MS is number of chunks to resend
								bridgingOffset =
										(int) Math.floor((lastAudioInput.size() - chunksFromMs) * chunkTime);
								// set bridging offset for next request
								for (int i = chunksFromMs; i < lastAudioInput.size(); i++) {
									request =
											StreamingRecognizeRequest.newBuilder()
													.setAudioContent(lastAudioInput.get(i))
													.build();
									clientStream.send(request);
								}
							}
							newStream = false;
						}

						tempByteString = ByteString.copyFrom(sharedQueue.take());

						request =
								StreamingRecognizeRequest.newBuilder().setAudioContent(tempByteString).build();

						audioInput.add(tempByteString);
					}

					clientStream.send(request);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
