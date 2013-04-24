package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilderException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Rectangle;

public class DoclogRESTUtils {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogRESTUtils.class);

	public static WebResource getDoclogREST(URL suaSrv)
			throws URISyntaxException {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		return client
				.resource(new URI(suaSrv.toExternalForm() + "/rest/doclog"));
	}

	public static Doclog readDoclog(URL suaSrv, Object identifier)
			throws Exception {
		WebResource doclogService = getDoclogREST(suaSrv);
		try {
			return doclogService.path(identifier.toString()).path("test")
					.accept(MediaType.APPLICATION_XML).get(Doclog.class);
		} catch (UniformInterfaceException e) {

		}
		return null;
	}

	public static Doclog readDoclog(Object identifier) throws Exception {
		return readDoclog(TestConfiguration.getDefaultSUAsrvURL(), identifier);
	}

	public static DoclogRecord readDoclogRecord(URL suaSrv, Object identifier,
			int index) throws Exception {
		WebResource doclogService = getDoclogREST(suaSrv);

		try {
			return doclogService.path(identifier.toString())
					.path(new Integer(index).toString())
					.accept(MediaType.APPLICATION_XML).get(DoclogRecord.class);
		} catch (UniformInterfaceException e) {

		} catch (WebApplicationException e) {

		}
		return null;
	}

	public static DoclogRecord readDoclogRecord(Object identifier, int index)
			throws Exception {
		return readDoclogRecord(TestConfiguration.getDefaultSUAsrvURL(),
				identifier, index);
	}

	/**
	 * Returns the sorted {@link DoclogRecord}s identified by the given
	 * {@link IIdentifier} and the given indices.
	 * 
	 * @param identifier
	 * @param indices
	 * @return
	 * @throws MalformedURLException
	 * @throws ConfigurationException
	 * @throws UriBuilderException
	 * @throws IllegalArgumentException
	 */
	public static DoclogRecord[] readSortedDoclogRecords(Object identifier,
			int... indices) throws Exception {
		List<DoclogRecord> doclogRecords = new ArrayList<DoclogRecord>();
		for (int index : indices) {
			DoclogRecord doclogRecord = readDoclogRecord(identifier, index);
			doclogRecords.add(doclogRecord);
		}
		DoclogRecord[] sortedRecords = doclogRecords
				.toArray(new DoclogRecord[0]);
		Arrays.sort(sortedRecords, new Comparator<DoclogRecord>() {
			@Override
			public int compare(DoclogRecord o1, DoclogRecord o2) {
				return o1.getDateTime().compareTo(o2.getDateTime());
			}
		});
		return sortedRecords;
	}

	public static DoclogRecord createDoclogRecord(IIdentifier key,
			final DoclogRecord doclogRecord) throws Exception {
		return DoclogRESTUtils.createDoclogRecord(key, null, doclogRecord);
	}

	@SuppressWarnings("serial")
	public static DoclogRecord createDoclogRecord(URL suaSrv, IIdentifier key,
			final Object id, final DoclogRecord doclogRecord) throws Exception {
		WebResource doclogService = getDoclogREST(suaSrv);

		String success = doclogService.path(key.toString())
				.type(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.APPLICATION_XML)
				.post(String.class, new MultivaluedMapImpl() {
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
						add("bounds.width", doclogRecord.getBounds().getWidth());
						add("bounds.height", doclogRecord.getBounds()
								.getHeight());
					}
				});
		if (!success.contains("error")) {
			return readDoclogRecord(key, -1);
		}
		return null;
	}

	public static DoclogRecord createDoclogRecord(IIdentifier key,
			final Object id, final DoclogRecord doclogRecord) throws Exception {
		return createDoclogRecord(TestConfiguration.getDefaultSUAsrvURL(), key,
				id, doclogRecord);
	}

	public static boolean deleteDoclogRecord(URL suaSrv, Object identifier,
			int index) throws Exception {
		WebResource doclogService = getDoclogREST(suaSrv);

		String success = doclogService.path(identifier.toString())
				.path(new Integer(index).toString())
				.accept(MediaType.APPLICATION_XML).delete(String.class);
		return !success.contains("error");
	}

	public static boolean deleteDoclogRecord(Object identifier, int index)
			throws Exception {
		return deleteDoclogRecord(TestConfiguration.getDefaultSUAsrvURL(),
				identifier, index);
	}

	/**
	 * Tests if the {@link DoclogRecord} sufficiently equals the given
	 * {@link DoclogRecord}.
	 * <p>
	 * The actual {@link DoclogRecord} is retrieved from the REST server.
	 * 
	 * @param identifier
	 * @param index
	 * @param expected
	 * @param maxTimeDifference
	 * @throws SocketException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws ConfigurationException
	 * @throws UriBuilderException
	 * @throws IllegalArgumentException
	 */
	public static void testDoclogRecord(IIdentifier identifier, int index,
			DoclogRecord expected, int maxTimeDifference) throws Exception {
		DoclogRecord actual = readDoclogRecord(identifier, index);
		testDoclogRecord(expected, actual, maxTimeDifference);
	}

	/**
	 * Tests if the {@link DoclogRecord} sufficiently equals the given
	 * {@link DoclogRecord}.
	 * <p>
	 * The actual {@link DoclogRecord} is retrieved from the REST server.
	 * 
	 * @param expected
	 * @param actual
	 * @throws SocketException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 */
	public static void testDoclogRecord(DoclogRecord expected,
			DoclogRecord actual, int maxTimeDifference) throws SocketException,
			UnknownHostException, MalformedURLException {
		assertNotNull(actual);

		LOGGER.debug("Comparing " + DoclogRecord.class.getSimpleName()
				+ ":\n\t" + expected + "\n\t" + actual);

		if (expected.getIp() == null) {
			InetAddress[] inetAddresses = Utils.getIPAdresses();
			InetAddress inetAddress = InetAddress.getByName(actual.getIp());
			assertTrue(ArrayUtils.contains(inetAddresses, inetAddress));
		} else {
			assertEquals(expected.getIp(), actual.getIp());
		}
		assertEquals("Proxy IP differs", expected.getProxyIp(),
				actual.getProxyIp());

		if (expected.getUrl() != null) {
			List<String> expectedUrls = Arrays.asList(expected.getUrl(),
					UrlUtils.removeUserInfo(expected.getUrl()));
			assertTrue("URL " + actual.getUrl()
					+ " was not element of the possible expected urls "
					+ StringUtils.join(expectedUrls, " or "),
					expectedUrls.contains(expected.getUrl()));
		}

		assertEquals(DoclogAction.class.getSimpleName() + " differs",
				expected.getAction(), actual.getAction());
		assertEquals(DoclogAction.class.getSimpleName() + " parameter differs",
				expected.getActionParameter(), actual.getActionParameter());

		Rectangle actualBounds = actual.getBounds();
		Rectangle expectedBounds = expected.getBounds();
		if (actualBounds != null && expectedBounds != null) {
			if (expectedBounds.getX() > -1)
				assertEquals("X scroll position differs",
						expectedBounds.getX(), actualBounds.getX());
			if (expectedBounds.getY() > -1)
				assertEquals("Y scroll position differs",
						expectedBounds.getY(), actualBounds.getY());

			if (expectedBounds.getWidth() > -1) {
				int maxWidth = expectedBounds.getWidth();
				int minWidth = expectedBounds.getWidth() - 20;
				assertTrue(
						"The actual browser width (" + actualBounds.getWidth()
								+ ") is not within " + minWidth + " and "
								+ maxWidth, actualBounds.getWidth() <= maxWidth
								&& actualBounds.getWidth() >= minWidth);
			}

			if (expectedBounds.getHeight() > -1) {
				int maxHeight = expectedBounds.getHeight();
				int minHeight = expectedBounds.getHeight() - 20;
				assertTrue(
						"The actual browser height ("
								+ actualBounds.getHeight() + ") is not within "
								+ minHeight + " and " + maxHeight,
						actualBounds.getHeight() <= maxHeight
								&& actualBounds.getHeight() >= minHeight);
			}
		} else {
			assertTrue(actualBounds == null && expectedBounds == null);
		}
		long difference = expected.getDateTime().getMillis()
				- actual.getDateTime().getMillis();
		if (difference < -maxTimeDifference) {
			Assert.fail("Actual event occured " + (-difference)
					+ "ms to late although the tolerance is set to "
					+ maxTimeDifference + "ms.");
		} else if (difference > maxTimeDifference) {
			Assert.fail("Actual event occured " + difference
					+ "ms to early although the tolerance is set to "
					+ maxTimeDifference + "ms.");
		}
	}
}
