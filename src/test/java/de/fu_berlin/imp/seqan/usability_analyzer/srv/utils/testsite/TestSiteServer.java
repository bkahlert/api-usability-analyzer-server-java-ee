package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.testsite;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.TestConfiguration;

public class TestSiteServer {

	private static final Logger LOGGER = Logger.getLogger(TestSiteServer.class);

	private Connection connection = null;

	public void start() throws IOException, ConfigurationException {
		URL[] hostUrls = TestConfiguration.getSUAcltWebHostURLs();
		int port = hostUrls[0].getPort();
		this.start(port);
	}

	public void start(int port) throws IOException {
		Server server = new ContainerServer(new TestSiteContainer());
		connection = new SocketConnection(server);
		SocketAddress address = new InetSocketAddress(port);
		connection.connect(address);
		LOGGER.info(TestSiteServer.class.getSimpleName()
				+ " is now listening on port " + port);

	}

	public void stop() throws IOException {
		if (this.connection != null) {
			this.connection.close();
			LOGGER.info(TestSiteServer.class.getSimpleName() + " stopped");
			this.connection = null;
		}
	}
}
