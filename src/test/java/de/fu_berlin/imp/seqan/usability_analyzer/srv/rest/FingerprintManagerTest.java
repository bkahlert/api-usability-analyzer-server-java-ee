package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import junit.framework.Assert;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
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

	/**
	 * Creates a lot of {@link DoclogRecord}s and changes every here and now the
	 * {@link Fingerprint}.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void changeFingerprintLoadTest() throws InterruptedException {
		int cores = Runtime.getRuntime().availableProcessors();
		final int runtimeCycles = cores * 20;
		final int numFingerprintChanges = 1;

		final AtomicReference<Fingerprint> fingerprint = new AtomicReference<Fingerprint>(
				DoclogManagerTest.getTestFingerprint());

		ExecutorService executorService = Executors
				.newFixedThreadPool(cores * 5);
		Set<Callable<Void>> createTasks = new HashSet<Callable<Void>>();
		Set<Callable<Void>> readTasks = new HashSet<Callable<Void>>();
		Set<Callable<Void>> deleteTasks = new HashSet<Callable<Void>>();

		for (int i = 0; i < runtimeCycles; i++) {
			final int j = i;
			createTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					// Change every n calls the fingerprint
					if (j % (runtimeCycles / numFingerprintChanges) == 0) {
						synchronized (fingerprint) {
							Fingerprint oldFingerprint = fingerprint
									.getAndSet(DoclogManagerTest
											.getTestFingerprint());
							associate(oldFingerprint, fingerprint.get());
						}
					}
					return null;
				}
			});
			createTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					assertEquals(DoclogManagerTest.DOCLOG_RECORD,
							DoclogManagerTest.createDoclogRecord(
									fingerprint.get(),
									DoclogManagerTest.DOCLOG_RECORD));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});

			readTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					assertEquals(
							DoclogManagerTest.DOCLOG_RECORD,
							DoclogManagerTest.readDoclogRecord(
									fingerprint.get(), j));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});

			deleteTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Assert.assertTrue(DoclogManagerTest.deleteDoclogRecord(
							fingerprint.get(), 0));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
		}

		System.out.println("Fingerprint change load test:");
		System.out.print("Create:");
		executorService.invokeAll(createTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles,
				DoclogManagerTest.readDoclog(fingerprint.get()).size());
		System.out.print("Read  :");
		executorService.invokeAll(readTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles,
				DoclogManagerTest.readDoclog(fingerprint.get()).size());
		System.out.print("Delete:");
		executorService.invokeAll(deleteTasks);
		System.out.println(" .");
		Assert.assertNull(DoclogManagerTest.readDoclogRecord(fingerprint.get(),
				0));
		Assert.assertNull(DoclogManagerTest.readDoclog(fingerprint.get()));
	}
}
