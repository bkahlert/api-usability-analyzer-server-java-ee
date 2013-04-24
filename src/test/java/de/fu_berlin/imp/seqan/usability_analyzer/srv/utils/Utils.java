package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Rectangle;

public class Utils {

	private static final Logger LOGGER = Logger.getLogger(Utils.class);

	public static InetAddress PUBLIC_IP;

	static {
		try {
			PUBLIC_IP = InetAddress.getByName(IOUtils
					.toString((InputStream) new URL(
							"http://api.exip.org/?call=ip").getContent()));
		} catch (Exception e) {
			LOGGER.warn("Could not determine your public IP address", e);
		}
	}

	public static String getRandomString(int num) {
		return RandomStringUtils
				.random(num,
						"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
	}

	public static ID getTestID() {
		return new ID("TEST" + getRandomString(32));
	}

	public static Fingerprint getTestFingerprint() {
		return new Fingerprint("!TEST" + getRandomString(32));
	}

	/**
	 * optional <code>type="text/javascript"</code> attribute
	 * 
	 * @return
	 * @throws ConfigurationException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	public static String[] getSUAcltWebCode() throws MalformedURLException,
			ConfigurationException, URISyntaxException {
		String relativePath = "/static/js/SUAclt.js";
		String[] skeletons = new String[] { "<script src=\"${URL}\"></script>",
				"<script type=\"text/javascript\" src=\"${URL}\"></script>" };

		List<String> urls = new ArrayList<String>();
		// classic urls
		for (URL url : TestConfiguration.getSUAsrvURLs(relativePath)) {
			urls.add(url.toExternalForm());
		}
		// schema-relative urls
		for (URI uri : TestConfiguration
				.getSchemaRelativeSUAsrvURIs(relativePath)) {
			urls.add(uri.toString());
		}

		List<String> codes = new ArrayList<String>();
		for (String url : urls) {
			for (String skeleton : skeletons) {
				codes.add(skeleton.replace("${URL}", url));
			}
		}
		return codes.toArray(new String[0]);
	}

	public static final DoclogRecord DOCLOG_RECORD = new DoclogRecord(
			"http://bkahlert.com#interestingStuff",
			"192.168.0.5",
			"2001:0db8:85a3:08d3:1319:8a2e:0370:7344",
			DoclogAction.TYPING,
			"text-Work in PROGRESS.\n\nWelcome to the \"Hello World\" of SeqAn. This is the first tutorial you should look at when starting to use our software library\n\n	This tutorial will briefly introduce you to basic concepts and explain certain design decisions.\nWe assume that you have some programming experience (preferably in C++ or C) and concentrate on SeqAn specific aspect.",
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

	/**
	 * Connects to the given {@link URL} and returns the returned and parsed
	 * {@link Document}.
	 * <p>
	 * Should the website request credentials this method tries to look them up
	 * using {@link TestConfiguration#getUsername(String)}.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static Document loadDocument(URL url) throws IOException {
		Document doc = null;
		try {
			doc = Jsoup.connect(url.toString()).get();
		} catch (HttpStatusException e) {
			if (e.getStatusCode() == 401) {
				// could fail because of requested authentication
				String username = TestConfiguration.getUsername(url.getHost());
				String password = TestConfiguration.getPassword(url.getHost());
				String login = username + ":" + password;
				String base64login = new String(Base64.encodeBase64(login
						.getBytes()));
				doc = Jsoup.connect(url.toString())
						.header("Authorization", "Basic " + base64login).get();
			}
		}
		return doc;
	}

	public static InetAddress[] getIPAdresses() throws SocketException {
		List<InetAddress> addrList = new ArrayList<InetAddress>();
		for (Enumeration<NetworkInterface> nics = NetworkInterface
				.getNetworkInterfaces(); nics.hasMoreElements();) {
			NetworkInterface nic = nics.nextElement();
			if (nic.isUp()) {
				for (Enumeration<InetAddress> addresses = nic
						.getInetAddresses(); addresses.hasMoreElements();) {
					InetAddress address = addresses.nextElement();
					addrList.add(address);
				}
			}
		}
		addrList.add(PUBLIC_IP);
		return addrList.toArray(new InetAddress[0]);
	}

	public static boolean isLocalAddress(InetAddress addr) {
		if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
			return true;

		try {
			return NetworkInterface.getByInetAddress(addr) != null;
		} catch (SocketException e) {
			return false;
		}
	}

	public static boolean isLocalAddress(String host) {
		try {
			for (InetAddress addr : InetAddress.getAllByName(host)) {
				if (isLocalAddress(addr)) {
					return true;
				}
			}
		} catch (UnknownHostException e) {
			LOGGER.info("Could not determine "
					+ InetAddress.class.getSimpleName() + " for " + host);
		}
		return false;
	}

	public static boolean looksLikeSuaCltIsIncluded(String linkUrl)
			throws ConfigurationException, URISyntaxException, IOException {
		Document doc = Utils.loadDocument(new URL(linkUrl));

		Element head = doc.select("head").first();
		Element body = doc.select("body").first();
		for (Element secondLevelElement : Arrays.asList(head, body)) {
			for (Element thirdLevelElement : secondLevelElement.children()) {
				if (thirdLevelElement.tagName().equals("script")) {
					if (thirdLevelElement.attr("src").contains("SUAclt.js"))
						return true;
				}
			}
		}

		return false;
	}

	@Test
	public void testLooksLikeSuaCltIsIncluded() throws Exception {
		assertTrue(looksLikeSuaCltIsIncluded("http://trac.seqan.de"));
		assertTrue(looksLikeSuaCltIsIncluded("http://docs.seqan.de/seqan/dev/"));

		assertFalse(looksLikeSuaCltIsIncluded("http://google.de"));
		assertFalse(looksLikeSuaCltIsIncluded("http://bkahlert.com"));
	}

	public static boolean pageContainsFrames(String url)
			throws ConfigurationException, URISyntaxException, IOException {
		Document doc = Utils.loadDocument(new URL(url));

		if (doc.getElementsByTag("iframe").size() > 0)
			return true;
		if (doc.getElementsByTag("frameset").size() > 0)
			return true;

		return false;
	}

	@Test
	public void testPageContainsFrames() throws Exception {
		assertTrue(pageContainsFrames("http://docs.seqan.de/seqan/dev/"));
		assertTrue(pageContainsFrames("http://docs.seqan.de/seqan/dev2/"));

		assertFalse(pageContainsFrames("http://trac.seqan.de"));
		assertFalse(pageContainsFrames("http://google.de"));
		assertFalse(pageContainsFrames("http://bkahlert.com"));
	}
}
