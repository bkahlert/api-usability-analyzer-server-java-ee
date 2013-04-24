package de.fu_berlin.imp.seqan.usability_analyzer.srv.clt;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.Utils;

public class SUAcltTest {

	@Test
	public void testSUAcltAvailability() throws Exception {
		HttpURLConnection connection = (HttpURLConnection) new URL(
				Utils.getBaseURI() + "/static/js/SUAclt.js").openConnection();
		connection.setRequestMethod("GET");
		Assert.assertEquals(200, connection.getResponseCode());
		Assert.assertTrue(connection.getContentType().contains(
				"text/javascript"));
		String content = IOUtils
				.toString((InputStream) connection.getContent());
		Assert.assertTrue("SUAclt.js does not contain jQuery",
				content.contains("jQuery"));
		Assert.assertTrue("SUAclt.js does not contain the requested host",
				content.contains(Utils.getBaseURI().getHost()));
		Assert.assertTrue("SUAclt.js does not contain the requested port",
				content.contains(new Integer(Utils.getBaseURI().getPort())
						.toString()));
	}
}
