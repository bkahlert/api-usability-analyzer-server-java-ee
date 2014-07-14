package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.rules;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.rules.ExternalResource;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.testsite.TestSiteServer;

public class StartTestSiteIfNeededRule extends ExternalResource {

	private static final Logger LOGGER = Logger
			.getLogger(StartTestSiteIfNeededRule.class);
	private TestSiteServer testSiteServer;

	public void start() {
		boolean serverAlreadyStarted = false;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(
					"http://localhost:8081").openConnection();
			connection.getResponseCode();
			serverAlreadyStarted = true;
		} catch (Exception e) {
		}

		if (!serverAlreadyStarted) {
			testSiteServer = new TestSiteServer();
			try {
				testSiteServer.start(8081);
			} catch (IOException e) {
				LOGGER.fatal(
						"Error starting "
								+ TestSiteServer.class.getSimpleName(), e);
			}
		}
	}

	public void stop() {
		if (testSiteServer != null) {
			try {
				testSiteServer.stop();
			} catch (IOException e) {
				LOGGER.fatal(
						"Error stopping "
								+ TestSiteServer.class.getSimpleName(), e);
			}
		}
	}

	@Override
	protected void before() {
		start();
	};

	@Override
	protected void after() {
		stop();
	};
}
