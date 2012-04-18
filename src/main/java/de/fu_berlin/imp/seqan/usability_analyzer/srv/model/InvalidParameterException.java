package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

public class InvalidParameterException extends RuntimeException {

	private static final long serialVersionUID = 8834406473727824209L;

	public InvalidParameterException(String string) {
		super(string);
	}
}
