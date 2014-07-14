package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
public class StatisticsEntry {
	private ID id;
	private int numFiles;
	private DoclogRecord lastDoclogRecord;

	/**
	 * Needed by Jersey
	 */
	@SuppressWarnings("unused")
	private StatisticsEntry() {

	}

	public StatisticsEntry(ID id, int numFiles, DoclogRecord lastDoclogRecord) {
		this.id = id;
		this.numFiles = numFiles;
		this.lastDoclogRecord = lastDoclogRecord;
	}

	@XmlAttribute
	public ID getId() {
		return id;
	}

	@XmlAttribute
	public int getNumFiles() {
		return this.numFiles;
	}

	@XmlElement
	public DoclogRecord getLastDoclogRecord() {
		return lastDoclogRecord;
	}
}
