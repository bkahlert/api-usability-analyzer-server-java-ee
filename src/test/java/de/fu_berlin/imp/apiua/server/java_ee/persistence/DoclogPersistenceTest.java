package de.fu_berlin.imp.apiua.server.java_ee.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogRecord;
import de.fu_berlin.imp.apiua.server.java_ee.model.Fingerprint;
import de.fu_berlin.imp.apiua.server.java_ee.model.ID;
import de.fu_berlin.imp.apiua.server.java_ee.model.IIdentifier;
import de.fu_berlin.imp.apiua.server.java_ee.persistence.DoclogPersistence;
import de.fu_berlin.imp.apiua.server.java_ee.persistence.DoclogPersistence.DoclogPersistenceException;
import de.fu_berlin.imp.apiua.server.java_ee.utils.Utils;

public class DoclogPersistenceTest {

	private static final Logger LOGGER = Logger.getLogger(DoclogFileTest.class);

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test(expected = IllegalArgumentException.class)
	public void onlyWorksOnNotNull() throws IOException {
		new DoclogPersistence(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void onlyWorksOnDirectories() throws IOException {
		new DoclogPersistence(File.createTempFile("doclogPersistence", "tmp"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void onlyWorksOnWritableDirectories() throws IOException {
		File dir = temporaryFolder.getRoot();
		dir.setReadOnly();
		new DoclogPersistence(dir);
	}

	@Test
	public void writeDoclogRecordIllegaly() throws FileNotFoundException {
		File tempDirectory = temporaryFolder.getRoot();
		DoclogPersistence doclogPersistence = new DoclogPersistence(
				tempDirectory);
		IIdentifier id = Utils.getTestID();

		try {
			doclogPersistence.write(id, null);
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		try {
			doclogPersistence.write(null, Utils.DOCLOG_RECORD);
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		try {
			doclogPersistence.write(null, null);
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		try {
			doclogPersistence.write(new Fingerprint("!null"), null);
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}
		try {
			doclogPersistence.write(new Fingerprint("!undefined"), null);
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		try {
			doclogPersistence.write(new ID("null"), null);
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}
		try {
			doclogPersistence.write(new ID("undefined"), null);
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}
	}

	@Test
	public void writeDoclogRecord() throws FileNotFoundException,
			DoclogPersistenceException {
		File tempDirectory = temporaryFolder.getRoot();
		DoclogPersistence doclogPersistence = new DoclogPersistence(
				tempDirectory);
		IIdentifier id = Utils.getTestID();

		assertEquals(0, doclogPersistence.getNumRecords(id));
		doclogPersistence.write(id, Utils.DOCLOG_RECORD);
		assertEquals(1, doclogPersistence.getNumRecords(id));
		assertEquals(Utils.DOCLOG_RECORD, doclogPersistence.getRecords(id)[0]);

		doclogPersistence.write(id, Utils.DOCLOG_RECORD2);
		assertEquals(2, doclogPersistence.getNumRecords(id));

		doclogPersistence.write(id, Utils.DOCLOG_RECORD3);
		assertEquals(3, doclogPersistence.getNumRecords(id));

		assertEquals(null, doclogPersistence.getRecord(null, 0));
		assertEquals(null, doclogPersistence.getRecord(null, 1));
		assertEquals(null, doclogPersistence.getRecord(null, -1));

		assertEquals(Utils.DOCLOG_RECORD, doclogPersistence.getRecord(id, 0));
		assertEquals(Utils.DOCLOG_RECORD2, doclogPersistence.getRecord(id, 1));
		assertEquals(Utils.DOCLOG_RECORD3, doclogPersistence.getRecord(id, 2));
		assertEquals(null, doclogPersistence.getRecord(id, 3));
		assertEquals(null, doclogPersistence.getRecord(id, 4));

		assertEquals(Utils.DOCLOG_RECORD3, doclogPersistence.getRecord(id, -1));
		assertEquals(Utils.DOCLOG_RECORD2, doclogPersistence.getRecord(id, -2));
		assertEquals(Utils.DOCLOG_RECORD, doclogPersistence.getRecord(id, -3));
		assertEquals(null, doclogPersistence.getRecord(id, 3));
		assertEquals(null, doclogPersistence.getRecord(id, 4));
	}

	@Test
	public void writeDoclogRecordsConcurrently()
			throws DoclogPersistenceException, FileNotFoundException,
			InterruptedException {
		final int numRuns = 1000;
		final IIdentifier id1 = Utils.getTestID();
		final IIdentifier id2 = Utils.getTestFingerprint();

		final DoclogPersistence p = new DoclogPersistence(
				temporaryFolder.getRoot());
		assertEquals(0, p.getNumRecords(id1));
		assertEquals(0, p.getNumRecords(id2));

		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < numRuns; i++) {
					LOGGER.info(id1 + ": Creating "
							+ DoclogRecord.class.getSimpleName());
					try {
						p.write(id1, Utils.DOCLOG_RECORD);
						p.write(id2, Utils.DOCLOG_RECORD2);
					} catch (DoclogPersistenceException e) {
						fail();
					}
				}
			}
		});

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < numRuns; i++) {
					LOGGER.info(id2 + ": Creating "
							+ DoclogRecord.class.getSimpleName());
					try {
						p.write(id1, Utils.DOCLOG_RECORD2);
						p.write(id2, Utils.DOCLOG_RECORD);
					} catch (DoclogPersistenceException e) {
						fail();
					}
				}
			}
		});

		t1.start();
		t2.start();

		t1.join();
		t2.join();

		assertTrue(p.getNumRecords(id1) > 0);
		assertTrue(p.getNumRecords(id2) > 0);

		for (IIdentifier id : Arrays.asList(id1, id2)) {
			assertEquals(2 * numRuns, p.getNumRecords(id));
			int equalsRecord1 = 0;
			int equalsRecord2 = 0;
			for (DoclogRecord doclogRecord : p.getRecords(id1)) {
				if (doclogRecord.equals(Utils.DOCLOG_RECORD)) {
					equalsRecord1++;
				} else if (doclogRecord.equals(Utils.DOCLOG_RECORD2)) {
					equalsRecord2++;
				} else {
					fail(doclogRecord
							+ " is does not equal one of the expected "
							+ DoclogRecord.class.getSimpleName());
				}
			}
			assertEquals(numRuns, equalsRecord1);
			assertEquals(numRuns, equalsRecord2);
		}
	}

	@Test
	public void testGetIdentifiers() throws FileNotFoundException,
			DoclogPersistenceException {
		IIdentifier id1 = new ID("id1");
		IIdentifier id2 = new ID("id2");
		IIdentifier id3 = new ID("id3");

		final DoclogPersistence p = new DoclogPersistence(
				temporaryFolder.getRoot());
		assertEquals(0, p.getIdentifiers().length);

		p.write(id1, Utils.DOCLOG_RECORD);
		assertEquals(1, p.getIdentifiers().length);
		assertTrue(ArrayUtils.contains(p.getIdentifiers(), id1));

		p.write(id1, Utils.DOCLOG_RECORD2);
		assertEquals(1, p.getIdentifiers().length);
		assertTrue(ArrayUtils.contains(p.getIdentifiers(), id1));

		p.write(id2, Utils.DOCLOG_RECORD);
		assertEquals(2, p.getIdentifiers().length);
		assertTrue(ArrayUtils.contains(p.getIdentifiers(), id1));
		assertTrue(ArrayUtils.contains(p.getIdentifiers(), id2));

		p.write(id2, Utils.DOCLOG_RECORD);
		assertEquals(2, p.getIdentifiers().length);
		assertTrue(ArrayUtils.contains(p.getIdentifiers(), id1));
		assertTrue(ArrayUtils.contains(p.getIdentifiers(), id2));

		p.write(id3, Utils.DOCLOG_RECORD4);
		assertEquals(3, p.getIdentifiers().length);
		assertTrue(ArrayUtils.contains(p.getIdentifiers(), id3));
		assertTrue(ArrayUtils.contains(p.getIdentifiers(), id3));
		assertTrue(ArrayUtils.contains(p.getIdentifiers(), id3));
	}

	@Test(expected = DoclogPersistenceException.class)
	public void testLongID() throws FileNotFoundException,
			DoclogPersistenceException {
		IIdentifier id = new ID(Utils.getRandomString(500));

		final DoclogPersistence p = new DoclogPersistence(
				temporaryFolder.getRoot());
		assertEquals(0, p.getIdentifiers().length);

		p.write(id, Utils.DOCLOG_RECORD);
	}

	@Test
	public void getDoclog() throws FileNotFoundException,
			DoclogPersistenceException {
		final DoclogPersistence p = new DoclogPersistence(
				temporaryFolder.getRoot());
		final IIdentifier id = new ID("myID");

		assertEquals(id, p.getDoclog(id).getIdentifier());
		assertEquals(0, p.getDoclog(id).size());

		p.write(id, Utils.DOCLOG_RECORD);
		assertEquals(id, p.getDoclog(id).getIdentifier());
		assertEquals(1, p.getDoclog(id).getDoclogRecords().size());
		assertEquals(1, p.getIdentifiers().length);

		p.write(id, Utils.DOCLOG_RECORD2);
		assertEquals(id, p.getDoclog(id).getIdentifier());
		assertEquals(2, p.getDoclog(id).getDoclogRecords().size());
		assertEquals(1, p.getIdentifiers().length);
	}
}
