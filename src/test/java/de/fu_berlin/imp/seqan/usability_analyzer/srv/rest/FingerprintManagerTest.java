package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.AsyncTester;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;

public class FingerprintManagerTest {

	private WebResource getFingerprintService() {
		URI uri = UriBuilder.fromUri(
				Utils.getBaseURI().toString() + "/rest/fingerprint").build();
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		return client.resource(uri);
	}

	/**
	 * @param fingerprint
	 * @return
	 */
	private String getID(Fingerprint fingerprint) {
		WebResource doclogService = getFingerprintService();

		try {
			return doclogService.path(fingerprint.toString())
					.accept(MediaType.TEXT_PLAIN).get(String.class);
		} catch (UniformInterfaceException e) {
			return null;
		}
	}

	/**
	 * Associates a {@link Fingerprint}'s mapped {@link ID} with another
	 * {@link Fingerprint}
	 * 
	 * @param fingerprint1
	 *            the {@link Fingerprint} the {@link ID} is read from
	 * @param fingerprint2
	 *            the new {@link Fingerprint}
	 * @return
	 */
	private boolean associate(Fingerprint fingerprint1, Fingerprint fingerprint2) {
		WebResource doclogService = getFingerprintService();

		return Boolean.valueOf(doclogService.path(fingerprint1.toString())
				.path("associate").path(fingerprint2.toString())
				.accept(MediaType.TEXT_PLAIN).get(String.class));
	}

	@Test
	public void testGetID() {
		Fingerprint unmappedFingerprint = DoclogManagerTest
				.getTestFingerprint();
		assertNull(getID(unmappedFingerprint));

		Fingerprint mappedFingerprint = DoclogManagerTest.getTestFingerprint();
		ID id = DoclogManagerTest.getTestID();
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.createDoclogRecord(mappedFingerprint, id,
						DoclogManagerTest.DOCLOG_RECORD));

		assertNull(getID(unmappedFingerprint));
		assertEquals(id.toString(), getID(mappedFingerprint));
	}

	@Test
	public void testChangeFingerprint() {
		ID id = DoclogManagerTest.getTestID();
		Fingerprint f1 = DoclogManagerTest.getTestFingerprint();
		Fingerprint f2 = DoclogManagerTest.getTestFingerprint();
		Fingerprint f3 = DoclogManagerTest.getTestFingerprint();

		// Create with f1
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.createDoclogRecord(f1,
						DoclogManagerTest.DOCLOG_RECORD));

		assertNull(DoclogManagerTest.readDoclogRecord(id, 0));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f1, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f2, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f3, 0));

		// Associate f2 with f1
		assertTrue(associate(f1, f2));

		assertNull(DoclogManagerTest.readDoclogRecord(id, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, 0));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f2, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f3, 0));

		// Map ID to f2
		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.createDoclogRecord(f2, id,
						DoclogManagerTest.DOCLOG_RECORD2));

		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(id, -2));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -2));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f2, -2));
		assertNull(DoclogManagerTest.readDoclogRecord(f3, -2));

		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(id, -1));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -1));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(f2, -1));
		assertNull(DoclogManagerTest.readDoclogRecord(f3, -1));

		// Associate f3 with f2
		assertTrue(associate(f2, f3));

		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(id, -2));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -2));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f2, -2));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f3, -2));

		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(id, -1));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -1));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(f2, -1));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f3, -2));

		// Create with f3
		assertEquals(DoclogManagerTest.DOCLOG_RECORD3,
				DoclogManagerTest.createDoclogRecord(f3,
						DoclogManagerTest.DOCLOG_RECORD3));

		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(id, -3));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -3));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f2, -3));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f3, -3));

		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(id, -2));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -2));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(f2, -2));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(f3, -2));

		assertEquals(DoclogManagerTest.DOCLOG_RECORD3,
				DoclogManagerTest.readDoclogRecord(id, -1));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -1));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD3,
				DoclogManagerTest.readDoclogRecord(f2, -1));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD3,
				DoclogManagerTest.readDoclogRecord(f3, -1));

		/*
		 * Delete
		 */
		assertTrue(DoclogManagerTest.deleteDoclogRecord(f2, -2));
		assertTrue(DoclogManagerTest.deleteDoclogRecord(id, -1));
		assertTrue(DoclogManagerTest.deleteDoclogRecord(f3, 0));

		assertNull(DoclogManagerTest.readDoclogRecord(id, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f2, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f3, 0));
		assertNull(DoclogManagerTest.readDoclog(id));
		assertNull(DoclogManagerTest.readDoclog(f1));
		assertNull(DoclogManagerTest.readDoclog(f2));
		assertNull(DoclogManagerTest.readDoclog(f3));
	}

	@Test
	public void testChangeFingerprintFast() throws InterruptedException {
		final ID id = DoclogManagerTest.getTestID();
		final Fingerprint f1 = DoclogManagerTest.getTestFingerprint();
		final Fingerprint f2 = DoclogManagerTest.getTestFingerprint();
		final Fingerprint f3 = DoclogManagerTest.getTestFingerprint();

		AsyncTester f1Create = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				// Create with f1
				assertEquals(DoclogManagerTest.DOCLOG_RECORD,
						DoclogManagerTest.createDoclogRecord(f1,
								DoclogManagerTest.DOCLOG_RECORD));
			}
		});

		AsyncTester f2associatef1 = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				// Associate f2 with f1
				assertTrue(associate(f1, f2));
			}
		});

		AsyncTester mapIDtof2 = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				// Map ID to f2
				assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
						DoclogManagerTest.createDoclogRecord(f2, id,
								DoclogManagerTest.DOCLOG_RECORD2));
			}
		});

		AsyncTester f2associatef3 = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				// Associate f3 with f2
				assertTrue(associate(f2, f3));
			}
		});

		AsyncTester f3Create = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				// Create with f3
				assertEquals(DoclogManagerTest.DOCLOG_RECORD3,
						DoclogManagerTest.createDoclogRecord(f3,
								DoclogManagerTest.DOCLOG_RECORD3));
			}
		});

		AsyncTester delete = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				/*
				 * Delete
				 */
				assertTrue(DoclogManagerTest.deleteDoclogRecord(f2, -2));
				assertTrue(DoclogManagerTest.deleteDoclogRecord(id, -1));
				assertTrue(DoclogManagerTest.deleteDoclogRecord(f3, 0));
			}
		});

		f1Create.start();
		f1Create.join();

		assertNull(DoclogManagerTest.readDoclogRecord(id, 0));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f1, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f2, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f3, 0));

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
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(id, -3));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -3));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f2, -3));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD,
				DoclogManagerTest.readDoclogRecord(f3, -3));

		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(id, -2));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -2));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(f2, -2));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD2,
				DoclogManagerTest.readDoclogRecord(f3, -2));

		assertEquals(DoclogManagerTest.DOCLOG_RECORD3,
				DoclogManagerTest.readDoclogRecord(id, -1));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, -1));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD3,
				DoclogManagerTest.readDoclogRecord(f2, -1));
		assertEquals(DoclogManagerTest.DOCLOG_RECORD3,
				DoclogManagerTest.readDoclogRecord(f3, -1));

		delete.start();
		delete.join();

		assertNull(DoclogManagerTest.readDoclogRecord(id, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f1, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f2, 0));
		assertNull(DoclogManagerTest.readDoclogRecord(f3, 0));
		assertNull(DoclogManagerTest.readDoclog(id));
		assertNull(DoclogManagerTest.readDoclog(f1));
		assertNull(DoclogManagerTest.readDoclog(f2));
		assertNull(DoclogManagerTest.readDoclog(f3));
	}
}
