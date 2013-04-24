package de.fu_berlin.imp.seqan.usability_analyzer.srv.integration.SUAclt.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Rectangle;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.DoclogRESTUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.DoclogRecordBuilder;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.TestConfiguration;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.UrlUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.rules.StartTestSiteIfNeededRule;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.selenium.DoclogRecordOfType;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.selenium.FrameNameAvailable;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.selenium.SeleniumUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.selenium.SeleniumUtils.IWebDriverFactory;

@RunWith(value = Parameterized.class)
public class WebDataCollectionTest {

	@ClassRule
	public static ExternalResource testRule = new StartTestSiteIfNeededRule();

	private static final Logger LOGGER = Logger
			.getLogger(WebDataCollectionTest.class);

	/**
	 * in SUAclt.js defined delay for RESIZE actions to be sent
	 */
	public static final int RESIZE_NOTIFICATION_DELAY = 1500;

	/**
	 * in SUAclt.js defined delay for SCROLL actions to be sent
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
	@Parameters(name = "{0} using {1}")
	public static Collection<Object[]> data() throws Exception {
		List<Object[]> parameters = new ArrayList<Object[]>();

		IWebDriverFactory[] webDrivers = new IWebDriverFactory[] {
				SeleniumUtils.getDefaultSafariDriverFactory(),
				SeleniumUtils.getDefaultFirefoxDriverFactory(),
				SeleniumUtils.getDefaultChromeDriverFactory() };

		// TODO remove
		System.setProperty("config", "system_test");

		URL[] urls = TestConfiguration.getSUAcltWebHostURLs();

		// urls = new URL[] { new URL("https://trac.seqan.de/") };

		for (URL url : urls) {
			for (IWebDriverFactory webDriverFactory : webDrivers) {
				// TODO fixme: currently SSL sites are consideres needing an
				// authentication
				// this information should be defined explicitly in a properties
				// file
				if (url.getProtocol().equals("https")
						&& !webDriverFactory.supportsAuthentication()) {
					LOGGER.info("The page "
							+ url
							+ " propably needs an authentication. Since "
							+ driver
							+ " does not support authentication is test case is skipped.");
					continue;
				}
				parameters.add(new Object[] { url, webDriverFactory });
			}
		}

		return parameters;
	}

	public final int baseDelay;
	private final URL url;
	private static WebDriver driver;

	public WebDataCollectionTest(URL url, IWebDriverFactory webDriverFactory)
			throws ConfigurationException {
		baseDelay = TestConfiguration.getBaseDelay();
		this.url = url;
		WebDataCollectionTest.driver = webDriverFactory.create();
	}

	// TODO starten des Servers durch Unit Test damit in Testabdeckung

	@Test
	public void checkIfTestSiteSendsData() throws Exception {
		Dimension innerSize = new Dimension(800, 600);

		SeleniumUtils.setInnerSize(driver, innerSize);
		String url = this.url.toExternalForm();
		if (this.url.getProtocol().equals("https")
				&& this.url.getUserInfo() == null) {
			String username = TestConfiguration.getUsername(this.url.getHost());
			String password = TestConfiguration.getPassword(this.url.getHost());
			if (username != null || password != null) {
				String userInfo = (username != null ? username : "") + ":"
						+ (password != null ? password : "");
				url = UrlUtils.addUserInfo(this.url, userInfo).toExternalForm();
			}
		}
		driver.get(url);

		DoclogRecordBuilder builder = new DoclogRecordBuilder().setUrl(url)
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

	private Fingerprint waitForFingerprint() {
		new WebDriverWait(driver, 3).until(ExpectedConditions
				.presenceOfElementLocated(By.id("SUAsrv")));

		String frameName = new WebDriverWait(driver, 3)
				.until(new FrameNameAvailable("#SUAsrv"));

		if (frameName == null) {
			fail("Could not find SUAsrv frame");
		}
		String[] frameNameParts = frameName.split("-");
		assertEquals(3, frameNameParts.length);
		assertEquals("SUAsrv", frameNameParts[0]);

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
	private void testReadyLog(IIdentifier identifier,
			DoclogRecordBuilder builder) throws Exception {
		builder.setDateTime(DateTime.now().minus(150))
				.setAction(DoclogAction.READY).setActionParameter(null);
		DoclogRESTUtils
				.testDoclogRecord(identifier, -1, builder.create(), 1000);
	}

	private void testScrollVertically(IIdentifier identifier,
			DoclogRecordBuilder builder) throws Exception {
		SeleniumUtils.scrollTo(driver, 0, 200);
		Thread.sleep(SCROLL_NOTIFICATION_DELAY);
		builder.setDateTime(DateTime.now());
		builder.setAction(DoclogAction.SCROLL).setActionParameter(null)
				.setY(200);
		Thread.sleep(baseDelay);
		DoclogRESTUtils.testDoclogRecord(identifier, -1, builder.create(),
				baseDelay);
	}

	private void testResize(IIdentifier identifier, DoclogRecordBuilder builder)
			throws Exception {
		SeleniumUtils.setInnerSize(driver, new Dimension(
				builder.getWidth() - 100, builder.getHeight()));
		Thread.sleep(RESIZE_NOTIFICATION_DELAY);
		builder.setDateTime(DateTime.now()).setAction(DoclogAction.RESIZE)
				.setActionParameter(null).setWidth(builder.getWidth() - 100);
		Thread.sleep(baseDelay);
		DoclogRESTUtils.testDoclogRecord(identifier, -1, builder.create(),
				baseDelay);
	}

	private void testTyping(IIdentifier identifier,
			DoclogRecordBuilder builder, WebElement input, String testInput)
			throws Exception {
		int delayBetweenStrokes = 120; // 500 strokes per minute

		input.click();

		// Some browsers (verified with Safari on Mac OS X) move the viewport if
		// an element gets focused but is not in the viewport. If that's the
		// case we can expect an scroll event to be logged. We want to adapt our
		// expectation to the new scroll position.
		DoclogRecord[] implicitScrollDoclogRecord = new WebDriverWait(driver,
				TestConfiguration.maxWebDriverFluentWait())
				.until(new DoclogRecordOfType(identifier, DoclogAction.SCROLL,
						1));
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
	private void testFocus(IIdentifier identifier, DoclogRecordBuilder builder)
			throws Exception {
		SeleniumUtils.executeScript(driver, "$(window).focus();");
		builder.setDateTime(DateTime.now()).setAction(DoclogAction.FOCUS)
				.setActionParameter(null);
		Thread.sleep(baseDelay);
		DoclogRESTUtils.testDoclogRecord(identifier, -1, builder.create(),
				baseDelay);
	}

	private void testBlur(IIdentifier identifier, DoclogRecordBuilder builder)
			throws Exception {
		SeleniumUtils.executeScript(driver, "$(window).blur();");
		builder.setDateTime(DateTime.now()).setAction(DoclogAction.BLUR)
				.setActionParameter(null);
		Thread.sleep(baseDelay);
		DoclogRESTUtils.testDoclogRecord(identifier, -1, builder.create(),
				baseDelay);
	}

	private void testLink(IIdentifier identifier, DoclogRecordBuilder builder,
			WebElement linkElement) throws Exception {
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
			// = anker link, only scroll expected
			expectedDoclogRecords.add(builder.setDateTime(DateTime.now())
					.setAction(DoclogAction.SCROLL).setActionParameter(null)
					.setScrollPosition(newX, newY).create());
		} else if (Utils.looksLikeSuaCltIsIncluded(linkUrl)) {
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

	private void testUnknown(IIdentifier identifier, DoclogRecordBuilder builder)
			throws Exception {
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
		driver.quit();
	}
}
