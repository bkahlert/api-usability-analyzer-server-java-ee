package de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAclt.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.After;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogAction;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogRecord;
import de.fu_berlin.imp.apiua.server.java_ee.model.Fingerprint;
import de.fu_berlin.imp.apiua.server.java_ee.model.IIdentifier;
import de.fu_berlin.imp.apiua.server.java_ee.model.Rectangle;
import de.fu_berlin.imp.apiua.server.java_ee.utils.DoclogRESTUtils;
import de.fu_berlin.imp.apiua.server.java_ee.utils.DoclogRecordBuilder;
import de.fu_berlin.imp.apiua.server.java_ee.utils.TestConfiguration;
import de.fu_berlin.imp.apiua.server.java_ee.utils.UrlUtils;
import de.fu_berlin.imp.apiua.server.java_ee.utils.Utils;
import de.fu_berlin.imp.apiua.server.java_ee.utils.rules.StartTestSiteIfNeededRule;
import de.fu_berlin.imp.apiua.server.java_ee.utils.selenium.DoclogRecordOfType;
import de.fu_berlin.imp.apiua.server.java_ee.utils.selenium.FrameNameAvailable;
import de.fu_berlin.imp.apiua.server.java_ee.utils.selenium.SeleniumUtils;
import de.fu_berlin.imp.apiua.server.java_ee.utils.selenium.SeleniumUtils.IWebDriverFactory;

@RunWith(value = Parameterized.class)
public class WebDataCollectionTest {

	@ClassRule
	public static ExternalResource testRule = new StartTestSiteIfNeededRule();

	private static final Logger LOGGER = Logger
			.getLogger(WebDataCollectionTest.class);

	/**
	 * in APIUAclt.js defined delay for RESIZE actions to be sent
	 */
	public static final int RESIZE_NOTIFICATION_DELAY = 1500;

	/**
	 * in APIUAclt.js defined delay for SCROLL actions to be sent
	 */
	public static final int SCROLL_NOTIFICATION_DELAY = 2000;

	/**
	 * Requests all {@link URL} to be tested and chooses randomly from a list of
	 * {@link WebDriver}s which one to use to test the data collection on the
	 * given {@link URL}.
	 * <p>
	 * The {@link WebDriver}s are started with a random browser agent in order
	 * to provoke the calculation of a new {@link Fingerprint}.</li> </ul>
	 * 
	 * @return
	 * @throws Exception
	 */
	@Parameters(name = "{1} -> {0}")
	public static Collection<Object[]> data() throws Exception {

		StartTestSiteIfNeededRule localServer = new StartTestSiteIfNeededRule();
		localServer.start();

		List<Object[]> parameters = new ArrayList<Object[]>();

		IWebDriverFactory[] webDrivers = new IWebDriverFactory[] {
				SeleniumUtils.getDefaultSafariDriverFactory(),
				SeleniumUtils.getDefaultFirefoxDriverFactory(),
				SeleniumUtils.getDefaultChromeDriverFactory() };

		URL[] urls = TestConfiguration.getAPIUAcltWebHostURLs();

		List<URL> urlsToBeTested = new ArrayList<URL>();
		for (URL url : urls) {
			if (urlsToBeTested.contains(url))
				continue;

			LOGGER.info("Checking how to test " + url);
			boolean containsFrames = false;
			boolean framesHaveNoSUA = true;

			Document doc;
			try {
				doc = Utils.loadDocument(url);
			} catch (SocketTimeoutException e) {
				LOGGER.warn(url
						+ " can't be checked it connecting to it timed out");
				continue;
			}

			if (doc.select("frameset").size() > 0) {
				containsFrames = true;
				LOGGER.info("... contains frames thus testing the following pages instead");
				for (Element frame : doc.select("frame")) {
					String baseUri = frame.baseUri();
					baseUri = baseUri
							.substring(0, baseUri.lastIndexOf("/") + 1);
					URL frameUrl = new URL(baseUri + frame.attr("src"));
					if (Utils.looksLikeAPIUAcltIsIncluded(frameUrl
							.toExternalForm())) {
						if (!urlsToBeTested.contains(frameUrl)) {
							LOGGER.info("... " + frameUrl);
							urlsToBeTested.add(frameUrl);
						}
						framesHaveNoSUA = false;
					} else {
						LOGGER.info("... ignore because of no SUA code: "
								+ frameUrl);
					}
				}
			}

			if (doc.select("iframe").size() > 0) {
				containsFrames = true;
				LOGGER.info("... contains iframes thus testing the following pages instead");
				for (Element iframe : doc.select("iframe")) {
					String baseUri = iframe.baseUri();
					baseUri = baseUri
							.substring(0, baseUri.lastIndexOf("/") + 1);
					URL iframeUrl = new URL(baseUri + iframe.attr("src"));
					if (Utils.looksLikeAPIUAcltIsIncluded(iframeUrl
							.toExternalForm())) {
						if (!urlsToBeTested.contains(iframeUrl)) {
							LOGGER.info("... " + iframeUrl);
							urlsToBeTested.add(iframeUrl);
						}
					} else {
						LOGGER.info("... ignore because of no SUA code: "
								+ iframeUrl);
					}
				}
			}

			if (containsFrames) {
				if (framesHaveNoSUA && !urlsToBeTested.contains(url)) {
					LOGGER.info("... adding " + url);
					urlsToBeTested.add(url);
				} else {
					LOGGER.warn("... ignoring since test of nested SUA enabled pages is not supported: "
							+ url);
				}
			} else {
				LOGGER.info("... adding " + url);
				urlsToBeTested.add(url);
			}
		}

		for (URL urlToBeTested : urlsToBeTested) {
			for (IWebDriverFactory webDriverFactory : webDrivers) {
				if (UrlUtils.needsAuthentication(urlToBeTested, "GET")
						&& !webDriverFactory.supportsAuthentication()) {
					LOGGER.warn(urlToBeTested
							+ " authentication. Since "
							+ webDriverFactory
							+ " does not support authentication this test case is skipped.");
					continue;
				}
				parameters
						.add(new Object[] { urlToBeTested, webDriverFactory });
			}
		}

		localServer.stop();

		return parameters;
	}

	public final static int baseDelay = TestConfiguration.getBaseDelay();
	private final URL url;
	private static WebDriver driver;

	public WebDataCollectionTest(URL url, IWebDriverFactory webDriverFactory)
			throws ConfigurationException {
		this.url = url;
		WebDataCollectionTest.driver = webDriverFactory.create();
	}

	// TODO starten des Servers durch Unit Test damit in Testabdeckung

	@Test
	public void checkIfSendsData() throws Exception {
		String authUrl = UrlUtils.getAuthUrl(url, "GET").toExternalForm();

		Dimension innerSize = new Dimension(800, 600);
		SeleniumUtils.setInnerSize(driver, innerSize);
		driver.get(authUrl);

		DoclogRecordBuilder builder = new DoclogRecordBuilder().setUrl(authUrl)
				.setBounds(
						new Rectangle(0, 0, innerSize.width, innerSize.height));

		Fingerprint newFingerprint = waitForFingerprint();

		/*
		 * READY
		 */
		testReadyLog(newFingerprint, builder);

		/*
		 * SCROLL VERTICALLY
		 */
		testScrollVertically(newFingerprint, builder);

		/*
		 * RESIZE
		 */
		testResize(newFingerprint, builder);

		/*
		 * SCROLL HORIZONTALLY
		 */
		// TODO SeleniumUtils.scrollTo(driver, 50, 200);

		/*
		 * TYPING IN ALL INPUTs
		 */
		for (WebElement input : driver.findElements(By.tagName("input"))) {
			if (!Arrays.asList("text", "search").contains(
					input.getAttribute("type")))
				continue;
			testTyping(newFingerprint, builder, input, "a€");
		}

		/*
		 * TYPING IN ALL TEXTAREAs
		 */
		for (WebElement input : driver.findElements(By.tagName("textarea"))) {
			testTyping(newFingerprint, builder, input, "a€ \nABC");
		}

		/*
		 * BLUR
		 */
		testBlur(newFingerprint, builder);

		/*
		 * FOCUS
		 */
		// TODO
		// testFocus(newFingerprint, builder);

		// TODO zumindest in Firefox
		// TODO server leeren
		// TODO Fingerprint-Wechsel

		/*
		 * LINK / UNLOAD / READY
		 */
		List<WebElement> links = driver.findElements(By.tagName("a"));
		assertTrue(
				"The page does not contain any links. At least one link is needed to test the "
						+ DoclogAction.LINK + " event", links.size() > 0);
		WebElement randomLink = links
				.get((int) (Math.random() * (links.size())));
		LOGGER.info("Following link " + randomLink.getAttribute("href"));
		testLink(newFingerprint, builder, randomLink);

		/*
		 * UNKNOWN
		 */
		testUnknown(newFingerprint, builder);
	}

	private static Fingerprint waitForFingerprint() {
		new WebDriverWait(driver, 3).until(ExpectedConditions
				.presenceOfElementLocated(By.id("APIUAsrv")));

		String frameName = new WebDriverWait(driver, 3)
				.until(new FrameNameAvailable("#APIUAsrv"));

		if (frameName == null) {
			fail("Could not find APIUAsrv frame");
		}
		String[] frameNameParts = frameName.split("-");
		assertEquals(3, frameNameParts.length);
		assertEquals("APIUAsrv", frameNameParts[0]);

		Fingerprint oldFingerprint = new Fingerprint("!" + frameNameParts[1]);
		Fingerprint newFingerprint = new Fingerprint("!" + frameNameParts[2]);

		assertNotNull(oldFingerprint);
		assertNotNull(newFingerprint);

		return newFingerprint;
	}

	/**
	 * This method must be called right after the {@link Fingerprint} has been
	 * retrieved.
	 * 
	 * @param identifier
	 * @param builder
	 * @throws Exception
	 */
	private static void testReadyLog(IIdentifier identifier,
			DoclogRecordBuilder builder) throws Exception {
		builder.setDateTime(DateTime.now().minus(150))
				.setAction(DoclogAction.READY).setActionParameter(null);
		DoclogRESTUtils
				.testDoclogRecord(identifier, -1, builder.create(), 1000);
	}

	private static void testScrollVertically(IIdentifier identifier,
			DoclogRecordBuilder builder) throws Exception {
		SeleniumUtils.scrollTo(driver, 0, 200);
		Thread.sleep(SCROLL_NOTIFICATION_DELAY);
		builder.setDateTime(DateTime.now());
		builder.setAction(DoclogAction.SCROLL).setActionParameter(null)
				.setY(200);
		DoclogRecord[] doclogRecords = new WebDriverWait(driver,
				TestConfiguration.maxWebDriverFluentWait())
				.until(new DoclogRecordOfType(identifier, DoclogAction.SCROLL,
						1));
		DoclogRESTUtils.testDoclogRecord(builder.create(), doclogRecords[0],
				baseDelay);
	}

	private static void testResize(IIdentifier identifier,
			DoclogRecordBuilder builder) throws Exception {
		SeleniumUtils.setInnerSize(driver, new Dimension(
				builder.getWidth() - 100, builder.getHeight()));
		Thread.sleep(RESIZE_NOTIFICATION_DELAY);
		builder.setDateTime(DateTime.now()).setAction(DoclogAction.RESIZE)
				.setActionParameter(null).setWidth(builder.getWidth() - 100);
		Thread.sleep(baseDelay);
		DoclogRESTUtils.testDoclogRecord(identifier, -1, builder.create(),
				baseDelay);
	}

	private static void testTyping(IIdentifier identifier,
			DoclogRecordBuilder builder, WebElement input, String testInput)
			throws Exception {
		int delayBetweenStrokes = 120; // 500 strokes per minute

		// only test visible inputs
		Assume.assumeTrue(input.isDisplayed());

		input.click();

		// Some browsers (verified with Safari on Mac OS X) move the viewport if
		// an element gets focused but is not in the viewport. If that's the
		// case we can expect an scroll event to be logged. We want to adapt our
		// expectation to the new scroll position.
		DoclogRecord[] implicitScrollDoclogRecord = null;
		try {
			implicitScrollDoclogRecord = new WebDriverWait(driver,
					TestConfiguration.maxWebDriverFluentWait())
					.until(new DoclogRecordOfType(identifier,
							DoclogAction.SCROLL, 1));
		} catch (TimeoutException e) {
		}
		if (implicitScrollDoclogRecord != null
				&& implicitScrollDoclogRecord.length == 1) {
			builder.setX(implicitScrollDoclogRecord[0].getBounds().getX());
			builder.setY(implicitScrollDoclogRecord[0].getBounds().getY());
			LOGGER.info("Focussing the input field (id=\""
					+ input.getAttribute("id")
					+ "\", name=\""
					+ input.getAttribute("name")
					+ "\") made the browser change its viewport. The new scroll position is "
					+ new Point(implicitScrollDoclogRecord[0].getBounds()
							.getX(), implicitScrollDoclogRecord[0].getBounds()
							.getY()) + ".");
		}

		DateTime[] timesOfKeySends = new DateTime[testInput.length()];
		int[] compareIndices = new int[testInput.length()];

		for (int i = 0; i < testInput.length(); i++) {
			if (i != 0) {
				Thread.sleep(delayBetweenStrokes);
			}
			timesOfKeySends[i] = DateTime.now();
			String key = testInput.substring(i, i + 1);
			input.sendKeys(key);
			compareIndices[i] = -testInput.length() + i;
		}

		builder.setAction(DoclogAction.TYPING).setActionParameter(null);
		Thread.sleep(baseDelay);
		DoclogRecord[] inputRecords = DoclogRESTUtils.readSortedDoclogRecords(
				identifier, compareIndices);

		for (int i = 0; i < testInput.length(); i++) {
			builder.setDateTime(timesOfKeySends[i]);
			builder.setActionParameter(SeleniumUtils.getIdOrName(input) + "-"
					+ testInput.substring(0, i + 1));
			DoclogRESTUtils.testDoclogRecord(builder.create(), inputRecords[i],
					baseDelay);
		}
	}

	@SuppressWarnings("unused")
	private static void testFocus(IIdentifier identifier,
			DoclogRecordBuilder builder) throws Exception {
		SeleniumUtils.executeScript(driver, "$(window).focus();");
		builder.setDateTime(DateTime.now()).setAction(DoclogAction.FOCUS)
				.setActionParameter(null);
		Thread.sleep(baseDelay);
		DoclogRESTUtils.testDoclogRecord(identifier, -1, builder.create(),
				baseDelay);
	}

	private static void testBlur(IIdentifier identifier,
			DoclogRecordBuilder builder) throws Exception {
		SeleniumUtils.executeScript(driver, "$(window).blur();");
		builder.setDateTime(DateTime.now()).setAction(DoclogAction.BLUR)
				.setActionParameter(null);
		Thread.sleep(baseDelay);
		DoclogRESTUtils.testDoclogRecord(identifier, -1, builder.create(),
				baseDelay);
	}

	private static void testLink(IIdentifier identifier,
			DoclogRecordBuilder builder, WebElement linkElement)
			throws Exception {
		String linkUrl = linkElement.getAttribute("href");
		boolean pageContainsFrames = Utils.pageContainsFrames(linkUrl);

		List<DoclogRecord> expectedDoclogRecords = new ArrayList<DoclogRecord>();
		expectedDoclogRecords.add(builder.setDateTime(DateTime.now())
				.setAction(DoclogAction.LINK).setActionParameter(linkUrl)
				.create());

		int newX = 0;
		int newY = 0;
		// #anker lead to some unknown scroll position
		if (linkUrl.contains("#")) {
			newX = -1;
			newY = -1;
		}

		if (UrlUtils.referencesSamePage(builder.getUrl(), linkUrl)) {
			if (linkUrl.contains("#") && !linkUrl.endsWith("#")) {
				// = anker link, only scroll expected
				expectedDoclogRecords.add(builder.setDateTime(DateTime.now())
						.setAction(DoclogAction.SCROLL)
						.setActionParameter(null).setScrollPosition(newX, newY)
						.create());
			} else {
				// nothing expected
				return;
			}
		} else if (Utils.looksLikeAPIUAcltIsIncluded(linkUrl)) {
			// = different page with SUA
			String urlToCheck = pageContainsFrames ? linkUrl : null;
			int newWidth = builder.getWidth();
			int newHeight = builder.getHeight();
			if (pageContainsFrames) {
				newWidth = -1;
				newHeight = -1;
			}

			expectedDoclogRecords.add(builder.setDateTime(DateTime.now())
					.setAction(DoclogAction.UNLOAD).setActionParameter(null)
					.create());
			expectedDoclogRecords.add(builder
					.setUrl(urlToCheck)
					.setBounds(new Rectangle(newX, newY, newWidth, newHeight))
					.setDateTime(
							DateTime.now().plus(
									2 * TestConfiguration.getBaseDelay()))
					.setAction(DoclogAction.READY).setActionParameter(null)
					.create());
		} else {
			// = different page without SUA
			expectedDoclogRecords.add(builder.setDateTime(DateTime.now())
					.setAction(DoclogAction.UNLOAD).setActionParameter(null)
					.create());
			// TODO ... surprisingly BLUR instead of UNLOAD is logged
			expectedDoclogRecords.get(expectedDoclogRecords.size() - 1)
					.setAction(DoclogAction.BLUR);
		}

		linkElement.click();

		// wait for the last expected DoclogRecord's action type to be logged
		// and return the same number of DoclogRecords
		DoclogRecord[] actualDoclogRecords = new WebDriverWait(driver,
				TestConfiguration.maxWebDriverFluentWait())
				.until(new DoclogRecordOfType(identifier, expectedDoclogRecords
						.get(expectedDoclogRecords.size() - 1).getAction(),
						expectedDoclogRecords.size()));

		// LINK and UNLOAD events are not working reliable. Only check their
		// correctness if they have been logged.
		for (int i = 0; i < actualDoclogRecords.length; i++) {
			DoclogAction currentAction = actualDoclogRecords[i].getAction();
			if (currentAction == DoclogAction.LINK
					|| currentAction == DoclogAction.UNLOAD) {
				for (int j = 0; j < expectedDoclogRecords.size(); j++) {
					if (expectedDoclogRecords.get(j).getAction() == currentAction) {
						DoclogRESTUtils.testDoclogRecord(
								expectedDoclogRecords.get(j),
								actualDoclogRecords[i],
								TestConfiguration.getBaseDelay());
					}
				}
			}
		}

		if (expectedDoclogRecords.get(expectedDoclogRecords.size() - 1)
				.getAction() == DoclogAction.READY) {
			DoclogRESTUtils
					.testDoclogRecord(
							expectedDoclogRecords.get(expectedDoclogRecords
									.size() - 1),
							actualDoclogRecords[expectedDoclogRecords.size() - 1],
							2 * TestConfiguration.getBaseDelay());
		}
	}

	private static void testUnknown(IIdentifier identifier,
			DoclogRecordBuilder builder) throws Exception {
		SeleniumUtils.executeScript(driver, "SUAtestLog(\""
				+ identifier.toString().substring(1)
				+ "\", \"IamAnUnknownActionType-IamTheParameter\");");
		builder.setDateTime(DateTime.now()).setAction(DoclogAction.UNKNOWN)
				.setActionParameter("IamAnUnknownActionType-IamTheParameter");

		DoclogRecord[] actualUnknownRecord = new WebDriverWait(driver,
				TestConfiguration.maxWebDriverFluentWait())
				.until(new DoclogRecordOfType(identifier, DoclogAction.UNKNOWN,
						1));

		assertTrue(actualUnknownRecord != null
				&& actualUnknownRecord.length == 1);

		DoclogRESTUtils.testDoclogRecord(builder.create(),
				actualUnknownRecord[0], baseDelay);
	}

	@After
	public void after() throws Exception {
		if (driver != null)
			driver.quit();
	}
}
