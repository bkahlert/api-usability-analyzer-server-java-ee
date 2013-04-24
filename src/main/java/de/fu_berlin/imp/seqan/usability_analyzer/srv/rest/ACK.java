package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
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
