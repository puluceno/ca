package br.com.silva.exceptions;

public class InvalidCAException extends NullPointerException {

	private static final long serialVersionUID = -6820916043756394635L;

	public InvalidCAException() {
		super();
	}

	public InvalidCAException(String s) {
		super(s);
	}
}
