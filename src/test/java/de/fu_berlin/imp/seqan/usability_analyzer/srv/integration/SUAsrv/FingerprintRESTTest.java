package de.fu_berlin.imp.seqan.usability_analyzer.srv.integration.SUAsrv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.jersey.api.client.UniformInterfaceException;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.AsyncTester;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.DoclogRESTUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.FingerprintRESTUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.TestConfiguration;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.Utils;

@RunWith(value = Parameterized.class)
public class FingerprintRESTTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() throws Exception {
		List<Object[]> parameters = new ArrayList<Object[]>();
		for (URL suaSrv : TestConfiguration.getSUAsrvURLs()) {
			parameters.add(new Object[] { suaSrv });
		}
		return parameters;
	}

	private URL suaSrv;

	public FingerprintRESTTest(URL suaSrv) {
		this.suaSrv = suaSrv;
	}

	@Test
	public void testGetID() throws Exception {
		Fingerprint unmappedFingerprint = Utils.getTestFingerprint();
		assertNull(FingerprintRESTUtils.getID(suaSrv, unmappedFingerprint));

		Fingerprint mappedFingerprint = Utils.getTestFingerprint();
		ID id = Utils.getTestID();
		assertEquals(Utils.DOCLOG_RECORD, DoclogRESTUtils.createDoclogRecord(
				mappedFingerprint, id, Utils.DOCLOG_RECORD));

		assertNull(FingerprintRESTUtils.getID(suaSrv, unmappedFingerprint));
		assertEquals(id, FingerprintRESTUtils.getID(suaSrv, mappedFingerprint));
	}

	@Test(expected = UniformInterfaceException.class)
	public void testInvalidChangeFingerprint() throws Exception {
		FingerprintRESTUtils.associate(suaSrv, Utils.getTestFingerprint(),
				"!undefined");
	}

	@Test
	public void testChangeFingerprint() throws Exception {
		ID id = Utils.getTestID();
		Fingerprint f1 = Utils.getTestFingerprint();
		Fingerprint f2 = Utils.getTestFingerprint();
		Fingerprint f3 = Utils.getTestFingerprint();

		// Create with f1
		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.createDoclogRecord(f1, Utils.DOCLOG_RECORD));

		assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f1, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f3, 0));

		// Associate f2 with f1
		assertTrue(FingerprintRESTUtils.associate(suaSrv, f1, f2));

		assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 0));
		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f2, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f3, 0));

		// Map ID to f2
		assertEquals(Utils.DOCLOG_RECORD2, DoclogRESTUtils.createDoclogRecord(
				f2, id, Utils.DOCLOG_RECORD2));

		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 0));
		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f2, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f3, 0));

		assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(id, 1));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 1));
		assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(f2, 1));
		assertNull(DoclogRESTUtils.readDoclogRecord(f3, 1));

		// Associate f3 with f2
		assertTrue(FingerprintRESTUtils.associate(suaSrv, f2, f3));

		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 0));
		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f3, 0));

		assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(id, 1));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 1));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 1));
		assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(f3, 1));

		// Create with f3
		assertEquals(Utils.DOCLOG_RECORD3,
				DoclogRESTUtils.createDoclogRecord(f3, Utils.DOCLOG_RECORD3));

		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 0));
		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f3, 0));

		assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(id, 1));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 1));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 1));
		assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(f3, 1));

		assertEquals(Utils.DOCLOG_RECORD3,
				DoclogRESTUtils.readDoclogRecord(id, 2));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 2));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 2));
		assertEquals(Utils.DOCLOG_RECORD3,
				DoclogRESTUtils.readDoclogRecord(f3, 2));

		/*
		 * Delete
		 */
		assertFalse(DoclogRESTUtils.deleteDoclogRecord(f2, -2));
		assertTrue(DoclogRESTUtils.deleteDoclogRecord(f3, -2));
		assertTrue(DoclogRESTUtils.deleteDoclogRecord(id, -1));
		assertTrue(DoclogRESTUtils.deleteDoclogRecord(f3, 0));

		assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f3, 0));
		assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
		assertEquals(0, DoclogRESTUtils.readDoclog(f1).size());
		assertEquals(0, DoclogRESTUtils.readDoclog(f2).size());
		assertEquals(0, DoclogRESTUtils.readDoclog(f3).size());
	}

	@Test
	public void testChangeFingerprintFast() throws Exception {
		final ID id = Utils.getTestID();
		final Fingerprint f1 = Utils.getTestFingerprint();
		final Fingerprint f2 = Utils.getTestFingerprint();
		final Fingerprint f3 = Utils.getTestFingerprint();

		AsyncTester f1Create = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Create with f1
				assertEquals(Utils.DOCLOG_RECORD,
						DoclogRESTUtils.createDoclogRecord(f1,
								Utils.DOCLOG_RECORD));
				return null;
			}
		});

		AsyncTester f2associatef1 = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Associate f2 with f1
				assertTrue(FingerprintRESTUtils.associate(suaSrv, f1, f2));
				return null;
			}
		});

		AsyncTester mapIDtof2 = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Map ID to f2
				assertEquals(Utils.DOCLOG_RECORD2,
						DoclogRESTUtils.createDoclogRecord(f2, id,
								Utils.DOCLOG_RECORD2));
				return null;
			}
		});

		AsyncTester f2associatef3 = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Associate f3 with f2
				assertTrue(FingerprintRESTUtils.associate(suaSrv, f2, f3));
				return null;
			}
		});

		AsyncTester f3Create = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// Create with f3
				assertEquals(Utils.DOCLOG_RECORD3,
						DoclogRESTUtils.createDoclogRecord(f3,
								Utils.DOCLOG_RECORD3));
				return null;
			}
		});

		AsyncTester delete = new AsyncTester(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				/*
				 * Delete
				 */
				assertFalse(DoclogRESTUtils.deleteDoclogRecord(f2, -2));
				assertTrue(DoclogRESTUtils.deleteDoclogRecord(f3, -2));
				assertTrue(DoclogRESTUtils.deleteDoclogRecord(id, -1));
				assertTrue(DoclogRESTUtils.deleteDoclogRecord(f3, 0));
				return null;
			}
		});

		f1Create.start();
		f1Create.join();

		assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f1, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f3, 0));

		// TODO decrease needed waiting time
		f2associatef1.start();
		Thread.sleep(500);
		mapIDtof2.start();
		Thread.sleep(500);
		f2associatef3.start();
		Thread.sleep(500);
		f3Create.start();

		f2associatef1.join();
		mapIDtof2.join();
		f2associatef3.join();
		f3Create.join();

		// ... we can only test the final but no intermediate results.
		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 0));
		assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f3, 0));

		assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(id, 1));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 1));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 1));
		assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(f3, 1));

		assertEquals(Utils.DOCLOG_RECORD3,
				DoclogRESTUtils.readDoclogRecord(id, 2));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 2));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 2));
		assertEquals(Utils.DOCLOG_RECORD3,
				DoclogRESTUtils.readDoclogRecord(f3, 2));

		delete.start();
		delete.join();

		assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f1, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f2, 0));
		assertNull(DoclogRESTUtils.readDoclogRecord(f3, 0));
		assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
		assertEquals(0, DoclogRESTUtils.readDoclog(f1).size());
		assertEquals(0, DoclogRESTUtils.readDoclog(f2).size());
		assertEquals(0, DoclogRESTUtils.readDoclog(f3).size());
	}
}
