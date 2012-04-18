package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Rectangle;

// TODO: start own web server and operate on temp directory
public class DoclogManagerTest {

	public static final DoclogRecord DOCLOG_RECORD = new DoclogRecord(
			"http://bkahlert.com#interestingStuff",
			"192.168.0.5",
			"2001:0db8:85a3:08d3:1319:8a2e:0370:7344",
			DoclogAction.LINK,
			"https://bkahlert.com/some/other/resource",
			DoclogRecord.ISO8601.parseDateTime("1984-04-15T14:30:02.012+02:00"),
			new Rectangle(12, 34, 100300500, 250450650));

	public static final DoclogRecord DOCLOG_RECORD2 = new DoclogRecord(
			"https://bkahlert.com/some/other/resource", "192.168.0.5",
			"2001:0db8:85a3:08d3:1319:8a2e:0370:7344", DoclogAction.READY,
			null,
			DoclogRecord.ISO8601.parseDateTime("1984-04-15T14:30:02.512+0200"),
			new Rectangle(0, 0, 1024, 768));

	public static DoclogRecord DOCLOG_RECORD3 = new DoclogRecord("my url",
			"my ip", "my proxy ip", DoclogAction.SCROLL, null,
			new DateTime().withZone(DateTimeZone.forOffsetHours(-4)),
			new Rectangle(555, 666, 777, 888));

	public static DoclogRecord DOCLOG_RECORD4 = new DoclogRecord("my url 2",
			"my ip 2", "my proxy ip 2", DoclogAction.SURVEY, null,
			new DateTime().withZone(DateTimeZone.forOffsetHoursMinutes(5, 30))
					.withFieldAdded(DurationFieldType.seconds(), 5),
			new Rectangle(550, 660, 770, 880));

	static ID getTestID() {
		return new ID("TEST" + Utils.getRandomString(32));
	}

	static Fingerprint getTestFingerprint() {
		return new Fingerprint("!TEST" + Utils.getRandomString(32));
	}

	private static WebResource getDoclogService() {
		URI uri = UriBuilder.fromUri(
				Utils.getBaseURI().toString() + "/rest/doclog").build();
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		return client.resource(uri);
	}

	static Doclog readDoclog(Object key) {
		WebResource doclogService = getDoclogService();
		try {
			return doclogService.path(key.toString()).path("test")
					.accept(MediaType.APPLICATION_XML).get(Doclog.class);
		} catch (UniformInterfaceException e) {

		}
		return null;
	}

	static DoclogRecord readDoclogRecord(Object key, int index) {
		WebResource doclogService = getDoclogService();

		try {
			return doclogService.path(key.toString())
					.path(new Integer(index).toString())
					.accept(MediaType.APPLICATION_XML).get(DoclogRecord.class);
		} catch (UniformInterfaceException e) {

		}
		return null;
	}

	static DoclogRecord createDoclogRecord(Object key,
			final DoclogRecord doclogRecord) {
		return createDoclogRecord(key, null, doclogRecord);
	}

	@SuppressWarnings("serial")
	static DoclogRecord createDoclogRecord(Object key, final ID id,
			final DoclogRecord doclogRecord) {
		WebResource doclogService = getDoclogService();

		try {
			return doclogService.path(key.toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							if (id != null)
								add("id", id.toString());
							add("url", doclogRecord.getUrl());
							add("ip", doclogRecord.getIp());
							add("proxyIp", doclogRecord.getProxyIp());
							add("action", doclogRecord.getAction());
							add("actionParameter",
									doclogRecord.getActionParameter());
							add("dateTime", doclogRecord.getDateTime());
							add("bounds.x", doclogRecord.getBounds().getX());
							add("bounds.y", doclogRecord.getBounds().getY());
							add("bounds.width", doclogRecord.getBounds()
									.getWidth());
							add("bounds.height", doclogRecord.getBounds()
									.getHeight());
						}
					});
		} catch (UniformInterfaceException e) {

		}
		return null;
	}

	static boolean deleteDoclogRecord(Object key, int index) {
		WebResource doclogService = getDoclogService();

		return Boolean.valueOf(doclogService.path(key.toString())
				.path(new Integer(index).toString())
				.accept(MediaType.APPLICATION_XML).delete(String.class));
	}

	@Test
	public void testReadDoclogs() {
		WebResource doclogService = getDoclogService();

		Doclog[] doclogs = doclogService.accept(MediaType.APPLICATION_XML).get(
				Doclog[].class);
		for (Doclog doclog : doclogs) {
			boolean hasId = doclog.getId() != null;
			boolean hasFingerprint = doclog.getFingerprint() != null;
			Assert.assertTrue(
					doclog + " must contain a " + ID.class.getSimpleName()
							+ " xor " + Fingerprint.class.getSimpleName(),
					hasId ^ hasFingerprint);
		}
		Assert.assertTrue(true);
	}

	@Test
	public void testReadDoclog() {
		WebResource doclogService = getDoclogService();

		Doclog[] doclogs = doclogService.accept(MediaType.APPLICATION_XML).get(
				Doclog[].class);
		Assert.assertTrue(true);
		for (Doclog doclog : doclogs) {
			Object key = doclog.getKey();
			Assert.assertNotNull(key);
		}

		// query onyl a random doclog for performance reasons
		if (doclogs.length > 0) {
			int i = (int) (Math.random() * doclogs.length);
			readDoclog(doclogs[i].getKey());
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testCreateReadDeleteIDbasedDoclogRecords() {
		ID testID = getTestID();

		/*
		 * Create
		 */
		DoclogRecord createdDoclogRecord = createDoclogRecord(testID,
				DOCLOG_RECORD);
		DoclogRecord createdDoclogRecord2 = createDoclogRecord(testID,
				DOCLOG_RECORD2);
		Doclog implicitlyCreatedDoclog = readDoclog(testID);
		Assert.assertNotNull(implicitlyCreatedDoclog);
		boolean hasId = implicitlyCreatedDoclog.getId() != null;
		boolean hasFingerprint = implicitlyCreatedDoclog.getFingerprint() != null;
		Assert.assertTrue(hasId && !hasFingerprint);
		Assert.assertEquals(DOCLOG_RECORD, createdDoclogRecord);
		Assert.assertEquals(DOCLOG_RECORD2, createdDoclogRecord2);

		/*
		 * Read
		 */
		DoclogRecord readDoclogRecord = readDoclogRecord(testID.toString(), -2);
		DoclogRecord readDoclogRecord2 = readDoclogRecord(testID.toString(), -1);
		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord);
		Assert.assertEquals(DOCLOG_RECORD2, readDoclogRecord2);

		/*
		 * Delete
		 */
		boolean deleted = deleteDoclogRecord(testID, -1);
		boolean deleted2 = deleteDoclogRecord(testID, -1);
		Assert.assertTrue(deleted);
		Assert.assertTrue(deleted2);

		DoclogRecord deletedDoclogRecord = readDoclogRecord(testID, -1);
		Doclog implicitlyDeletedDoclog = readDoclog(testID);
		Assert.assertNull(deletedDoclogRecord);
		Assert.assertNull(implicitlyDeletedDoclog);
	}

	@Test
	public void testCreateReadDeleteFingerprintBasedDoclogRecords() {
		Fingerprint testFingerprint = getTestFingerprint();

		/*
		 * Create
		 */
		DoclogRecord createdDoclogRecord = createDoclogRecord(testFingerprint,
				DOCLOG_RECORD);
		DoclogRecord createdDoclogRecord2 = createDoclogRecord(testFingerprint,
				DOCLOG_RECORD2);
		Doclog implicitlyCreatedDoclog = readDoclog(testFingerprint);
		Assert.assertNotNull(implicitlyCreatedDoclog);
		boolean hasId = implicitlyCreatedDoclog.getId() != null;
		boolean hasFingerprint = implicitlyCreatedDoclog.getFingerprint() != null;
		Assert.assertTrue(!hasId && hasFingerprint);
		Assert.assertEquals(DOCLOG_RECORD, createdDoclogRecord);
		Assert.assertEquals(DOCLOG_RECORD2, createdDoclogRecord2);

		/*
		 * Read
		 */
		DoclogRecord readDoclogRecord = readDoclogRecord(
				testFingerprint.toString(), -2);
		DoclogRecord readDoclogRecord2 = readDoclogRecord(
				testFingerprint.toString(), -1);
		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord);
		Assert.assertEquals(DOCLOG_RECORD2, readDoclogRecord2);

		/*
		 * Delete
		 */
		boolean deleted = deleteDoclogRecord(testFingerprint, -1);
		boolean deleted2 = deleteDoclogRecord(testFingerprint, -1);
		Assert.assertTrue(deleted);
		Assert.assertTrue(deleted2);

		DoclogRecord deletedDoclogRecord = readDoclogRecord(testFingerprint, -1);
		Doclog implicitlyDeletedDoclog = readDoclog(testFingerprint);
		Assert.assertNull(deletedDoclogRecord);
		Assert.assertNull(implicitlyDeletedDoclog);
	}

	@Test
	public void testCreateReadDeleteIDthenFingerprintBasedDoclogRecords() {
		ID testID = getTestID();
		Fingerprint testFingerprint = getTestFingerprint();

		/*
		 * Create with Fingerprint
		 */
		DoclogRecord createdDoclogRecord = createDoclogRecord(testFingerprint,
				DOCLOG_RECORD);
		Doclog implicitlyCreatedDoclog = readDoclog(testFingerprint);
		boolean hasId = implicitlyCreatedDoclog.getId() != null;
		boolean hasFingerprint = implicitlyCreatedDoclog.getFingerprint() != null;
		Assert.assertTrue(!hasId && hasFingerprint);
		Assert.assertEquals(DOCLOG_RECORD, createdDoclogRecord);

		/*
		 * Create with ID
		 */
		DoclogRecord createdDoclogRecord2 = createDoclogRecord(testFingerprint,
				testID, DOCLOG_RECORD2);
		// doclog accessible by ID?
		Doclog rewrittenDoclog = readDoclog(testID);
		Assert.assertNotNull(rewrittenDoclog);
		hasId = rewrittenDoclog.getId() != null;
		hasFingerprint = rewrittenDoclog.getFingerprint() != null;
		Assert.assertTrue(hasId && !hasFingerprint);
		// doclog accessible by Fingerprint?
		Doclog rewrittenDoclog2 = readDoclog(testFingerprint);
		Assert.assertNotNull(rewrittenDoclog2);
		hasId = rewrittenDoclog2.getId() != null;
		hasFingerprint = rewrittenDoclog2.getFingerprint() != null;
		Assert.assertTrue(hasId && !hasFingerprint);
		// doclogs are the same?
		Assert.assertEquals(rewrittenDoclog, rewrittenDoclog2);
		Assert.assertEquals(DOCLOG_RECORD2, createdDoclogRecord2);

		/*
		 * Read by ID and Fingerprint
		 */
		Assert.assertEquals(DOCLOG_RECORD,
				readDoclogRecord(testID.toString(), -2));
		Assert.assertEquals(DOCLOG_RECORD,
				readDoclogRecord(testFingerprint.toString(), -2));
		Assert.assertEquals(DOCLOG_RECORD2,
				readDoclogRecord(testID.toString(), -1));
		Assert.assertEquals(DOCLOG_RECORD2,
				readDoclogRecord(testFingerprint.toString(), -1));

		/*
		 * Delete by ID and Fingerprint
		 */
		boolean deleted = deleteDoclogRecord(testID, -1);
		boolean deleted2 = deleteDoclogRecord(testFingerprint, -1);
		Assert.assertTrue(deleted);
		Assert.assertTrue(deleted2);

		DoclogRecord deletedDoclogRecord = readDoclogRecord(testID, -1);
		DoclogRecord deletedDoclogRecord2 = readDoclogRecord(testFingerprint,
				-1);
		Doclog implicitlyDeletedDoclog = readDoclog(testID);
		Doclog implicitlyDeletedDoclog2 = readDoclog(testFingerprint);
		Assert.assertNull(deletedDoclogRecord);
		Assert.assertNull(deletedDoclogRecord2);
		Assert.assertNull(implicitlyDeletedDoclog);
		Assert.assertNull(implicitlyDeletedDoclog2);
	}

	@Test
	public void testCreateReadDeleteFingerprintThenIDBasedDoclogRecords() {
		ID testID = getTestID();
		Fingerprint testFingerprint = getTestFingerprint();

		/*
		 * Create with ID
		 */
		DoclogRecord createdDoclogRecord = createDoclogRecord(testID,
				DOCLOG_RECORD);
		Doclog implicitlyCreatedDoclog = readDoclog(testID);
		boolean hasId = implicitlyCreatedDoclog.getId() != null;
		boolean hasFingerprint = implicitlyCreatedDoclog.getFingerprint() != null;
		Assert.assertTrue(hasId && !hasFingerprint);
		Assert.assertEquals(DOCLOG_RECORD, createdDoclogRecord);

		/*
		 * Create with Fingerprint
		 */
		DoclogRecord createdDoclogRecord2 = createDoclogRecord(testFingerprint,
				testID, DOCLOG_RECORD2);
		// doclog accessible by ID?
		Doclog rewrittenDoclog = readDoclog(testID);
		Assert.assertNotNull(rewrittenDoclog);
		hasId = rewrittenDoclog.getId() != null;
		hasFingerprint = rewrittenDoclog.getFingerprint() != null;
		Assert.assertTrue(hasId && !hasFingerprint);
		// doclog accessible by Fingerprint?
		Doclog rewrittenDoclog2 = readDoclog(testFingerprint);
		Assert.assertNotNull(rewrittenDoclog2);
		hasId = rewrittenDoclog2.getId() != null;
		hasFingerprint = rewrittenDoclog2.getFingerprint() != null;
		Assert.assertTrue(hasId && !hasFingerprint);
		// doclogs are the same?
		Assert.assertEquals(rewrittenDoclog, rewrittenDoclog2);
		Assert.assertEquals(DOCLOG_RECORD2, createdDoclogRecord2);

		/*
		 * Read by ID and Fingerprint
		 */
		Assert.assertEquals(DOCLOG_RECORD,
				readDoclogRecord(testID.toString(), -2));
		Assert.assertEquals(DOCLOG_RECORD,
				readDoclogRecord(testFingerprint.toString(), -2));
		Assert.assertEquals(DOCLOG_RECORD2,
				readDoclogRecord(testID.toString(), -1));
		Assert.assertEquals(DOCLOG_RECORD2,
				readDoclogRecord(testFingerprint.toString(), -1));

		/*
		 * Delete by ID and Fingerprint
		 */
		boolean deleted = deleteDoclogRecord(testID, -1);
		boolean deleted2 = deleteDoclogRecord(testFingerprint, -1);
		Assert.assertTrue(deleted);
		Assert.assertTrue(deleted2);

		DoclogRecord deletedDoclogRecord = readDoclogRecord(testID, -1);
		DoclogRecord deletedDoclogRecord2 = readDoclogRecord(testFingerprint,
				-1);
		Doclog implicitlyDeletedDoclog = readDoclog(testID);
		Doclog implicitlyDeletedDoclog2 = readDoclog(testFingerprint);
		Assert.assertNull(deletedDoclogRecord);
		Assert.assertNull(deletedDoclogRecord2);
		Assert.assertNull(implicitlyDeletedDoclog);
		Assert.assertNull(implicitlyDeletedDoclog2);
	}

	@Test
	public void testMultipleFingerprintsThenID() {
		ID id = getTestID();
		Fingerprint f1 = getTestFingerprint();
		Fingerprint f2 = getTestFingerprint();

		/*
		 * Create with different Fingerprints
		 */
		Assert.assertEquals(DOCLOG_RECORD,
				createDoclogRecord(f1, DOCLOG_RECORD));
		Assert.assertEquals(DOCLOG_RECORD2,
				createDoclogRecord(f2, DOCLOG_RECORD2));

		/*
		 * Read
		 */
		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(f1, 0));
		Assert.assertEquals(DOCLOG_RECORD2, readDoclogRecord(f2, 0));

		/*
		 * Merge #1
		 */
		Assert.assertEquals(DOCLOG_RECORD3,
				createDoclogRecord(f1, id, DOCLOG_RECORD3));

		Assert.assertEquals(DOCLOG_RECORD3, readDoclogRecord(id, 1));
		Assert.assertEquals(DOCLOG_RECORD3, readDoclogRecord(f1, 1));

		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(id, 0));
		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(f1, 0));

		Assert.assertEquals(DOCLOG_RECORD2, readDoclogRecord(f2, 0));

		/*
		 * Merge #2
		 */
		Assert.assertEquals(DOCLOG_RECORD4,
				createDoclogRecord(f2, id, DOCLOG_RECORD4));

		Assert.assertEquals(DOCLOG_RECORD4, readDoclogRecord(id, 3));
		Assert.assertEquals(DOCLOG_RECORD4, readDoclogRecord(f1, 3));
		Assert.assertEquals(DOCLOG_RECORD4, readDoclogRecord(f2, 3));

		Assert.assertEquals(DOCLOG_RECORD2, readDoclogRecord(id, 2));
		Assert.assertEquals(DOCLOG_RECORD2, readDoclogRecord(f1, 2));
		Assert.assertEquals(DOCLOG_RECORD2, readDoclogRecord(f2, 2));

		Assert.assertEquals(DOCLOG_RECORD3, readDoclogRecord(id, 1));
		Assert.assertEquals(DOCLOG_RECORD3, readDoclogRecord(f1, 1));
		Assert.assertEquals(DOCLOG_RECORD3, readDoclogRecord(f2, 1));

		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(id, 0));
		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(f1, 0));
		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(f2, 0));

		/*
		 * Delete
		 */
		Assert.assertTrue(deleteDoclogRecord(id, -4));
		Assert.assertTrue(deleteDoclogRecord(id, -3));
		Assert.assertTrue(deleteDoclogRecord(id, -2));
		Assert.assertTrue(deleteDoclogRecord(id, -1));

		Assert.assertNull(readDoclogRecord(id, 0));
		Assert.assertNull(readDoclogRecord(f1, 0));
		Assert.assertNull(readDoclogRecord(f2, 0));
		Assert.assertNull(readDoclog(id));
		Assert.assertNull(readDoclog(f1));
		Assert.assertNull(readDoclog(f2));
	}

	@Test
	public void testIgnoreAllButFirstID() {
		ID id1 = getTestID();
		ID id2 = getTestID();

		Assert.assertEquals(DOCLOG_RECORD,
				createDoclogRecord(id1, id2, DOCLOG_RECORD));

		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(id1, 0));
		Assert.assertNull(readDoclogRecord(id2, 0));

		Assert.assertTrue(deleteDoclogRecord(id1, 0));
		Assert.assertFalse(deleteDoclogRecord(id2, 0));

		Assert.assertNull(readDoclogRecord(id1, 0));
		Assert.assertNull(readDoclogRecord(id2, 0));
		Assert.assertNull(readDoclog(id1));
		Assert.assertNull(readDoclog(id2));
	}

	@Test
	public void testIDandFingerprintInOneRequest() {
		ID id = getTestID();
		Fingerprint fingerprint = getTestFingerprint();

		Assert.assertEquals(DOCLOG_RECORD,
				createDoclogRecord(fingerprint, id, DOCLOG_RECORD));

		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(id, 0));
		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(fingerprint, 0));

		Assert.assertTrue(deleteDoclogRecord(id, 0));

		Assert.assertNull(readDoclogRecord(id, 0));
		Assert.assertNull(readDoclogRecord(fingerprint, 0));
		Assert.assertNull(readDoclog(id));
		Assert.assertNull(readDoclog(fingerprint));
	}

	@Test
	public void testChangeOfIDdisallowed() {
		ID id1 = getTestID();
		ID id2 = getTestID();
		Fingerprint fingerprint = getTestFingerprint();

		Assert.assertEquals(DOCLOG_RECORD,
				createDoclogRecord(fingerprint, id1, DOCLOG_RECORD));
		Assert.assertNull(createDoclogRecord(fingerprint, id2, DOCLOG_RECORD2));

		Assert.assertEquals(DOCLOG_RECORD, readDoclogRecord(id1, 0));
		Assert.assertNull(readDoclogRecord(id2, 0));

		Assert.assertTrue(deleteDoclogRecord(id1, 0));
		Assert.assertFalse(deleteDoclogRecord(id2, 0));

		Assert.assertNull(readDoclogRecord(id1, 0));
		Assert.assertNull(readDoclogRecord(id2, 0));
		Assert.assertNull(readDoclog(id1));
		Assert.assertNull(readDoclog(id2));
	}

	@SuppressWarnings("serial")
	@Test
	public void testMissingOptionalParameter() {
		ID id = getTestID();
		WebResource doclogService = getDoclogService();

		try {
			DoclogRecord doclogRecord = doclogService.path(id.toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							// add("ip", DOCLOG_RECORD.getIp());
							// add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("event", "ready");
							// add("action", DOCLOG_RECORD.getAction());
							// add("actionParameter",
							// DOCLOG_RECORD.getActionParameter());
							add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertEquals(DOCLOG_RECORD.getUrl(), doclogRecord.getUrl());
			Assert.assertEquals(DoclogAction.READY, doclogRecord.getAction());
			Assert.assertNull(doclogRecord.getActionParameter());
			Assert.assertEquals(DOCLOG_RECORD.getDateTime(),
					doclogRecord.getDateTime());
			Assert.assertEquals(DOCLOG_RECORD.getBounds(),
					doclogRecord.getBounds());

			deleteDoclogRecord(id, 0);
			Assert.assertNull(readDoclogRecord(id, 0));
			Assert.assertNull(readDoclog(id));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}

		try {
			DoclogRecord doclogRecord = doclogService.path(id.toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							// add("ip", DOCLOG_RECORD.getIp());
							// add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("event", "link-http-etc");
							// add("action", DOCLOG_RECORD.getAction());
							// add("actionParameter",
							// DOCLOG_RECORD.getActionParameter());
							add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertEquals(DOCLOG_RECORD.getUrl(), doclogRecord.getUrl());
			Assert.assertEquals(DoclogAction.LINK, doclogRecord.getAction());
			Assert.assertEquals("http-etc", doclogRecord.getActionParameter());
			Assert.assertEquals(DOCLOG_RECORD.getDateTime(),
					doclogRecord.getDateTime());
			Assert.assertEquals(DOCLOG_RECORD.getBounds(),
					doclogRecord.getBounds());

			deleteDoclogRecord(id, 0);
			Assert.assertNull(readDoclogRecord(id, 0));
			Assert.assertNull(readDoclog(id));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void testMissingRequiredParameter() {
		WebResource doclogService = getDoclogService();

		try {
			doclogService.path(getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							// add("url", DOCLOG_RECORD.getUrl());
							add("ip", DOCLOG_RECORD.getIp());
							add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("action", DOCLOG_RECORD.getAction());
							add("actionParameter",
									DOCLOG_RECORD.getActionParameter());
							add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertTrue(false);
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(true);
		}

		try {
			doclogService.path(getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							add("ip", DOCLOG_RECORD.getIp());
							add("proxyIp", DOCLOG_RECORD.getProxyIp());
							// add("action", DOCLOG_RECORD.getAction());
							add("actionParameter",
									DOCLOG_RECORD.getActionParameter());
							add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertTrue(false);
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(true);
		}

		try {
			doclogService.path(getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							add("ip", DOCLOG_RECORD.getIp());
							add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("action", DOCLOG_RECORD.getAction());
							add("actionParameter",
									DOCLOG_RECORD.getActionParameter());
							// add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertTrue(false);
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(true);
		}

		try {
			doclogService.path(getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							add("ip", DOCLOG_RECORD.getIp());
							add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("action", DOCLOG_RECORD.getAction());
							add("actionParameter",
									DOCLOG_RECORD.getActionParameter());
							add("dateTime", DOCLOG_RECORD.getDateTime());
							// add("bounds.x",
							// DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertTrue(false);
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(true);
		}

		try {
			doclogService.path(getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							add("ip", DOCLOG_RECORD.getIp());
							add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("action", DOCLOG_RECORD.getAction());
							add("actionParameter",
									DOCLOG_RECORD.getActionParameter());
							add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							// add("bounds.y",
							// DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertTrue(false);
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(true);
		}

		try {
			doclogService.path(getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							add("ip", DOCLOG_RECORD.getIp());
							add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("action", DOCLOG_RECORD.getAction());
							add("actionParameter",
									DOCLOG_RECORD.getActionParameter());
							add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							// add("bounds.width", DOCLOG_RECORD.getBounds()
							// .getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertTrue(false);
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(true);
		}

		try {
			doclogService.path(getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							add("ip", DOCLOG_RECORD.getIp());
							add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("action", DOCLOG_RECORD.getAction());
							add("actionParameter",
									DOCLOG_RECORD.getActionParameter());
							add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							// add("bounds.height", DOCLOG_RECORD.getBounds()
							// .getHeight());
						}
					});
			Assert.assertTrue(false);
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(true);
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void testUnknownDoclogAction() {
		ID id = getTestID();
		WebResource doclogService = getDoclogService();

		try {
			DoclogRecord doclogRecord = doclogService.path(id.toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							add("event", "I_am_unknown");
							add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertEquals(DOCLOG_RECORD.getUrl(), doclogRecord.getUrl());
			Assert.assertEquals(DoclogAction.UNKNOWN, doclogRecord.getAction());
			Assert.assertNull(doclogRecord.getActionParameter());
			Assert.assertEquals(DOCLOG_RECORD.getDateTime(),
					doclogRecord.getDateTime());
			Assert.assertEquals(DOCLOG_RECORD.getBounds(),
					doclogRecord.getBounds());

			deleteDoclogRecord(id, 0);
			Assert.assertNull(readDoclogRecord(id, 0));
			Assert.assertNull(readDoclog(id));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}

		try {
			DoclogRecord doclogRecord = doclogService.path(id.toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_XML)
					.post(DoclogRecord.class, new MultivaluedMapImpl() {
						{
							add("url", DOCLOG_RECORD.getUrl());
							add("event", "I_am_unknown-I_am_a_parameter");
							add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", DOCLOG_RECORD.getBounds()
									.getHeight());
						}
					});
			Assert.assertEquals(DOCLOG_RECORD.getUrl(), doclogRecord.getUrl());
			Assert.assertEquals(DoclogAction.UNKNOWN, doclogRecord.getAction());
			Assert.assertEquals("I_am_a_parameter",
					doclogRecord.getActionParameter());
			Assert.assertEquals(DOCLOG_RECORD.getDateTime(),
					doclogRecord.getDateTime());
			Assert.assertEquals(DOCLOG_RECORD.getBounds(),
					doclogRecord.getBounds());

			deleteDoclogRecord(id, 0);
			Assert.assertNull(readDoclogRecord(id, 0));
			Assert.assertNull(readDoclog(id));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}
	}

	@Test
	public void createDoclogWithLongID() {
		DoclogRecord createdDoclogRecord = createDoclogRecord(
				Utils.getRandomString(500), DOCLOG_RECORD);
		Assert.assertNull(createdDoclogRecord);
	}

	@Test
	public void createDoclogWithLongFingerprint() {
		DoclogRecord createdDoclogRecord = createDoclogRecord(
				"!" + Utils.getRandomString(500), DOCLOG_RECORD);
		Assert.assertNull(createdDoclogRecord);
	}

	@Test
	public void fingerprintOnlyLoadTest() throws InterruptedException {
		int cores = Runtime.getRuntime().availableProcessors();
		final int runtimeCycles = cores * 20;

		final Fingerprint fingerprint = DoclogManagerTest.getTestFingerprint();

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
					assertEquals(DoclogManagerTest.DOCLOG_RECORD,
							DoclogManagerTest.createDoclogRecord(fingerprint,
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
					assertEquals(DoclogManagerTest.DOCLOG_RECORD,
							DoclogManagerTest.readDoclogRecord(fingerprint, j));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
			deleteTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Assert.assertTrue(deleteDoclogRecord(fingerprint, 0));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
		}

		System.out.println("Fingerprint only load test:");
		System.out.print("Create:");
		executorService.invokeAll(createTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles, readDoclog(fingerprint).size());
		System.out.print("Read  :");
		executorService.invokeAll(readTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles, readDoclog(fingerprint).size());
		System.out.print("Delete:");
		executorService.invokeAll(deleteTasks);
		System.out.println(" .");
		Assert.assertNull(readDoclogRecord(fingerprint, 0));
		Assert.assertNull(readDoclog(fingerprint));
	}

	@Test
	public void idOnlyLoadTest() throws InterruptedException {
		int cores = Runtime.getRuntime().availableProcessors();
		final int runtimeCycles = cores * 20;

		final ID id = DoclogManagerTest.getTestID();

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
					assertEquals(DoclogManagerTest.DOCLOG_RECORD,
							DoclogManagerTest.createDoclogRecord(id,
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
					assertEquals(DoclogManagerTest.DOCLOG_RECORD,
							DoclogManagerTest.readDoclogRecord(id, j));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
			deleteTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Assert.assertTrue(deleteDoclogRecord(id, 0));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
		}

		System.out.println("ID only load test:");
		System.out.print("Create:");
		executorService.invokeAll(createTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles, readDoclog(id).size());
		System.out.print("Read  :");
		executorService.invokeAll(readTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles, readDoclog(id).size());
		System.out.print("Delete:");
		executorService.invokeAll(deleteTasks);
		System.out.println(" .");
		Assert.assertNull(readDoclogRecord(id, 0));
		Assert.assertNull(readDoclog(id));
	}

	@Test
	public void fingerprintAndIDLoadTest() throws InterruptedException {
		int cores = Runtime.getRuntime().availableProcessors();
		final int runtimeCycles = cores * 20;

		final Fingerprint fingerprint = DoclogManagerTest.getTestFingerprint();
		final ID id = DoclogManagerTest.getTestID();

		ExecutorService executorService = Executors
				.newFixedThreadPool(cores * 5);
		Set<Callable<Void>> createTasks = new HashSet<Callable<Void>>();
		Set<Callable<Void>> readTasks = new HashSet<Callable<Void>>();
		Set<Callable<Void>> deleteTasks = new HashSet<Callable<Void>>();

		for (int i = 0; i < runtimeCycles; i++) {
			final int j = i;
			if (j % 2 == 0) {
				createTasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						assertEquals(DoclogManagerTest.DOCLOG_RECORD,
								DoclogManagerTest.createDoclogRecord(
										fingerprint,
										DoclogManagerTest.DOCLOG_RECORD));
						if (j % (runtimeCycles / 10) == 0) {
							System.out.print(" .");
						}
						return null;
					}
				});
			} else {
				createTasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						if (j > runtimeCycles / 2) {
							assertEquals(DoclogManagerTest.DOCLOG_RECORD,
									DoclogManagerTest.createDoclogRecord(id,
											DoclogManagerTest.DOCLOG_RECORD));
						} else {
							assertEquals(DoclogManagerTest.DOCLOG_RECORD,
									DoclogManagerTest.createDoclogRecord(
											fingerprint, id,
											DoclogManagerTest.DOCLOG_RECORD));
						}
						if (j % (runtimeCycles / 10) == 0) {
							System.out.print(" .");
						}
						return null;
					}
				});
			}

			readTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					assertEquals(DoclogManagerTest.DOCLOG_RECORD,
							DoclogManagerTest.readDoclogRecord(fingerprint, j));
					if (j % (runtimeCycles / 5) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
			readTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					assertEquals(DoclogManagerTest.DOCLOG_RECORD,
							DoclogManagerTest.readDoclogRecord(id, j));
					if (j % (runtimeCycles / 5) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});

			deleteTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Assert.assertTrue(deleteDoclogRecord(
							j % 2 == 0 ? fingerprint : id, 0));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
		}

		System.out.println("Fingerprint and ID load test:");
		System.out.print("Create:");
		executorService.invokeAll(createTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles, readDoclog(id).size());
		System.out.print("Read  :");
		executorService.invokeAll(readTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles, readDoclog(id).size());
		System.out.print("Delete:");
		executorService.invokeAll(deleteTasks);
		System.out.println(" .");
		Assert.assertNull(readDoclogRecord(id, 0));
		Assert.assertNull(readDoclog(id));
	}
}
