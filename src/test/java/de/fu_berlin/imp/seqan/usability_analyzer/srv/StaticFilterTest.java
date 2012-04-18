package de.fu_berlin.imp.seqan.usability_analyzer.srv;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

public class StaticFilterTest {

	private int getResponseCode(String path) throws IOException,
			MalformedURLException, ProtocolException {
		HttpURLConnection connection = (HttpURLConnection) new URL(
				Utils.getBaseURI() + "/static/" + path).openConnection();
		connection.setRequestMethod("GET");
		return connection.getResponseCode();
	}

	@Test
	public void testFilterWEBINF() throws Exception {
		Assert.assertEquals(404, getResponseCode("WEB-INF"));
		Assert.assertEquals(404, getResponseCode("WEB-INF/"));
		Assert.assertEquals(404, getResponseCode("WEB-INF/web.xml"));
	}

	@Test
	public void testFilterMETAINF() throws Exception {
		Assert.assertEquals(404, getResponseCode("META-INF"));
		Assert.assertEquals(404, getResponseCode("META-INF/"));
		Assert.assertEquals(404, getResponseCode("META-INF/context.xml"));
		Assert.assertEquals(404, getResponseCode("META-INF/MANIFEST.MF"));
	}

}
