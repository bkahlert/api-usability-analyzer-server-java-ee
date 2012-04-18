package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;

public class DoclogKeyMapTest {

	public static final ID ID1 = new ID("id1");
	public static final ID ID2 = new ID("id2");
	public static final Fingerprint FINGERPRINT1 = new Fingerprint(
			"!fingerprint1");
	public static final Fingerprint FINGERPRINT2 = new Fingerprint(
			"!fingerprint2");
	public static final Fingerprint FINGERPRINT3 = new Fingerprint(
			"!fingerprint3");
	public static final Fingerprint FINGERPRINT4 = new Fingerprint(
			"!fingerprint4");
	public static final Fingerprint FINGERPRINT5 = new Fingerprint(
			"!fingerprint5");

	private DoclogKeyMap DOCLOG_KEY_MAP;

	public DoclogKeyMapTest() throws FinterprintAlreadyMappedException {
		DOCLOG_KEY_MAP = new DoclogKeyMap();
		DOCLOG_KEY_MAP.associate(FINGERPRINT1, ID1);
		DOCLOG_KEY_MAP.associate(FINGERPRINT2, ID1);
		DOCLOG_KEY_MAP.associate(FINGERPRINT3, ID2);
		DOCLOG_KEY_MAP.associate(FINGERPRINT4, ID2);
		DOCLOG_KEY_MAP.associate(FINGERPRINT5, ID2);
	}

	@Test
	public void testMarshalling() throws JAXBException, IOException {
		File xml = File.createTempFile("doclogKeyMap", ".xml");
		xml.deleteOnExit();

		DOCLOG_KEY_MAP.save(xml);
		DoclogKeyMap doclogKeyMap = DoclogKeyMap.load(xml);

		Assert.assertEquals(DOCLOG_KEY_MAP, doclogKeyMap);
	}
}
