package de.fu_berlin.imp.seqan.usability_analyzer.srv.integration.SUAclt.web.host;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.TestConfiguration;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.rules.StartTestSiteIfNeededRule;

@RunWith(value = Parameterized.class)
public class ClientIntegrationTest {

	@ClassRule
	public static ExternalResource testRule = new StartTestSiteIfNeededRule();

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() throws Exception {
		List<Object[]> parameters = new ArrayList<Object[]>();
		URL[] hostUrls = TestConfiguration.getSUAcltWebHostURLs();
		for (URL hostUrl : hostUrls) {
			parameters.add(new Object[] { hostUrl });
		}
		return parameters;
	}

	private final URL url;

	public ClientIntegrationTest(URL url) {
		this.url = url;
	}

	private static boolean isJQueryIncluded(Element element) {
		for (Element child : element.children()) {
			if (child.tagName().toLowerCase().equals("script")) {
				String src = child.attr("src");
				if (src == null)
					continue;
				if (src.endsWith("jquery.min.js") || src.endsWith("jquery.js")
						|| src.endsWith("jquery-1.3.2.min.js")) {
					return true;
				}
			}
		}
		return false;
	}

	@Test
	public void clientIsIncluded() throws Exception {
		Document doc = Utils.loadDocument(this.url);

		Element head = doc.select("head").first();
		Element body = doc.select("body").first();
		assertNotNull(head);
		if (isJQueryIncluded(head)) {
			Element last = head.children().last();
			assertTrue(
					this.url + "'s last head element is " + last.toString(),
					ArrayUtils.contains(Utils.getSUAcltWebCode(),
							last.toString()));
		} else if (body != null) {
			if (isJQueryIncluded(body)) {
				Element last = body.children().last();
				assertTrue(
						this.url + "'s last body element is " + last.toString(),
						ArrayUtils.contains(Utils.getSUAcltWebCode(),
								last.toString()));
			} else {
				fail("SUAclt.web include could not be found in the head nor in the body.");
			}
		} else {
			fail("SUAclt.web include could not be found in the head and the a body tag could not be found.");
		}
	}
}
