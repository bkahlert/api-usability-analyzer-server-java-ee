package de.fu_berlin.imp.apiua.server.java_ee.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogKeyMap;
import de.fu_berlin.imp.apiua.server.java_ee.model.Fingerprint;
import de.fu_berlin.imp.apiua.server.java_ee.model.FinterprintAlreadyMappedException;
import de.fu_berlin.imp.apiua.server.java_ee.model.ID;

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

	@Before
	public void prepare() throws FinterprintAlreadyMappedException {
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

	@Test
	public void testAddRemove() throws FinterprintAlreadyMappedException {
		DoclogKeyMap doclogKeyMap = new DoclogKeyMap();

		assertNull(doclogKeyMap.getID(FINGERPRINT1));
		assertNull(doclogKeyMap.getID(FINGERPRINT2));

		doclogKeyMap.associate(FINGERPRINT1, ID1);
		assertEquals(ID1, doclogKeyMap.getID(FINGERPRINT1));
		assertNull(doclogKeyMap.getID(FINGERPRINT2));

		doclogKeyMap.associate(FINGERPRINT2, ID1);
		assertEquals(ID1, doclogKeyMap.getID(FINGERPRINT1));
		assertEquals(ID1, doclogKeyMap.getID(FINGERPRINT2));

		doclogKeyMap.deassociate(FINGERPRINT1);
		assertNull(doclogKeyMap.getID(FINGERPRINT1));
		assertEquals(ID1, doclogKeyMap.getID(FINGERPRINT2));

		doclogKeyMap.deassociate(FINGERPRINT2);
		assertNull(doclogKeyMap.getID(FINGERPRINT1));
		assertNull(doclogKeyMap.getID(FINGERPRINT1));
	}
}
