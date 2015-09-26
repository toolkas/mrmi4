package ru.idmt.commons.mrmi4.commons;

import java.io.IOException;

public class RException extends IOException {
	public RException() {
	}

	public RException(String message) {
		super(message);
	}

	public RException(String message, Throwable cause) {
		super(message, cause);
	}

	public RException(Throwable cause) {
		super(cause);
	}
}
