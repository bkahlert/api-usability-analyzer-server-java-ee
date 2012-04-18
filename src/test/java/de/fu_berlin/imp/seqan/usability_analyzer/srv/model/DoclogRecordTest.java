package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.joda.time.DateTimeZone;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.JAXBUtils;

public class DoclogRecordTest {

	public static final DoclogRecord DOCLOG_RECORD = new DoclogRecord(
			"http://www.seqan.de/dddoc/html_devel/INDEX_Shortcut_Iterators.html#PeptideIterator",
			"141.14.249.178", "192.168.0.1", DoclogAction.LINK,
			"http://www.bkahlert.com", DoclogRecord.ISO8601_simple
					.withOffsetParsed()
					.withZone(DateTimeZone.forOffsetHours(3))
					.parseDateTime("2011-09-13T12-09-04"), new Rectangle(0,
					132, 179, 477));

	@Test
	public void testMarshalling() throws JAXBException, IOException {
		File xml = File.createTempFile("doclogRecord", ".xml");
		xml.deleteOnExit();

		JAXBUtils.marshall(DOCLOG_RECORD, xml);
		DoclogRecord doclogRecord = JAXBUtils.unmarshall(DoclogRecord.class,
				xml);

		Assert.assertEquals(DOCLOG_RECORD.getUrl(), doclogRecord.getUrl());
		Assert.assertEquals(DOCLOG_RECORD.getIp(), doclogRecord.getIp());
		Assert.assertEquals(DOCLOG_RECORD.getProxyIp(),
				doclogRecord.getProxyIp());
		Assert.assertEquals(DOCLOG_RECORD.getAction(), doclogRecord.getAction());
		Assert.assertEquals(DOCLOG_RECORD.getActionParameter(),
				doclogRecord.getActionParameter());
		Assert.assertEquals(DOCLOG_RECORD.getDateTime(),
				doclogRecord.getDateTime());
		Assert.assertEquals(DOCLOG_RECORD.getBounds(), doclogRecord.getBounds());
	}
}
