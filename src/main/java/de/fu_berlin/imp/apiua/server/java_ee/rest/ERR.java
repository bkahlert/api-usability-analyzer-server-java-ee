package de.fu_berlin.imp.apiua.server.java_ee.rest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

@XmlRootElement(namespace = "de.fu_berlin.imp.apiua.server.java_ee")
public class ERR {
	private DateTime dateTime;
	private String errorMessage;

	@SuppressWarnings("unused")
	private ERR() {
		this.dateTime = null;
		this.errorMessage = null;
	}

	public ERR(String errorMessage) {
		this.dateTime = DateTime.now();
		this.errorMessage = errorMessage;
	}

	@XmlAttribute
	public String getTimestamp() {
		return ISODateTimeFormat.dateTime().withOffsetParsed()
				.print(this.dateTime);
	}

	@XmlAttribute
	public String getErrorMessage() {
		return errorMessage;
	}
}
