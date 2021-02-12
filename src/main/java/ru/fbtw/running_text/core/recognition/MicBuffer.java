package ru.fbtw.running_text.core.recognition;

import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.BlockingQueue;

public class MicBuffer implements Runnable {
	private static final int BYTES_PER_BUFFER = 6400; // buffer size in bytes

	private final TargetDataLine targetDataLine;
	private final BlockingQueue<byte[]> sharedQueue;

	public MicBuffer(TargetDataLine targetDataLine, BlockingQueue<byte[]> sharedQueue) {
		this.targetDataLine = targetDataLine;
		this.sharedQueue = sharedQueue;
	}

	@Override
	public void run() {
		targetDataLine.start();
		byte[] data = new byte[BYTES_PER_BUFFER];
		while (targetDataLine.isOpen()) {
			try {
				int numBytesRead = targetDataLine.read(data, 0, data.length);
				if ((numBytesRead <= 0) && (targetDataLine.isOpen())) {
					continue;
				}
				sharedQueue.put(data.clone());
			} catch (InterruptedException e) {
				System.out.println("Microphone input buffering interrupted : " + e.getMessage());
			}
		}
	}
}
