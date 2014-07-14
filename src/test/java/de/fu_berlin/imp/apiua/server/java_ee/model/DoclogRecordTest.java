package de.fu_berlin.imp.apiua.server.java_ee.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogAction;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogRecord;
import de.fu_berlin.imp.apiua.server.java_ee.model.Rectangle;

@RunWith(value = Parameterized.class)
public class DoclogRecordTest {

	public static final DoclogRecord DOCLOG_RECORD = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", "192.168.0.1", DoclogAction.LINK,
			"http://www.bkahlert.com", DoclogRecord.ISO8601_simple
					.withOffsetParsed()
					.withZone(DateTimeZone.forOffsetHours(3))
					.parseDateTime("2011-09-13T12-09-04+05:30"), new Rectangle(
					0, 132, 179, 477));

	public static final DoclogRecord DOCLOG_RECORD2 = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178",
			"192.168.0.1",
			DoclogAction.TYPING,
			"text-Work in PROGRESS.\n\nWelcome to the \"Hello World\" of SeqAn. This is the first tutorial you should look at when starting to use our software library\n\n	This tutorial will briefly introduce you to basic concepts and explain certain design decisions.\nWe assume that you have some programming experience (preferably in C++ or C) and concentrate on SeqAn specific aspect.",
			DoclogRecord.ISO8601_simple.withOffsetParsed()
					.withZone(DateTimeZone.forOffsetHours(3))
					.parseDateTime("2011-09-13T12-09-04+05:30"), new Rectangle(
					0, 132, 179, 477));

	public static final DoclogRecord DOCLOG_RECORD3 = new DoclogRecord(
			"2011-09-13T12:09:04.952+05:30	"
					+ "TYPING-text-Work in PROGRESS.\\n\\nWelcome to the \"Hello World\" of SeqAn. This is the first tutorial you should look at when starting to use our software library\\n\\n\\tThis tutorial will briefly introduce you to basic concepts and explain certain design decisions.\\nWe assume that you have some programming experience (preferably in C++ or C) and concentrate on SeqAn specific aspect.	"
					+ "http://trac.seqan.de/wiki/Tutorial/FirstExamples	"
					+ "141.14.249.178	" + "192.168.0.1	" + "0	132	179	477");

	public static final DoclogRecord DOCLOG_RECORD4 = new DoclogRecord(
			"2012-11-15T09:37:02.000Z	READY	http://trac.seqan.de/wiki/HowTo/FixWhitespaceAutomatically	163.1.246.69	-	0	0	1425	813");

	public static final DoclogRecord DOCLOG_RECORD5 = new DoclogRecord(
			"2012-11-13T16:22:11.888-05:00	BLUR	http://trac.seqan.de/wiki/Tutorial/BackgroundAndMotivation	159.14.243.253	-	0	-2	1028	861");

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { DOCLOG_RECORD },
				{ DOCLOG_RECORD2 }, { DOCLOG_RECORD3 }, { DOCLOG_RECORD4 } };
		return Arrays.asList(data);
	}

	private DoclogRecord doclogRecord;

	public DoclogRecordTest(DoclogRecord doclogRecord) {
		this.doclogRecord = doclogRecord;
	}

	@Test
	public void testMarshalling() throws JAXBException, IOException {
		String marshalledLine = this.doclogRecord.toString();
		DoclogRecord doclogRecord = new DoclogRecord(marshalledLine);

		assertEquals(this.doclogRecord.getUrl(), doclogRecord.getUrl());
		assertEquals(this.doclogRecord.getIp(), doclogRecord.getIp());
		assertEquals(this.doclogRecord.getProxyIp(), doclogRecord.getProxyIp());
		assertEquals(this.doclogRecord.getAction(), doclogRecord.getAction());
		assertEquals(this.doclogRecord.getActionParameter(),
				doclogRecord.getActionParameter());
		assertEquals(this.doclogRecord.getDateTime(),
				doclogRecord.getDateTime());
		assertEquals(this.doclogRecord.getBounds(), doclogRecord.getBounds());

		assertEquals(this.doclogRecord, doclogRecord);
	}

}
