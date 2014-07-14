package de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAsrv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import de.fu_berlin.imp.apiua.server.java_ee.model.Doclog;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogAction;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogRecord;
import de.fu_berlin.imp.apiua.server.java_ee.model.Fingerprint;
import de.fu_berlin.imp.apiua.server.java_ee.model.ID;
import de.fu_berlin.imp.apiua.server.java_ee.model.IIdentifier;
import de.fu_berlin.imp.apiua.server.java_ee.utils.DoclogRESTUtils;
import de.fu_berlin.imp.apiua.server.java_ee.utils.TestConfiguration;
import de.fu_berlin.imp.apiua.server.java_ee.utils.Utils;

// TODO: start own web server and operate on temp directory
@RunWith(value = Parameterized.class)
public class DoclogRESTTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() throws Exception {
		List<Object[]> parameters = new ArrayList<Object[]>();
		for (URL APIUAsrv : TestConfiguration.getAPIUAsrvURLs()) {
			parameters.add(new Object[] { APIUAsrv });
		}
		return parameters;
	}

	public static final int RUNS_PER_CORE = 5;

	private URL APIUAsrv;

	public DoclogRESTTest(URL APIUAsrv) {
		this.APIUAsrv = APIUAsrv;
	}

	@Test
	public void testCreateReadDeleteIDbasedDoclogRecords() throws Exception {
		ID testID = Utils.getTestID();

		/*
		 * Create
		 */
		DoclogRecord createdDoclogRecord = DoclogRESTUtils.createDoclogRecord(
				testID, Utils.DOCLOG_RECORD);
		DoclogRecord createdDoclogRecord2 = DoclogRESTUtils.createDoclogRecord(
				testID, Utils.DOCLOG_RECORD2);
		Doclog implicitlyCreatedDoclog = DoclogRESTUtils.readDoclog(testID);
		Assert.assertNotNull(implicitlyCreatedDoclog);
		boolean hasId = implicitlyCreatedDoclog.getIdentifier() instanceof IIdentifier;
		Assert.assertTrue(hasId);
		Assert.assertEquals(Utils.DOCLOG_RECORD, createdDoclogRecord);
		Assert.assertEquals(Utils.DOCLOG_RECORD2, createdDoclogRecord2);

		/*
		 * Read
		 */
		DoclogRecord readDoclogRecord = DoclogRESTUtils.readDoclogRecord(
				testID, -2);
		DoclogRecord readDoclogRecord2 = DoclogRESTUtils.readDoclogRecord(
				testID, -1);
		Assert.assertEquals(Utils.DOCLOG_RECORD, readDoclogRecord);
		Assert.assertEquals(Utils.DOCLOG_RECORD2, readDoclogRecord2);

		/*
		 * Delete
		 */
		boolean deleted = DoclogRESTUtils.deleteDoclogRecord(testID, -1);
		boolean deleted2 = DoclogRESTUtils.deleteDoclogRecord(testID, -1);
		Assert.assertTrue(deleted);
		Assert.assertTrue(deleted2);

		DoclogRecord deletedDoclogRecord = DoclogRESTUtils.readDoclogRecord(
				testID, -1);
		Doclog implicitlyDeletedDoclog = DoclogRESTUtils.readDoclog(testID);
		Assert.assertNull(deletedDoclogRecord);
		Assert.assertEquals(0, implicitlyDeletedDoclog.size());
	}

	@Test
	public void testCreateReadDeleteFingerprintBasedDoclogRecords()
			throws Exception {
		Fingerprint testFingerprint = Utils.getTestFingerprint();

		/*
		 * Create
		 */
		DoclogRecord createdDoclogRecord = DoclogRESTUtils.createDoclogRecord(
				testFingerprint, Utils.DOCLOG_RECORD);
		DoclogRecord createdDoclogRecord2 = DoclogRESTUtils.createDoclogRecord(
				testFingerprint, Utils.DOCLOG_RECORD2);
		Doclog implicitlyCreatedDoclog = DoclogRESTUtils
				.readDoclog(testFingerprint);
		Assert.assertNotNull(implicitlyCreatedDoclog);
		boolean hasFingerprint = implicitlyCreatedDoclog.getIdentifier() instanceof Fingerprint;
		Assert.assertTrue(hasFingerprint);
		Assert.assertEquals(Utils.DOCLOG_RECORD, createdDoclogRecord);
		Assert.assertEquals(Utils.DOCLOG_RECORD2, createdDoclogRecord2);

		/*
		 * Read
		 */
		DoclogRecord readDoclogRecord = DoclogRESTUtils.readDoclogRecord(
				testFingerprint, -2);
		DoclogRecord readDoclogRecord2 = DoclogRESTUtils.readDoclogRecord(
				testFingerprint, -1);
		Assert.assertEquals(Utils.DOCLOG_RECORD, readDoclogRecord);
		Assert.assertEquals(Utils.DOCLOG_RECORD2, readDoclogRecord2);

		/*
		 * Delete
		 */
		boolean deleted = DoclogRESTUtils.deleteDoclogRecord(testFingerprint,
				-1);
		boolean deleted2 = DoclogRESTUtils.deleteDoclogRecord(testFingerprint,
				-1);
		Assert.assertTrue(deleted);
		Assert.assertTrue(deleted2);

		DoclogRecord deletedDoclogRecord = DoclogRESTUtils.readDoclogRecord(
				testFingerprint, -1);
		Doclog implicitlyDeletedDoclog = DoclogRESTUtils
				.readDoclog(testFingerprint);
		Assert.assertNull(deletedDoclogRecord);
		Assert.assertEquals(0, implicitlyDeletedDoclog.size());
	}

	@Test
	public void testCreateReadDeleteFingerprintThenIDBasedDoclogRecords()
			throws Exception {
		ID testID = Utils.getTestID();
		Fingerprint testFingerprint = Utils.getTestFingerprint();

		/*
		 * Create with Fingerprint
		 */
		DoclogRecord createdDoclogRecord = DoclogRESTUtils.createDoclogRecord(
				testFingerprint, Utils.DOCLOG_RECORD);
		Doclog implicitlyCreatedDoclog = DoclogRESTUtils
				.readDoclog(testFingerprint);
		Assert.assertTrue(implicitlyCreatedDoclog.getIdentifier() instanceof Fingerprint);
		Assert.assertEquals(Utils.DOCLOG_RECORD, createdDoclogRecord);

		/*
		 * Create with ID
		 */
		DoclogRecord createdDoclogRecord2 = DoclogRESTUtils.createDoclogRecord(
				testFingerprint, testID, Utils.DOCLOG_RECORD2);
		// doclog accessible by ID?
		Doclog rewrittenDoclog = DoclogRESTUtils.readDoclog(testID);
		Assert.assertNotNull(rewrittenDoclog);
		Assert.assertTrue(rewrittenDoclog.getIdentifier() instanceof ID);
		// doclog accessible by Fingerprint?
		Doclog rewrittenDoclog2 = DoclogRESTUtils.readDoclog(testFingerprint);
		Assert.assertNotNull(rewrittenDoclog2);
		Assert.assertTrue(rewrittenDoclog2.getIdentifier() instanceof ID);
		// doclogs are the same?
		Assert.assertEquals(rewrittenDoclog, rewrittenDoclog2);
		Assert.assertEquals(Utils.DOCLOG_RECORD2, createdDoclogRecord2);

		/*
		 * Read by ID and Fingerprint
		 */
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(testID, -2));
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(testFingerprint, -2));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(testID, -1));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(testFingerprint, -1));

		/*
		 * Delete by ID and Fingerprint
		 */
		boolean deleted = DoclogRESTUtils.deleteDoclogRecord(testID, -1);
		boolean deleted2 = DoclogRESTUtils.deleteDoclogRecord(testFingerprint,
				-1);
		Assert.assertTrue(deleted);
		Assert.assertTrue(deleted2);

		DoclogRecord deletedDoclogRecord = DoclogRESTUtils.readDoclogRecord(
				testID, -1);
		DoclogRecord deletedDoclogRecord2 = DoclogRESTUtils.readDoclogRecord(
				testFingerprint, -1);
		Doclog implicitlyDeletedDoclog = DoclogRESTUtils.readDoclog(testID);
		Doclog implicitlyDeletedDoclog2 = DoclogRESTUtils
				.readDoclog(testFingerprint);
		Assert.assertNull(deletedDoclogRecord);
		Assert.assertNull(deletedDoclogRecord2);
		Assert.assertEquals(0, implicitlyDeletedDoclog.size());
		Assert.assertEquals(0, implicitlyDeletedDoclog2.size());
	}

	@Test
	public void testCreateReadDeleteIDthenFingerprintBasedDoclogRecords()
			throws Exception {
		ID testID = Utils.getTestID();
		Fingerprint testFingerprint = Utils.getTestFingerprint();

		/*
		 * Create with ID
		 */
		DoclogRecord createdDoclogRecord = DoclogRESTUtils.createDoclogRecord(
				testID, Utils.DOCLOG_RECORD);
		Doclog implicitlyCreatedDoclog = DoclogRESTUtils.readDoclog(testID);
		Assert.assertTrue(implicitlyCreatedDoclog.getIdentifier() instanceof ID);
		Assert.assertEquals(Utils.DOCLOG_RECORD, createdDoclogRecord);

		/*
		 * Create with Fingerprint
		 */
		DoclogRecord createdDoclogRecord2 = DoclogRESTUtils.createDoclogRecord(
				testFingerprint, testID, Utils.DOCLOG_RECORD2);
		// doclog accessible by ID?
		Doclog rewrittenDoclog = DoclogRESTUtils.readDoclog(testID);
		Assert.assertNotNull(rewrittenDoclog);
		Assert.assertTrue(rewrittenDoclog.getIdentifier() instanceof ID);
		// doclog accessible by Fingerprint?
		Doclog rewrittenDoclog2 = DoclogRESTUtils.readDoclog(testFingerprint);
		Assert.assertNotNull(rewrittenDoclog2);
		Assert.assertTrue(rewrittenDoclog2.getIdentifier() instanceof ID);
		// doclogs are the same?
		Assert.assertEquals(rewrittenDoclog, rewrittenDoclog2);
		Assert.assertEquals(Utils.DOCLOG_RECORD2, createdDoclogRecord2);

		/*
		 * Read by ID and Fingerprint
		 */
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(testID, -2));
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(testFingerprint, -2));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(testID, -1));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(testFingerprint, -1));

		/*
		 * Delete by ID and Fingerprint
		 */
		boolean deleted = DoclogRESTUtils.deleteDoclogRecord(testID, -1);
		boolean deleted2 = DoclogRESTUtils.deleteDoclogRecord(testFingerprint,
				-1);
		Assert.assertTrue(deleted);
		Assert.assertTrue(deleted2);

		DoclogRecord deletedDoclogRecord = DoclogRESTUtils.readDoclogRecord(
				testID, -1);
		DoclogRecord deletedDoclogRecord2 = DoclogRESTUtils.readDoclogRecord(
				testFingerprint, -1);
		Doclog implicitlyDeletedDoclog = DoclogRESTUtils.readDoclog(testID);
		Doclog implicitlyDeletedDoclog2 = DoclogRESTUtils
				.readDoclog(testFingerprint);
		Assert.assertNull(deletedDoclogRecord);
		Assert.assertNull(deletedDoclogRecord2);
		Assert.assertEquals(0, implicitlyDeletedDoclog.size());
		Assert.assertEquals(0, implicitlyDeletedDoclog2.size());
	}

	@Test
	public void testMultipleFingerprintsThenID() throws Exception {
		ID id = Utils.getTestID();
		Fingerprint f1 = Utils.getTestFingerprint();
		Fingerprint f2 = Utils.getTestFingerprint();

		/*
		 * Create with different Fingerprints
		 */
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.createDoclogRecord(f1, Utils.DOCLOG_RECORD));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.createDoclogRecord(f2, Utils.DOCLOG_RECORD2));

		/*
		 * Read
		 */
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f1, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(f2, 0));

		/*
		 * Merge #1
		 */
		Assert.assertEquals(Utils.DOCLOG_RECORD3, DoclogRESTUtils
				.createDoclogRecord(f1, id, Utils.DOCLOG_RECORD3));

		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f1, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD3,
				DoclogRESTUtils.readDoclogRecord(id, 1));
		Assert.assertEquals(Utils.DOCLOG_RECORD3,
				DoclogRESTUtils.readDoclogRecord(f1, 1));

		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(f2, 0));

		/*
		 * Merge #2
		 */
		Assert.assertEquals(Utils.DOCLOG_RECORD4, DoclogRESTUtils
				.createDoclogRecord(f2, id, Utils.DOCLOG_RECORD4));

		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(id, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(f1, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id, 1));
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(f1, 1));

		Assert.assertEquals(Utils.DOCLOG_RECORD3,
				DoclogRESTUtils.readDoclogRecord(id, 2));
		Assert.assertEquals(Utils.DOCLOG_RECORD3,
				DoclogRESTUtils.readDoclogRecord(f1, 2));
		Assert.assertEquals(Utils.DOCLOG_RECORD4,
				DoclogRESTUtils.readDoclogRecord(id, 3));
		Assert.assertEquals(Utils.DOCLOG_RECORD4,
				DoclogRESTUtils.readDoclogRecord(f1, 3));

		/*
		 * Delete
		 */
		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id, -4));
		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id, -3));
		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id, -2));
		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id, -1));

		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(f1, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(f2, 0));

		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(f1).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(f2).size());
	}

	@Test
	public void testIgnoreAllButFirstID() throws Exception {
		ID id1 = Utils.getTestID();
		ID id2 = Utils.getTestID();

		Assert.assertEquals(Utils.DOCLOG_RECORD, DoclogRESTUtils
				.createDoclogRecord(id1, id2, Utils.DOCLOG_RECORD));

		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id1, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id2, 0));

		Assert.assertEquals(1, DoclogRESTUtils.readDoclog(id1).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id2).size());

		Assert.assertEquals(Utils.DOCLOG_RECORD2, DoclogRESTUtils
				.createDoclogRecord(id1, id2, Utils.DOCLOG_RECORD2));

		Assert.assertEquals(2, DoclogRESTUtils.readDoclog(id1).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id2).size());

		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id1, 0));
		Assert.assertFalse(DoclogRESTUtils.deleteDoclogRecord(id2, 0));

		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id1, 0));
		Assert.assertFalse(DoclogRESTUtils.deleteDoclogRecord(id2, 0));

		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id1, 1));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id1, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id2, 1));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id2, 0));
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id1).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id2).size());
	}

	@Test
	public void testIDandFingerprintInOneRequest() throws Exception {
		ID id = Utils.getTestID();
		Fingerprint fingerprint = Utils.getTestFingerprint();

		Assert.assertEquals(Utils.DOCLOG_RECORD, DoclogRESTUtils
				.createDoclogRecord(fingerprint, id, Utils.DOCLOG_RECORD));

		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(fingerprint, 0));

		Assert.assertEquals(1, DoclogRESTUtils.readDoclog(id).size());
		Assert.assertEquals(1, DoclogRESTUtils.readDoclog(id).size());

		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id, 0));

		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(fingerprint, 0));

		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(fingerprint).size());
	}

	@Test
	public void testChangeOfIDdisallowed() throws Exception {
		ID id1 = Utils.getTestID();
		ID id2 = Utils.getTestID();
		Fingerprint fingerprint = Utils.getTestFingerprint();

		Assert.assertEquals(Utils.DOCLOG_RECORD, DoclogRESTUtils
				.createDoclogRecord(fingerprint, id1, Utils.DOCLOG_RECORD));
		Assert.assertEquals(Utils.DOCLOG_RECORD2, DoclogRESTUtils
				.createDoclogRecord(fingerprint, id2, Utils.DOCLOG_RECORD2));

		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id1, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(fingerprint, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id2, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(id1, 1));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(fingerprint, 1));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id2, 1));

		Assert.assertEquals(2, DoclogRESTUtils.readDoclog(id1).size());
		Assert.assertEquals(2, DoclogRESTUtils.readDoclog(fingerprint).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id2).size());

		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id1, 0));
		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(fingerprint, 0));
		Assert.assertFalse(DoclogRESTUtils.deleteDoclogRecord(id2, 0));

		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id1, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(fingerprint, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id2, 0));
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id1).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(fingerprint).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id2).size());
	}

	@Test
	public void testInvalidIDsIgnored() throws Exception {
		String id1 = "null";
		ID id2 = Utils.getTestID();
		Fingerprint fingerprint = Utils.getTestFingerprint();

		Assert.assertEquals(Utils.DOCLOG_RECORD, DoclogRESTUtils
				.createDoclogRecord(fingerprint, id1, Utils.DOCLOG_RECORD));
		Assert.assertEquals(Utils.DOCLOG_RECORD2, DoclogRESTUtils
				.createDoclogRecord(fingerprint, id2, Utils.DOCLOG_RECORD2));

		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id1, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(fingerprint, 0));
		Assert.assertEquals(Utils.DOCLOG_RECORD,
				DoclogRESTUtils.readDoclogRecord(id2, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id1, 1));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(fingerprint, 1));
		Assert.assertEquals(Utils.DOCLOG_RECORD2,
				DoclogRESTUtils.readDoclogRecord(id2, 1));

		Assert.assertNull(DoclogRESTUtils.readDoclog(id1));
		Assert.assertEquals(2, DoclogRESTUtils.readDoclog(fingerprint).size());
		Assert.assertEquals(2, DoclogRESTUtils.readDoclog(id2).size());

		Assert.assertFalse(DoclogRESTUtils.deleteDoclogRecord(id1, 0));
		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(fingerprint, 0));
		Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id2, 0));

		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id1, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(fingerprint, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id2, 0));
		Assert.assertNull(DoclogRESTUtils.readDoclog(id1));
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(fingerprint).size());
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id2).size());
	}

	@SuppressWarnings("serial")
	@Test
	public void testMissingOptionalParameter() throws Exception {
		ID id = Utils.getTestID();
		WebResource doclogService = DoclogRESTUtils.getDoclogREST(APIUAsrv);

		try {
			String success = doclogService.path(id.toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							// add("ip", DOCLOG_RECORD.getIp());
							// add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("event", "ready");
							// add("action", DOCLOG_RECORD.getAction());
							// add("actionParameter",
							// DOCLOG_RECORD.getActionParameter());
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertFalse(success.contains("error"));

			DoclogRecord doclogRecord = DoclogRESTUtils
					.readDoclogRecord(id, -1);
			Assert.assertEquals(Utils.DOCLOG_RECORD.getUrl(),
					doclogRecord.getUrl());
			Assert.assertEquals(DoclogAction.READY, doclogRecord.getAction());
			Assert.assertNull(doclogRecord.getActionParameter());
			Assert.assertEquals(Utils.DOCLOG_RECORD.getDateTime(),
					doclogRecord.getDateTime());
			Assert.assertEquals(Utils.DOCLOG_RECORD.getBounds(),
					doclogRecord.getBounds());

			DoclogRESTUtils.deleteDoclogRecord(id, 0);
			Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));

			Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}

		try {
			String success = doclogService.path(id.toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							// add("ip", DOCLOG_RECORD.getIp());
							// add("proxyIp", DOCLOG_RECORD.getProxyIp());
							add("event", "link-http-etc");
							// add("action", DOCLOG_RECORD.getAction());
							// add("actionParameter",
							// DOCLOG_RECORD.getActionParameter());
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertFalse(success.contains("error"));

			DoclogRecord doclogRecord = DoclogRESTUtils
					.readDoclogRecord(id, -1);
			Assert.assertEquals(Utils.DOCLOG_RECORD.getUrl(),
					doclogRecord.getUrl());
			Assert.assertEquals(DoclogAction.LINK, doclogRecord.getAction());
			Assert.assertEquals("http-etc", doclogRecord.getActionParameter());
			Assert.assertEquals(Utils.DOCLOG_RECORD.getDateTime(),
					doclogRecord.getDateTime());
			Assert.assertEquals(Utils.DOCLOG_RECORD.getBounds(),
					doclogRecord.getBounds());

			DoclogRESTUtils.deleteDoclogRecord(id, 0);
			Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));

			Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void testMissingRequiredParameter() throws Exception {
		WebResource doclogService = DoclogRESTUtils.getDoclogREST(APIUAsrv);

		try {
			String success = doclogService.path(Utils.getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							// add("url", DOCLOG_RECORD.getUrl());
							add("ip", Utils.DOCLOG_RECORD.getIp());
							add("proxyIp", Utils.DOCLOG_RECORD.getProxyIp());
							add("action", Utils.DOCLOG_RECORD.getAction());
							add("actionParameter",
									Utils.DOCLOG_RECORD.getActionParameter());
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertTrue(success.contains("error"));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}

		try {
			String success = doclogService.path(Utils.getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							add("ip", Utils.DOCLOG_RECORD.getIp());
							add("proxyIp", Utils.DOCLOG_RECORD.getProxyIp());
							// add("action", DOCLOG_RECORD.getAction());
							add("actionParameter",
									Utils.DOCLOG_RECORD.getActionParameter());
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertTrue(success.contains("error"));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}

		try {
			String success = doclogService.path(Utils.getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							add("ip", Utils.DOCLOG_RECORD.getIp());
							add("proxyIp", Utils.DOCLOG_RECORD.getProxyIp());
							add("action", Utils.DOCLOG_RECORD.getAction());
							add("actionParameter",
									Utils.DOCLOG_RECORD.getActionParameter());
							// add("dateTime", DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertTrue(success.contains("error"));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}

		try {
			String success = doclogService.path(Utils.getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							add("ip", Utils.DOCLOG_RECORD.getIp());
							add("proxyIp", Utils.DOCLOG_RECORD.getProxyIp());
							add("action", Utils.DOCLOG_RECORD.getAction());
							add("actionParameter",
									Utils.DOCLOG_RECORD.getActionParameter());
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							// add("bounds.x",
							// DOCLOG_RECORD.getBounds().getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertTrue(success.contains("error"));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}

		try {
			String success = doclogService.path(Utils.getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							add("ip", Utils.DOCLOG_RECORD.getIp());
							add("proxyIp", Utils.DOCLOG_RECORD.getProxyIp());
							add("action", Utils.DOCLOG_RECORD.getAction());
							add("actionParameter",
									Utils.DOCLOG_RECORD.getActionParameter());
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							// add("bounds.y",
							// DOCLOG_RECORD.getBounds().getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertTrue(success.contains("error"));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}

		try {
			String success = doclogService.path(Utils.getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							add("ip", Utils.DOCLOG_RECORD.getIp());
							add("proxyIp", Utils.DOCLOG_RECORD.getProxyIp());
							add("action", Utils.DOCLOG_RECORD.getAction());
							add("actionParameter",
									Utils.DOCLOG_RECORD.getActionParameter());
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							// add("bounds.width", DOCLOG_RECORD.getBounds()
							// .getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertTrue(success.contains("error"));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}

		try {
			String success = doclogService.path(Utils.getTestID().toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							add("ip", Utils.DOCLOG_RECORD.getIp());
							add("proxyIp", Utils.DOCLOG_RECORD.getProxyIp());
							add("action", Utils.DOCLOG_RECORD.getAction());
							add("actionParameter",
									Utils.DOCLOG_RECORD.getActionParameter());
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							// add("bounds.height", DOCLOG_RECORD.getBounds()
							// .getHeight());
						}
					});
			assertTrue(success.contains("error"));
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void testUnknownDoclogAction() throws Exception {
		ID id = Utils.getTestID();
		WebResource doclogService = DoclogRESTUtils.getDoclogREST(APIUAsrv);

		try {
			String success = doclogService.path(id.toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							add("event", "I_am_unknown");
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertFalse(success.contains("error"));

			DoclogRecord doclogRecord = DoclogRESTUtils
					.readDoclogRecord(id, -1);
			Assert.assertEquals(Utils.DOCLOG_RECORD.getUrl(),
					doclogRecord.getUrl());
			Assert.assertEquals(DoclogAction.UNKNOWN, doclogRecord.getAction());
			Assert.assertEquals("I_am_unknown",
					doclogRecord.getActionParameter());
			Assert.assertEquals(Utils.DOCLOG_RECORD.getDateTime(),
					doclogRecord.getDateTime());
			Assert.assertEquals(Utils.DOCLOG_RECORD.getBounds(),
					doclogRecord.getBounds());

			DoclogRESTUtils.deleteDoclogRecord(id, 0);
			Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
			Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(e.getMessage(), false);
		}

		try {
			String success = doclogService.path(id.toString())
					.type(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON)
					.post(String.class, new MultivaluedMapImpl() {
						{
							add("url", Utils.DOCLOG_RECORD.getUrl());
							add("event", "I_am_unknown-I_am_a_parameter");
							add("dateTime", Utils.DOCLOG_RECORD.getDateTime());
							add("bounds.x", Utils.DOCLOG_RECORD.getBounds()
									.getX());
							add("bounds.y", Utils.DOCLOG_RECORD.getBounds()
									.getY());
							add("bounds.width", Utils.DOCLOG_RECORD.getBounds()
									.getWidth());
							add("bounds.height", Utils.DOCLOG_RECORD
									.getBounds().getHeight());
						}
					});
			assertFalse(success.contains("error"));

			DoclogRESTUtils.deleteDoclogRecord(id, 0);
			Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
			Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
		} catch (UniformInterfaceException e) {
			Assert.assertTrue(false);
		}
	}

	@Test
	public void createDoclogWithLongID() throws Exception {
		DoclogRecord createdDoclogRecord = DoclogRESTUtils.createDoclogRecord(
				new ID(Utils.getRandomString(500)), Utils.DOCLOG_RECORD);
		Assert.assertNull(createdDoclogRecord);
	}

	@Test
	public void createDoclogWithLongFingerprint() throws Exception {
		DoclogRecord createdDoclogRecord = DoclogRESTUtils.createDoclogRecord(
				new Fingerprint("!" + Utils.getRandomString(500)),
				Utils.DOCLOG_RECORD);
		Assert.assertNull(createdDoclogRecord);
	}

	@Test
	public void fingerprintOnlyLoadTest() throws Exception {
		int cores = Runtime.getRuntime().availableProcessors();
		final int runtimeCycles = cores * RUNS_PER_CORE;

		final Fingerprint fingerprint = Utils.getTestFingerprint();

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
					assertEquals(Utils.DOCLOG_RECORD, DoclogRESTUtils
							.createDoclogRecord(fingerprint,
									Utils.DOCLOG_RECORD));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
			readTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					assertEquals(Utils.DOCLOG_RECORD,
							DoclogRESTUtils.readDoclogRecord(fingerprint, j));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
			deleteTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(
							fingerprint, 0));
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
		Assert.assertEquals(runtimeCycles,
				DoclogRESTUtils.readDoclog(fingerprint).size());
		System.out.print("Read  :");
		executorService.invokeAll(readTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles,
				DoclogRESTUtils.readDoclog(fingerprint).size());
		System.out.print("Delete:");
		executorService.invokeAll(deleteTasks);
		System.out.println(" .");
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(fingerprint, 0));
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(fingerprint).size());
	}

	@Test
	public void idOnlyLoadTest() throws Exception {
		int cores = Runtime.getRuntime().availableProcessors();
		final int runtimeCycles = cores * RUNS_PER_CORE;

		final ID id = Utils.getTestID();

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
					assertEquals(Utils.DOCLOG_RECORD, DoclogRESTUtils
							.createDoclogRecord(id, Utils.DOCLOG_RECORD));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
			readTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					assertEquals(Utils.DOCLOG_RECORD,
							DoclogRESTUtils.readDoclogRecord(id, j));
					if (j % (runtimeCycles / 10) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
			deleteTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(id, 0));
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
		Assert.assertEquals(runtimeCycles, DoclogRESTUtils.readDoclog(id)
				.size());
		System.out.print("Read  :");
		executorService.invokeAll(readTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles, DoclogRESTUtils.readDoclog(id)
				.size());
		System.out.print("Delete:");
		executorService.invokeAll(deleteTasks);
		System.out.println(" .");
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
	}

	@Test
	public void fingerprintAndIDLoadTest() throws Exception {
		int cores = Runtime.getRuntime().availableProcessors();
		final int runtimeCycles = cores * RUNS_PER_CORE;

		final Fingerprint fingerprint = Utils.getTestFingerprint();
		final ID id = Utils.getTestID();

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
						assertEquals(Utils.DOCLOG_RECORD, DoclogRESTUtils
								.createDoclogRecord(fingerprint,
										Utils.DOCLOG_RECORD));
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
							assertEquals(Utils.DOCLOG_RECORD,
									DoclogRESTUtils.createDoclogRecord(id,
											Utils.DOCLOG_RECORD));
						} else {
							assertEquals(Utils.DOCLOG_RECORD, DoclogRESTUtils
									.createDoclogRecord(fingerprint, id,
											Utils.DOCLOG_RECORD));
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
					assertEquals(Utils.DOCLOG_RECORD,
							DoclogRESTUtils.readDoclogRecord(fingerprint, j));
					if (j % (runtimeCycles / 5) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});
			readTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					assertEquals(Utils.DOCLOG_RECORD,
							DoclogRESTUtils.readDoclogRecord(id, j));
					if (j % (runtimeCycles / 5) == 0) {
						System.out.print(" .");
					}
					return null;
				}
			});

			deleteTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Assert.assertTrue(DoclogRESTUtils.deleteDoclogRecord(
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
		Assert.assertEquals(runtimeCycles, DoclogRESTUtils.readDoclog(id)
				.size());
		System.out.print("Read  :");
		executorService.invokeAll(readTasks);
		System.out.println(" .");
		Assert.assertEquals(runtimeCycles, DoclogRESTUtils.readDoclog(id)
				.size());
		System.out.print("Delete:");
		executorService.invokeAll(deleteTasks);
		System.out.println(" .");
		Assert.assertNull(DoclogRESTUtils.readDoclogRecord(id, 0));
		Assert.assertEquals(0, DoclogRESTUtils.readDoclog(id).size());
	}
}
