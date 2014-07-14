package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.openqa.selenium.support.ui.FluentWait;

public class TestConfiguration {

	private static String getConfigurationBasename() {
		return System.getProperty("config", "integration_test");
	}

	/**
	 * Returns the {@link Configuration} stored in the corresponding file
	 * located in Maven's test resources.
	 * 
	 * @param basename
	 * @return
	 * @throws ConfigurationException
	 */
	public static Configuration getProperties(String basename)
			throws ConfigurationException {
		URL url = TestConfiguration.class.getResource("../" + basename
				+ ".properties");
		return new PropertiesConfiguration(url);
	}

	public static Configuration getModeProperties()
			throws ConfigurationException {
		String basename = getConfigurationBasename();
		return getProperties(basename);
	}

	/**
	 * Returns the {@link Configuration} that stores the credentials to access
	 * the given domain.
	 * 
	 * @param domain
	 * @return
	 */
	private static Configuration getCredentials(String domain) {
		URL url = TestConfiguration.class.getResource("../credentials/"
				+ domain + ".properties");
		try {
			return new PropertiesConfiguration(url);
		} catch (ConfigurationException e) {
			return null;
		}
	}

	/**
	 * Gets the username to be used to access the given domain if it requests a
	 * username.
	 * 
	 * @param domain
	 * @return
	 */
	public static String getUsername(String domain) {
		Configuration credentials = getCredentials(domain);
		return credentials != null ? credentials.getString("username") : null;
	}

	/**
	 * Gets the password to be used to access the given domain if it requests a
	 * password.
	 * 
	 * @param domain
	 * @return
	 */
	public static String getPassword(String domain) {
		Configuration credentials = getCredentials(domain);
		return credentials != null ? credentials.getString("password") : null;
	}

	public static URL[] getSUAsrvURLs() throws ConfigurationException,
			MalformedURLException {
		String[] urlStrings = getModeProperties().getStringArray("SUAsrv.url");
		List<URL> urls = new ArrayList<URL>();
		for (String urlString : urlStrings) {
			urls.add(new URL(urlString));
		}
		return urls.toArray(new URL[0]);
	}

	public static URL getDefaultSUAsrvURL() throws ConfigurationException,
			MalformedURLException {
		return getSUAsrvURLs()[0];
	}

	public static URI[] getSchemaRelativeSUAsrvURIs()
			throws ConfigurationException, MalformedURLException,
			URISyntaxException {
		return makeSchemaRelativeURIs(getSUAsrvURLs());
	}

	public static URL[] getSUAsrvURLs(String relativePath)
			throws MalformedURLException, ConfigurationException {
		List<URL> suasrvURLs = new ArrayList<URL>();
		for (URL suasrvURL : getSUAsrvURLs()) {
			suasrvURLs.add(new URL(suasrvURL + relativePath));
		}
		return suasrvURLs.toArray(new URL[0]);
	}

	public static URI[] getSchemaRelativeSUAsrvURIs(String relativePath)
			throws ConfigurationException, MalformedURLException,
			URISyntaxException {
		return makeSchemaRelativeURIs(getSUAsrvURLs(relativePath));
	}

	public static URL[] getSUAcltWebHostURLs() throws ConfigurationException,
			MalformedURLException {
		List<URL> urls = new ArrayList<URL>();
		for (String url : getModeProperties().getStringArray(
				"SUAclt.web.host.url")) {
			urls.add(new URL(url));
		}
		return urls.toArray(new URL[0]);
	}

	public static URI[] getSchemaRelativeSUAcltWebHostURIs()
			throws ConfigurationException, MalformedURLException,
			URISyntaxException {
		return makeSchemaRelativeURIs(getSUAcltWebHostURLs());
	}

	/**
	 * Removes the schema from the given {@link URL} and returns the result.
	 * 
	 * @param url
	 * @return
	 * @throws URISyntaxException
	 */
	public static URI makeSchemaRelativeURI(URL url) throws URISyntaxException {
		Pattern schemaPattern = Pattern.compile("^\\w*(:)");
		String schemaRelativeURL = schemaPattern.matcher(url.toExternalForm())
				.replaceFirst("");
		URI uri = new URI(schemaRelativeURL);
		return uri;
	}

	/**
	 * Removes the schema from each of the given {@link URL}s and returns the
	 * result.
	 * <p>
	 * The result can have less elements than the input since duplicates are
	 * automatically removed.
	 * 
	 * @param urls
	 * @return
	 * @throws URISyntaxException
	 */
	public static URI[] makeSchemaRelativeURIs(URL[] urls)
			throws URISyntaxException {
		List<URI> uris = new ArrayList<URI>();
		for (URL url : urls) {
			URI uri = makeSchemaRelativeURI(url);
			if (!uris.contains(uri))
				uris.add(uri);
		}
		return uris.toArray(new URI[0]);
	}

	/**
	 * Returns the base delay (in milliseconds) to wait before checking for
	 * state changes on the server will reliably occur after browsed a SUAclt
	 * host site.
	 * <p>
	 * e.g. Imagine you have opened a SUAclt host site. This will result in a
	 * log entry on the SUAsrv. The time that passes between those two events
	 * depends on the location of those servers (remote server need a higher
	 * delay than locally running servers).
	 * 
	 * @return
	 * @throws ConfigurationException
	 */
	public static int getBaseDelay() {
		try {
			return getModeProperties().getInt("SUAclt.web.baseDelay");
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return 999;
		}
	}

	/**
	 * Returns the maximum number of seconds to wait fluently.
	 * 
	 * @see {@link FluentWait}.
	 * 
	 * @return
	 * @throws ConfigurationException
	 */
	public static int maxWebDriverFluentWait() throws ConfigurationException {
		return 10;
	}
}
