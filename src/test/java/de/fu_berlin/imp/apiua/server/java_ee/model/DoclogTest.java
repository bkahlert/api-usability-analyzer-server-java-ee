package de.fu_berlin.imp.apiua.server.java_ee.model;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.apiua.server.java_ee.model.Doclog;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogAction;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogRecord;
import de.fu_berlin.imp.apiua.server.java_ee.model.ID;
import de.fu_berlin.imp.apiua.server.java_ee.model.Rectangle;

public class DoclogTest {

	public static final DoclogRecord doclogRecord1 = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", null, DoclogAction.READY, null,
			DoclogRecord.ISO8601_simple
					.parseDateTime("2011-09-13T12-07-53+02:00"), new Rectangle(
					0, 0, 179, 477));
	public static final DoclogRecord doclogRecord2 = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", "-", DoclogAction.SCROLL, null,
			DoclogRecord.ISO8601_simple
					.parseDateTime("2011-09-13T12-07-55+03:00"), new Rectangle(
					0, 132, 179, 477));
	public static final DoclogRecord doclogRecord3 = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", "192.168.0.1", DoclogAction.LINK,
			"http://www.bkahlert.com", DoclogRecord.ISO8601_simple
					.parseDateTime("2011-09-13T12-09-04-08:00"), new Rectangle(
					0, 132, 179, 477));
	public static final DoclogRecord doclogRecord4 = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", null, DoclogAction.UNLOAD, null,
			DoclogRecord.ISO8601_simple
					.parseDateTime("2011-09-13T12-09-04-08:30"), new Rectangle(
					0, 132, 179, 477));

	public static Doclog DOCLOG;

	public DoclogTest() {
		DOCLOG = new Doclog(new ID("test"));
		DOCLOG.add(doclogRecord1);
		DOCLOG.add(doclogRecord2);
		DOCLOG.add(doclogRecord3);
		DOCLOG.add(doclogRecord4);
	}

	@Test
	public void testMarshalling() throws JAXBException, IOException {
		File xml = File.createTempFile("doclog", ".xml");
		xml.deleteOnExit();

		StringBuffer marshalled = new StringBuffer();
		for (DoclogRecord doclogRecord : DOCLOG) {
			marshalled.append(doclogRecord.toString());
			marshalled.append("\n");
		}

		for (String line : marshalled.toString().split("\n")) {
			if (line.isEmpty())
				continue;
			DoclogRecord expected = DOCLOG.remove(0);
			DoclogRecord actual = new DoclogRecord(line);
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, DOCLOG.size());
	}
}
