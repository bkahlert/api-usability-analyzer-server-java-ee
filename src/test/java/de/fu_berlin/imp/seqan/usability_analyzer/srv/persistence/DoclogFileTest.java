package de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.Utils;

public class DoclogFileTest {

	private static final Logger LOGGER = Logger.getLogger(DoclogFileTest.class);

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test(expected = IllegalArgumentException.class)
	public void onlyWorksOnNotNull() throws IOException {
		new DoclogFile(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void onlyWorksOnFiles() throws IOException {
		new DoclogFile(temporaryFolder.getRoot());
	}

	@Test
	public void dontCreateAutomatically() throws IOException {
		File file = new File(File.createTempFile("doclog", "tmp")
				.getAbsoluteFile() + "_not_existant");
		assertNotNull(new DoclogFile(file));
		assertTrue(!file.exists());
	}

	@Test(expected = IllegalArgumentException.class)
	public void onlyWorksOnWritableDirectories() throws IOException {
		File file = File.createTempFile("doclog", "tmp");
		file.setReadOnly();
		new DoclogFile(file);
	}

	@Test
	public void writeDoclogRecords() throws IOException {
		File file = File.createTempFile("doclog", "doclog");
		IDoclogFile doclogFile = new DoclogFile(file);

		DoclogRecord[] doclogRecords = new DoclogRecord[] {
				Utils.DOCLOG_RECORD, Utils.DOCLOG_RECORD2,
				Utils.DOCLOG_RECORD3, Utils.DOCLOG_RECORD4 };

		// write records
		for (int i = 0; i < doclogRecords.length; i++) {
			assertEquals(i, doclogFile.getNumRecords());
			assertEquals(i, doclogFile.getRecords().length);
			doclogFile.write(doclogRecords[i]);
			assertEquals(i + 1, doclogFile.getNumRecords());
			assertEquals(i + 1, doclogFile.getRecords().length);
		}

		// read from same file
		DoclogRecord[] sameFileRecords = doclogFile.getRecords();
		assertEquals(doclogRecords.length, sameFileRecords.length);
		for (int i = 0; i < doclogRecords.length; i++) {
			assertEquals(doclogRecords[i], sameFileRecords[i]);
		}

		// read from different file managing the same resource
		IDoclogFile differentDoclogFile = new DoclogFile(file);
		DoclogRecord[] differentFileRecords = differentDoclogFile.getRecords();
		assertEquals(doclogRecords.length, differentFileRecords.length);
		for (int i = 0; i < doclogRecords.length; i++) {
			assertEquals(doclogRecords[i], differentFileRecords[i]);
		}
	}

	@Test
	public void writeDoclogRecordsConcurrently() throws IOException,
			InterruptedException {
		final int numRuns = 1000;
		final File f = File.createTempFile("doclog", ".doclog");

		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				// created once
				try {
					final IDoclogFile f1 = new DoclogFile(f);
					for (int i = 0; i < numRuns; i++) {
						LOGGER.info("A: Creating "
								+ DoclogRecord.class.getSimpleName());
						f1.write(Utils.DOCLOG_RECORD);
					}
				} catch (IOException e) {
					fail(e.getMessage());
				}
			}
		});

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < numRuns; i++) {
					// create n times
					try {
						final IDoclogFile f2 = new DoclogFile(f);
						LOGGER.info("B: Creating "
								+ DoclogRecord.class.getSimpleName());
						f2.write(Utils.DOCLOG_RECORD2);
					} catch (IOException e) {
						fail(e.getMessage());
					}
				}
			}
		});

		t1.start();
		t2.start();

		t1.join();
		t2.join();

		final IDoclogFile df = new DoclogFile(f);
		assertEquals(2 * numRuns, df.getNumRecords());
		assertEquals(2 * numRuns, df.getRecords().length);
		int equalsA = 0;
		int equalsB = 0;
		for (DoclogRecord doclogRecord : df.getRecords()) {
			if (doclogRecord.equals(Utils.DOCLOG_RECORD)) {
				equalsA++;
			} else if (doclogRecord.equals(Utils.DOCLOG_RECORD2)) {
				equalsB++;
			} else {
				fail(doclogRecord + " is does not equal one of the expected "
						+ DoclogRecord.class.getSimpleName());
			}
		}
		assertEquals(numRuns, equalsA);
		assertEquals(numRuns, equalsB);
	}

	@Test
	public void testGetAndDeleteRecord() throws IOException {
		File file = File.createTempFile("doclog", "doclog");
		IDoclogFile doclogFile = new DoclogFile(file);
		doclogFile.write(Utils.DOCLOG_RECORD);
		doclogFile.write(Utils.DOCLOG_RECORD2);
		doclogFile.write(Utils.DOCLOG_RECORD3);
		doclogFile.write(Utils.DOCLOG_RECORD4);
		// invalid since URL is missing
		FileUtils
				.write(file,
						"2012-08-29T17:31:37.430+02:00	READY		91.65.217.254	-	0	0	715	459\n",
						true);

		assertEquals(5, doclogFile.getNumRecords());

		// 1, 2, 3, 4, null
		assertEquals(Utils.DOCLOG_RECORD, doclogFile.getRecord(0));
		assertEquals(Utils.DOCLOG_RECORD2, doclogFile.getRecord(1));
		assertEquals(Utils.DOCLOG_RECORD3, doclogFile.getRecord(2));
		assertEquals(Utils.DOCLOG_RECORD4, doclogFile.getRecord(3));
		assertEquals(null, doclogFile.getRecord(4));

		assertEquals(null, doclogFile.getRecord(-1));
		assertEquals(Utils.DOCLOG_RECORD4, doclogFile.getRecord(-2));
		assertEquals(Utils.DOCLOG_RECORD3, doclogFile.getRecord(-3));
		assertEquals(Utils.DOCLOG_RECORD2, doclogFile.getRecord(-4));
		assertEquals(Utils.DOCLOG_RECORD, doclogFile.getRecord(-5));
		assertEquals(null, doclogFile.getRecord(-6));

		// 1, 3, 4, null
		assertTrue(doclogFile.deleteRecord(1));

		assertEquals(Utils.DOCLOG_RECORD, doclogFile.getRecord(0));
		assertEquals(Utils.DOCLOG_RECORD3, doclogFile.getRecord(1));
		assertEquals(Utils.DOCLOG_RECORD4, doclogFile.getRecord(2));
		assertEquals(null, doclogFile.getRecord(3));
		assertEquals(null, doclogFile.getRecord(4));

		// 1, 3, 4, null
		assertEquals(null, doclogFile.getRecord(-1));
		assertEquals(Utils.DOCLOG_RECORD4, doclogFile.getRecord(-2));
		assertEquals(Utils.DOCLOG_RECORD3, doclogFile.getRecord(-3));
		assertEquals(Utils.DOCLOG_RECORD, doclogFile.getRecord(-4));
		assertEquals(null, doclogFile.getRecord(-5));

		assertTrue(doclogFile.deleteRecord(-2));

		// 1, 3, null
		assertEquals(Utils.DOCLOG_RECORD, doclogFile.getRecord(0));
		assertEquals(Utils.DOCLOG_RECORD3, doclogFile.getRecord(1));
		assertEquals(null, doclogFile.getRecord(2));
		assertEquals(null, doclogFile.getRecord(3));

		assertEquals(null, doclogFile.getRecord(-1));
		assertEquals(Utils.DOCLOG_RECORD3, doclogFile.getRecord(-2));
		assertEquals(Utils.DOCLOG_RECORD, doclogFile.getRecord(-3));
		assertEquals(null, doclogFile.getRecord(-4));

		assertTrue(doclogFile.deleteRecord(-1));

		// 1, 3
		assertEquals(Utils.DOCLOG_RECORD, doclogFile.getRecord(0));
		assertEquals(Utils.DOCLOG_RECORD3, doclogFile.getRecord(1));
		assertEquals(null, doclogFile.getRecord(2));

		assertEquals(Utils.DOCLOG_RECORD3, doclogFile.getRecord(-1));
		assertEquals(Utils.DOCLOG_RECORD, doclogFile.getRecord(-2));
		assertEquals(null, doclogFile.getRecord(-3));
	}

	@Test
	public void mergeFromEmptyDoclogFile() throws IOException {
		final IDoclogFile dest = new DoclogFile(File.createTempFile("doclog",
				".doclog"));
		dest.write(Utils.DOCLOG_RECORD);

		final IDoclogFile src = new DoclogFile(File.createTempFile("doclog",
				".doclog"));

		dest.merge(src);

		try {
			src.getNumRecords();
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		try {
			src.getRecords();
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		assertEquals(1, dest.getNumRecords());
		assertEquals(1, dest.getRecords().length);
		DoclogRecord[] records = dest.getRecords();
		assertEquals(records[0], Utils.DOCLOG_RECORD);

		dest.write(Utils.DOCLOG_RECORD3);
		assertEquals(2, dest.getNumRecords());
		assertEquals(Utils.DOCLOG_RECORD3, dest.getRecords()[1]);

		dest.write(Utils.DOCLOG_RECORD4);
		assertEquals(3, dest.getNumRecords());
		assertEquals(Utils.DOCLOG_RECORD4, dest.getRecords()[2]);
	}

	@Test
	public void mergeIntoEmptyDoclogFile() throws IOException {
		final IDoclogFile dest = new DoclogFile(File.createTempFile("doclog",
				".doclog"));

		final IDoclogFile src = new DoclogFile(File.createTempFile("doclog",
				".doclog"));
		src.write(Utils.DOCLOG_RECORD);

		dest.merge(src);

		try {
			src.getNumRecords();
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		try {
			src.getRecords();
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		assertEquals(1, dest.getNumRecords());
		assertEquals(1, dest.getRecords().length);
		DoclogRecord[] records = dest.getRecords();
		assertEquals(records[0], Utils.DOCLOG_RECORD);

		dest.write(Utils.DOCLOG_RECORD3);
		assertEquals(2, dest.getNumRecords());
		assertEquals(Utils.DOCLOG_RECORD3, dest.getRecords()[1]);

		dest.write(Utils.DOCLOG_RECORD4);
		assertEquals(3, dest.getNumRecords());
		assertEquals(Utils.DOCLOG_RECORD4, dest.getRecords()[2]);
	}

	@Test
	public void mergeDoclogFile() throws IOException, InterruptedException {
		final int numEntries = 1000;

		final IDoclogFile dest = new DoclogFile(File.createTempFile("doclog",
				".doclog"));
		for (int i = 0; i < numEntries; i++) {
			dest.write(Utils.DOCLOG_RECORD);
		}

		final IDoclogFile src = new DoclogFile(File.createTempFile("doclog",
				".doclog"));
		for (int i = 0; i < numEntries; i++) {
			src.write(Utils.DOCLOG_RECORD2);
		}

		dest.merge(src);

		try {
			src.getNumRecords();
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		try {
			src.getRecords();
			fail();
		} catch (Throwable e) {
			assertTrue(true);
		}

		assertEquals(2 * numEntries, dest.getNumRecords());
		assertEquals(2 * numEntries, dest.getRecords().length);
		DoclogRecord[] records = dest.getRecords();
		for (int i = 0; i < numEntries; i++) {
			if (i < numEntries) {
				assertEquals(records[i], Utils.DOCLOG_RECORD2);
			} else {
				assertEquals(records[i], Utils.DOCLOG_RECORD);
			}
		}

		dest.write(Utils.DOCLOG_RECORD3);
		assertEquals(2 * numEntries + 1, dest.getNumRecords());
		assertEquals(Utils.DOCLOG_RECORD3, dest.getRecords()[2 * numEntries]);

		dest.write(Utils.DOCLOG_RECORD4);
		assertEquals(2 * numEntries + 2, dest.getNumRecords());
		assertEquals(Utils.DOCLOG_RECORD4,
				dest.getRecords()[2 * numEntries + 1]);
	}
}
