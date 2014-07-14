package de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.concurrent.Callable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.DoclogPersistence.DoclogPersistenceException;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.AsyncTester;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.Utils;

public class MappingDoclogPersistenceTest {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void testGetID() throws DoclogPersistenceException,
			FileNotFoundException {
		MappingDoclogPersistence p = new MappingDoclogPersistence(
				temporaryFolder.getRoot());

		Fingerprint f1 = Utils.getTestFingerprint();
		assertNull(p.getMapping(f1));

		Fingerprint f2 = Utils.getTestFingerprint();
		ID id = Utils.getTestID();
		p.write(f2, id, Utils.DOCLOG_RECORD);
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f2, -1));
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(id, -1));

		assertNull(p.getMapping(f1));
		assertEquals(id, p.getMapping(f2));
	}

	@Test
	public void testChangeFingerprint() throws DoclogPersistenceException,
			FileNotFoundException {
		MappingDoclogPersistence p = new MappingDoclogPersistence(
				temporaryFolder.getRoot());

		ID id = Utils.getTestID();
		Fingerprint f1 = Utils.getTestFingerprint();
		Fingerprint f2 = Utils.getTestFingerprint();
		Fingerprint f3 = Utils.getTestFingerprint();

		// Create with f1
		p.write(f1, Utils.DOCLOG_RECORD);
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f1, -1));
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f1, 0));
		assertEquals(0, p.getNumRecords(id));
		assertEquals(0, p.getNumRecords(f2));
		assertEquals(0, p.getNumRecords(f3));

		// Create with f2
		p.write(f2, Utils.DOCLOG_RECORD2);
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f1, -1));
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f1, 0));
		assertEquals(0, p.getNumRecords(id));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(f2, -1));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(f2, 0));
		assertEquals(0, p.getNumRecords(f3));

		// Associate f2 with f1
		p.associateFingerprints(f1, f2);

		assertNull(p.getRecord(id, 0));
		assertNull(p.getRecord(f1, 0));
		assertEquals(2, p.getNumRecords(f2));
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f2, 0));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(f2, 1));
		assertNull(p.getRecord(f3, 0));

		// Map ID to f2
		p.write(f2, id, Utils.DOCLOG_RECORD3);

		assertEquals(0, p.getNumRecords(f1));
		assertEquals(3, p.getNumRecords(f2));
		assertEquals(3, p.getNumRecords(id));
		assertEquals(0, p.getNumRecords(f3));

		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(id, 0));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(id, 1));
		assertEquals(Utils.DOCLOG_RECORD3, p.getRecord(id, 2));
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f2, 0));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(f2, 1));
		assertEquals(Utils.DOCLOG_RECORD3, p.getRecord(f2, 2));

		// Associate f3 with f2
		p.associateFingerprints(f2, f3);

		assertEquals(0, p.getNumRecords(f1));
		assertEquals(0, p.getNumRecords(f2));
		assertEquals(3, p.getNumRecords(id));
		assertEquals(3, p.getNumRecords(f3));

		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(id, 0));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(id, 1));
		assertEquals(Utils.DOCLOG_RECORD3, p.getRecord(id, 2));
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f3, 0));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(f3, 1));
		assertEquals(Utils.DOCLOG_RECORD3, p.getRecord(f3, 2));

		// Create with f3
		p.write(f3, Utils.DOCLOG_RECORD4);

		assertEquals(0, p.getNumRecords(f1));
		assertEquals(0, p.getNumRecords(f2));
		assertEquals(4, p.getNumRecords(id));
		assertEquals(4, p.getNumRecords(f3));

		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(id, 0));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(id, 1));
		assertEquals(Utils.DOCLOG_RECORD3, p.getRecord(id, 2));
		assertEquals(Utils.DOCLOG_RECORD4, p.getRecord(id, 3));
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f3, 0));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(f3, 1));
		assertEquals(Utils.DOCLOG_RECORD3, p.getRecord(f3, 2));
		assertEquals(Utils.DOCLOG_RECORD4, p.getRecord(id, 3));

		/*
		 * Delete
		 */
		assertFalse(p.deleteRecord(f2, 0));
		assertTrue(p.deleteRecord(f3, -3));
		assertTrue(p.deleteRecord(f3, -2));
		assertTrue(p.deleteRecord(id, -1));
		assertTrue(p.deleteRecord(f3, 0));

		assertNull(p.getRecord(id, 0));
		assertNull(p.getRecord(f1, 0));
		assertNull(p.getRecord(f2, 0));
		assertNull(p.getRecord(f3, 0));
		assertEquals(0, p.getDoclog(id).size());
		assertEquals(0, p.getDoclog(f1).size());
		assertEquals(0, p.getDoclog(f2).size());
		assertEquals(0, p.getDoclog(f3).size());
	}

	@Test
	public void testChangeFingerprintFast() throws Exception {
		final MappingDoclogPersistence p = new MappingDoclogPersistence(
				temporaryFolder.getRoot());

		final ID id = Utils.getTestID();
		final Fingerprint f1 = Utils.getTestFingerprint();
		final Fingerprint f2 = Utils.getTestFingerprint();
		final Fingerprint f3 = Utils.getTestFingerprint();

		AsyncTester f1Create = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Create with f1
				p.write(f1, Utils.DOCLOG_RECORD);
				return null;
			}
		});

		AsyncTester f2associatef1 = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Associate f2 with f1
				p.associateFingerprints(f1, f2);
				return null;
			}
		});

		AsyncTester mapIDtof2 = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Map ID to f2
				p.write(f2, id, Utils.DOCLOG_RECORD2);
				return null;
			}
		});

		AsyncTester f2associatef3 = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Associate f3 with f2
				p.associateFingerprints(f2, f3);
				return null;
			}
		});

		AsyncTester f3Create = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Create with f3
				p.write(f3, Utils.DOCLOG_RECORD3);
				return null;
			}
		});

		AsyncTester delete = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				/*
				 * Delete
				 */
				assertTrue(p.deleteRecord(f3, -2));
				assertFalse(p.deleteRecord(f2, -2));
				assertTrue(p.deleteRecord(id, -1));
				assertTrue(p.deleteRecord(f3, 0));
				return null;
			}
		});

		f1Create.start();
		f1Create.join();

		assertNull(p.getRecord(id, 0));
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f1, 0));
		assertNull(p.getRecord(f2, 0));
		assertNull(p.getRecord(f3, 0));

		// Since the execution of the following tasks the their assertions
		// aren't atomic...
		f2associatef1.start();
		Thread.sleep(200); // FIXME
		mapIDtof2.start();
		Thread.sleep(200); // FIXME
		f2associatef3.start();
		Thread.sleep(200); // FIXME
		f3Create.start();

		f2associatef1.join();
		mapIDtof2.join();
		f2associatef3.join();
		f3Create.join();

		// ... we can only test the final but no intermediate results.
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(id, -3));
		assertNull(p.getRecord(f1, -3));
		assertEquals(0, p.getNumRecords(f2));
		assertEquals(Utils.DOCLOG_RECORD, p.getRecord(f3, -3));

		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(id, -2));
		assertNull(p.getRecord(f1, -2));
		assertEquals(0, p.getNumRecords(f2));
		assertEquals(Utils.DOCLOG_RECORD2, p.getRecord(f3, -2));

		assertEquals(Utils.DOCLOG_RECORD3, p.getRecord(id, -1));
		assertNull(p.getRecord(f1, -1));
		assertEquals(0, p.getNumRecords(f2));
		assertEquals(Utils.DOCLOG_RECORD3, p.getRecord(f3, -1));

		delete.start();
		delete.join();

		assertNull(p.getRecord(id, 0));
		assertNull(p.getRecord(f1, 0));
		assertNull(p.getRecord(f2, 0));
		assertNull(p.getRecord(f3, 0));
		assertEquals(0, p.getDoclog(id).size());
		assertEquals(0, p.getDoclog(f1).size());
		assertEquals(0, p.getDoclog(f2).size());
		assertEquals(0, p.getDoclog(f3).size());
	}
}
