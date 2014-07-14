package de.fu_berlin.imp.apiua.server.java_ee.persistence.db;

public class PersistenceException extends Exception {

	private static final long serialVersionUID = 1L;

	public PersistenceException(String message, Throwable t) {
		super(message, t);
	}

}
