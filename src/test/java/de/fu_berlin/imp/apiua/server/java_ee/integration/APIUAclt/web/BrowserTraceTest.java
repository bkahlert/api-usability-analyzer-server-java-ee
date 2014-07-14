package de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAclt.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogAction;
import de.fu_berlin.imp.apiua.server.java_ee.model.Fingerprint;
import de.fu_berlin.imp.apiua.server.java_ee.model.ID;
import de.fu_berlin.imp.apiua.server.java_ee.model.IIdentifier;
import de.fu_berlin.imp.apiua.server.java_ee.model.Rectangle;
import de.fu_berlin.imp.apiua.server.java_ee.utils.DoclogRESTUtils;
import de.fu_berlin.imp.apiua.server.java_ee.utils.DoclogRecordBuilder;
import de.fu_berlin.imp.apiua.server.java_ee.utils.FingerprintRESTUtils;
import de.fu_berlin.imp.apiua.server.java_ee.utils.TestConfiguration;
import de.fu_berlin.imp.apiua.server.java_ee.utils.rules.StartTestSiteIfNeededRule;
import de.fu_berlin.imp.apiua.server.java_ee.utils.selenium.DocumentCompletedLoading;
import de.fu_berlin.imp.apiua.server.java_ee.utils.selenium.FrameNameAvailable;
import de.fu_berlin.imp.apiua.server.java_ee.utils.selenium.SeleniumUtils;
import de.fu_berlin.imp.apiua.server.java_ee.utils.selenium.SeleniumUtils.IWebDriverFactory;

@RunWith(value = Parameterized.class)
@Ignore
// FIXME Test-Browser merken sich kein localStorage, daher keine Simulation
// möglich
public class BrowserTraceTest {

	@ClassRule
	public static ExternalResource testRule = new StartTestSiteIfNeededRule();

	private static final Logger LOGGER = Logger
			.getLogger(BrowserTraceTest.class);

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

		IWebDriverFactory[] webDrivers = new IWebDriverFactory[] { SeleniumUtils
				.getSuffixedFirefoxDriverFactory("xxy") };

		URL[] urls = TestConfiguration.getAPIUAcltWebHostURLs();

		for (URL url : urls) {
			for (IWebDriverFactory webDriverFactory : webDrivers) {
				parameters.add(new Object[] {
						TestConfiguration.getDefaultAPIUAsrvURL(), url,
						webDriverFactory });
			}
			// TODO
			break;
		}

		return parameters;
	}

	private final URL APIUAsrvUrl;
	private final URL url;
	private static WebDriver driver;

	public BrowserTraceTest(URL APIUAsrvUrl, URL url,
			IWebDriverFactory webDriverFactory) {
		this.APIUAsrvUrl = APIUAsrvUrl;
		this.url = url;
		BrowserTraceTest.driver = webDriverFactory.create();
	}

	// TODO fingerprint change
	// TODO id change

	// TODO starten des Servers durch Unit Test damit in Testabdeckung

	@Test
	public void checkIfTestSiteSendsData() throws Exception {
		Dimension innerSize = new Dimension(800, 600);
		SeleniumUtils.setInnerSize(driver, innerSize);

		driver.get(APIUAsrvUrl.toExternalForm());
		System.err.println(SeleniumUtils.executeScript(driver,
				"return window.localStorage.getItem('lastFingerprint');"));
		System.err
				.println(SeleniumUtils
						.executeScript(driver,
								"return window.localStorage.setItem('lastFingerprint', 'testtest');"));

		driver.get(this.url.toExternalForm());

		DoclogRecordBuilder builder = new DoclogRecordBuilder().setUrl(
				this.url.toString()).setBounds(
				new Rectangle(0, 0, innerSize.width, innerSize.height));

		Fingerprint newFingerprint = waitForFingerprint();

		/*
		 * READY
		 */
		testReadyLog(newFingerprint, builder);

		URL APIUAsrvUrl = new URL((String) SeleniumUtils.executeScript(driver,
				"return window['APIUAsrvURL']"));
		ID id = FingerprintRESTUtils.getID(APIUAsrvUrl, newFingerprint);

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
	}

	private Fingerprint waitForFingerprint() {
		new WebDriverWait(driver, 3).until(ExpectedConditions
				.presenceOfElementLocated(By.id("APIUAsrv")));

		String frameName = new WebDriverWait(driver, 3)
				.until(new FrameNameAvailable("#APIUAsrv"));
		System.err.println(frameName);

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
	private void testReadyLog(IIdentifier identifier,
			DoclogRecordBuilder builder) throws Exception {
		builder.setDateTime(DateTime.now().minus(150))
				.setAction(DoclogAction.READY).setActionParameter(null);
		DoclogRESTUtils
				.testDoclogRecord(identifier, -1, builder.create(), 1000);
	}

	private void testLink(IIdentifier identifier, DoclogRecordBuilder builder,
			WebElement linkElement) throws Exception {
		String linkUrl = linkElement.getAttribute("href");
		builder.setDateTime(DateTime.now()).setAction(DoclogAction.UNLOAD)
				.setActionParameter(null);
		linkElement.click();

		// TODO DoclogAction.LINK is not always triggered
		Thread.sleep(100); // wait for the page to unload before starting for
							// the new page to load
		new WebDriverWait(driver, 3).until(new DocumentCompletedLoading());
		try {
			DoclogRESTUtils.testDoclogRecord(identifier, -2, builder.create(),
					2 * WebDataCollectionTest.baseDelay);
		} catch (AssertionError e) {
			LOGGER.warn(
					"No "
							+ DoclogAction.UNLOAD
							+ " "
							+ DoclogAction.class.getSimpleName()
							+ " was triggered."
							+ "\nThis can sometimes occur."
							+ " Please make sure this is no implementation error.",
					e);
		}
		builder.setUrl(linkUrl).setScrollPosition(0, 0)
				.setDateTime(DateTime.now()).setAction(DoclogAction.READY)
				.setActionParameter(null);
		DoclogRESTUtils.testDoclogRecord(identifier, -1, builder.create(),
				3 * WebDataCollectionTest.baseDelay);
	}

	@After
	public void after() throws Exception {
		driver.quit();
	}
}
