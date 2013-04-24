package de.fu_berlin.imp.seqan.usability_analyzer.srv;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;

public class Utils {
	public static String getRandomString(int num) {
		return RandomStringUtils
				.random(num,
						"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
	}

	public static Properties getProperties() {
		Properties props = new Properties();
		InputStream fis = Utils.class.getResourceAsStream("test.properties");
		try {
			props.load(fis);
		} catch (IOException e) {
			System.err.println("Error reading test properties");
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return props;
	}

	public static URI getBaseURI() {
		return UriBuilder.fromUri(
				Utils.getProperties().getProperty("SUAsrv.url")).build();
	}
}
