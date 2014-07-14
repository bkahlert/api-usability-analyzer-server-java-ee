package de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAclt.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.cookie.DateUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.fu_berlin.imp.apiua.server.java_ee.utils.TestConfiguration;

@RunWith(value = Parameterized.class)
public class ClientProvisionTest {

	private URL APIUAcltWebUrl;
	private HttpURLConnection connection;
	private String content;

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() throws Exception {
		List<Object[]> parameters = new ArrayList<Object[]>();
		URL[] APIUAcltWebUrls = TestConfiguration
				.getAPIUAsrvURLs("/static/js/APIUAclt.js");
		for (URL APIUAcltWebUrl : APIUAcltWebUrls) {
			parameters.add(new Object[] { APIUAcltWebUrl });
		}
		return parameters;
	}

	public ClientProvisionTest(URL APIUAcltWebUrl) {
		this.APIUAcltWebUrl = APIUAcltWebUrl;
	}

	@Before
	public void before() throws Exception {
		connection = (HttpURLConnection) APIUAcltWebUrl.openConnection();
		connection.setRequestMethod("GET");

		content = IOUtils.toString((InputStream) connection.getContent());
	}

	@After
	public void after() {
		connection.disconnect();
	}

	@Test
	public void testCorrectResponseCode() throws Exception {
		assertEquals(200, connection.getResponseCode());
	}

	@Test
	public void testCorrectMimeType() throws Exception {
		assertTrue(Arrays.asList("text/javascript", "application/javascript",
				"application/x-javascript").contains(
				connection.getContentType().split(";")[0]));
	}

	@Test
	public void testFreeOfPlaceholders() throws Exception {
		assertFalse(Pattern.compile("\\$\\{.*?\\}").matcher(content).find());
	}

	@Test
	public void testRequestedHostPresent() throws Exception {
		assertTrue(content.contains(APIUAcltWebUrl.getHost()));
	}

	@Test
	public void testRequestedPortPresent() throws Exception {
		assertTrue(content.contains(new Integer(APIUAcltWebUrl.getPort())
				.toString()));
	}

	@Test
	public void testCacheControlHeaderDefined() throws Exception {
		String cacheControl = connection.getHeaderField("Cache-Control");
		assertNotNull(cacheControl);
		assertTrue(cacheControl.contains("no-cache"));
		assertTrue(cacheControl.contains("no-store"));
		assertTrue(cacheControl.contains("must-revalidate"));
	}

	@Test
	public void testExpiresHeaderDefined() throws Exception {
		String exprires = connection.getHeaderField("Expires");
		assertNotNull(exprires);
		DateTime expiresDateTime = new DateTime(DateUtils.parseDate(exprires));
		assertNotNull(expiresDateTime);
		assertTrue(DateTime.now().isAfter(expiresDateTime));
	}

}
