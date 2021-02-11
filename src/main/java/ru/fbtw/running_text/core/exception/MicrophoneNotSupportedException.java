package ru.fbtw.running_text.core.exception;

public class MicrophoneNotSupportedException extends Exception {
	private static final String MESSAGE = "Microphone unfortunately not supported";

	public MicrophoneNotSupportedException() {
		super(MESSAGE);
	}
}
