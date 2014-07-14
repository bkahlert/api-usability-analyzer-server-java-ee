package de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAsrv;

import static org.junit.Assert.assertEquals;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.fu_berlin.imp.apiua.server.java_ee.utils.TestConfiguration;

@RunWith(Parameterized.class)
public class BlockedContentTest {

	@Parameters(name = "{0}")
	public static List<Object[]> getParameters() {
		Object[][] data = new Object[][] { new Object[] { "WEB-INF" },
				new Object[] { "WEB-INF/" },
				new Object[] { "WEB-INF/web.xml" },
				new Object[] { "META-INF" }, new Object[] { "META-INF/" },
				new Object[] { "META-INF/context.xml" },
				new Object[] { "META-INF/MANIFEST.MF" } };
		return Arrays.asList(data);
	}

	private String blockedPath;

	public BlockedContentTest(String blockedResource) {
		this.blockedPath = blockedResource;
	}

	@Test
	public void testBlocked() throws Exception {
		for (URL url : TestConfiguration
				.getAPIUAsrvURLs("/static/" + blockedPath)) {
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();

			assertEquals(404, responseCode);
		}
	}

}
