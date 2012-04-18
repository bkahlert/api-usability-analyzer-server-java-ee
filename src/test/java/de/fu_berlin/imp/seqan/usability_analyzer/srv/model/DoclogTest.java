package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.JAXBUtils;

public class DoclogTest {

	public static final DoclogRecord doclogRecord1 = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", null, DoclogAction.READY, null,
			DoclogRecord.ISO8601_simple.parseDateTime("2011-09-13T12-07-53"),
			new Rectangle(0, 0, 179, 477));
	public static final DoclogRecord doclogRecord2 = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", null, DoclogAction.SCROLL, null,
			DoclogRecord.ISO8601_simple.parseDateTime("2011-09-13T12-07-55"),
			new Rectangle(0, 132, 179, 477));
	public static final DoclogRecord doclogRecord3 = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", "192.168.0.1", DoclogAction.LINK,
			"http://www.bkahlert.com", DoclogRecord.ISO8601_simple
					.parseDateTime("2011-09-13T12-09-04"), new Rectangle(0,
					132, 179, 477));
	public static final DoclogRecord doclogRecord4 = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", null, DoclogAction.UNLOAD, null,
			DoclogRecord.ISO8601_simple.parseDateTime("2011-09-13T12-09-04"),
			new Rectangle(0, 132, 179, 477));

	public static Doclog DOCLOG;

	public DoclogTest() {
		DOCLOG = new Doclog();
		DOCLOG.add(doclogRecord1);
		DOCLOG.add(doclogRecord2);
		DOCLOG.add(doclogRecord3);
		DOCLOG.add(doclogRecord4);
	}

	@Test
	public void testMarshalling() throws JAXBException, IOException {
		File xml = File.createTempFile("doclog", ".xml");
		xml.deleteOnExit();

		JAXBUtils.marshall(DOCLOG, xml);
		Doclog doclog = JAXBUtils.unmarshall(Doclog.class, xml);

		Assert.assertEquals(DOCLOG.size(), doclog.size());
	}
}
