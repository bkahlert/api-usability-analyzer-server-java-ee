package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.selenium;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.DoclogRESTUtils;

/**
 * Checks for an DoclogRecord to be logged and returns the sorted recently
 * logged records.
 * 
 * @author bkahlert
 * 
 */
public class DoclogRecordOfType implements ExpectedCondition<DoclogRecord[]> {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogRecordOfType.class);

	private IIdentifier identifier;
	private DoclogAction doclogAction;
	private int[] indicesOfInterest;

	public DoclogRecordOfType(IIdentifier identifier,
			DoclogAction doclogAction, int numRecentRecords) {
		if (identifier == null || doclogAction == null || numRecentRecords < 1)
			throw new IllegalArgumentException();
		this.identifier = identifier;
		this.doclogAction = doclogAction;
		this.indicesOfInterest = new int[numRecentRecords];
		for (int i = 0; i < numRecentRecords; i++) {
			this.indicesOfInterest[i] = -(i + 1);
		}
	}

	public DoclogRecord[] apply(WebDriver driver) {
		try {
			DoclogRecord[] doclogRecords = DoclogRESTUtils
					.readSortedDoclogRecords(identifier, indicesOfInterest);
			if (doclogRecords == null || doclogRecords.length < 1)
				return null;
			if (doclogAction.equals(doclogRecords[doclogRecords.length - 1]
					.getAction()))
				return doclogRecords;
			else
				return null;
		} catch (Exception e) {
			LOGGER.error(e);
			return null;
		}
	}

	@Override
	public String toString() {
		return DoclogRecord.class.getSimpleName() + " of type "
				+ this.doclogAction;
	}
}
