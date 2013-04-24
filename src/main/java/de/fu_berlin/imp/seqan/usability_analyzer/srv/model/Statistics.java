package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.DoclogPersistence.DoclogPersistenceException;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.rest.DiffManager;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.rest.DiffManager.DiffManagerException;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.LocationUtils;

// TODO lastModification
// TODO lastDoclogRecord ist aktuell immer null
@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
public class Statistics {
	private ServletContext context;

	/**
	 * Need by Jersey
	 */
	@SuppressWarnings("unused")
	private Statistics() {

	}

	public Statistics(ServletContext context) {
		this.context = context;
	}

	@XmlAttribute
	public DateTime getCreation() {
		return DateTime.now();
	}

	@XmlElement
	public List<StatisticsEntry> getEntries() {
		List<StatisticsEntry> entries = new ArrayList<StatisticsEntry>();
		for (ID id : DiffManager.getIDs(context)) {
			int numFiles;
			try {
				numFiles = DiffManager.listFiles(context, id).size();
			} catch (DiffManagerException e) {
				numFiles = -1;
			}
			DoclogRecord lastDoclogRecord;
			try {
				lastDoclogRecord = LocationUtils.getDoclogPersistence(context)
						.getRecord(id, -1);
			} catch (DoclogPersistenceException e) {
				lastDoclogRecord = null;
				e.printStackTrace();
			}
			entries.add(new StatisticsEntry(id, numFiles, lastDoclogRecord));
		}
		return entries;
	}
}