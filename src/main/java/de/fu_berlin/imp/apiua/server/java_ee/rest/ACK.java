package de.fu_berlin.imp.apiua.server.java_ee.rest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

@XmlRootElement(namespace = "de.fu_berlin.imp.apiua.server.java_ee")
public class ACK {
	private DateTime dateTime;

	public ACK() {
		this.dateTime = DateTime.now();
	}

	@XmlAttribute
	public String getTimestamp() {
		return ISODateTimeFormat.dateTime().withOffsetParsed()
				.print(this.dateTime);
	}
}
