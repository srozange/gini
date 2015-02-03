package org.theglump.gini;

public class GiniException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GiniException() {
	}

	public GiniException(String message) {
		super(message);
	}

	public GiniException(Throwable cause) {
		super(cause);
	}

	public GiniException(String message, Throwable cause) {
		super(message, cause);
	}

}
