package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class UrlUtils {

	/**
	 * Checks if the given URL needs authentication and returns a new URL that
	 * may contain authentication information.
	 * 
	 * @param url
	 * @param method
	 * @return
	 * @throws IOException
	 */
	public static URL getAuthUrl(URL url, String method) throws IOException {
		String authUrl = url.toExternalForm();
		if (UrlUtils.needsAuthentication(url, method)
				&& url.getUserInfo() == null) {
			String username = TestConfiguration.getUsername(url.getHost());
			String password = TestConfiguration.getPassword(url.getHost());
			if (username != null || password != null) {
				String userInfo = (username != null ? username : "") + ":"
						+ (password != null ? password : "");
				authUrl = UrlUtils.addUserInfo(url, userInfo).toExternalForm();
			}
		}
		return new URL(authUrl);
	}

	public static URL addUserInfo(URL url, String userInfo)
			throws MalformedURLException {
		StringBuilder sb = new StringBuilder();
		if (url.getProtocol() != null)
			sb.append(url.getProtocol() + "://");

		if (userInfo != null) {
			String[] parts = userInfo.split(":");
			for (int i = 0; i < parts.length; i++) {
				try {
					parts[i] = URLEncoder.encode(parts[i], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			sb.append(StringUtils.join(parts, ":") + "@");
		}

		if (url.getHost() != null)
			sb.append(url.getHost());
		if (url.getPort() != -1)
			sb.append(":" + url.getPort());
		if (url.getPath() != null)
			sb.append(url.getPath());
		if (url.getQuery() != null)
			sb.append("?" + url.getQuery());
		if (url.getRef() != null)
			sb.append("#" + url.getRef());
		return new URL(sb.toString());
	}

	@Test
	public void testAddUserInfo() throws Exception {
		assertEquals(new URL("http://test@bkahlert.com"),
				addUserInfo(new URL("http://bkahlert.com"), "test"));

		assertEquals(new URL("http://test@bkahlert.com:80"),
				addUserInfo(new URL("http://bkahlert.com:80"), "test"));

		assertEquals(new URL("https://test@bkahlert.com"),
				addUserInfo(new URL("https://bkahlert.com"), "test"));

		assertEquals(new URL("https://test@bkahlert.com:4488"),
				addUserInfo(new URL("https://bkahlert.com:4488"), "test"));

		assertEquals(
				new URL("https://test:pass%2Ford@bkahlert.com:4488"),
				addUserInfo(new URL("https://bkahlert.com:4488"),
						"test:pass/ord"));

		assertEquals(
				new URL(
						"http://test:xyz@www.bkahlert.com:80/page.html?key=value&key2=value2#fragement"),
				addUserInfo(
						new URL(
								"http://www.bkahlert.com:80/page.html?key=value&key2=value2#fragement"),
						"test:xyz"));

	}

	public static String removeUserInfo(String url)
			throws MalformedURLException {
		return addUserInfo(new URL(url), null).toExternalForm();
	}

	@Test
	public void testRemoveUserInfo() throws Exception {
		assertEquals("http://bkahlert.com",
				removeUserInfo("http://test@bkahlert.com"));

		assertEquals("http://bkahlert.com:80",
				removeUserInfo("http://test@bkahlert.com:80"));

		assertEquals("https://bkahlert.com",
				removeUserInfo("https://test@bkahlert.com"));

		assertEquals("https://bkahlert.com:4488",
				removeUserInfo("https://test@bkahlert.com:4488"));

		assertEquals("https://bkahlert.com:4488",
				removeUserInfo("https://test:pass%2Ford@bkahlert.com:4488"));

		assertEquals(
				"http://www.bkahlert.com:80/page.html?key=value&key2=value2#fragement",
				removeUserInfo("http://test:xyz@www.bkahlert.com:80/page.html?key=value&key2=value2#fragement"));

	}

	public static boolean referencesSamePage(String url, String destinationUrl) {
		if (url.equals(destinationUrl))
			return true;
		if (!destinationUrl.contains("#"))
			return false;
		if (destinationUrl.startsWith("#"))
			return true;
		int hashPos = destinationUrl.indexOf("#");
		String destinationUrlWithoutFragement = destinationUrl.substring(0,
				hashPos);
		return url.equals(destinationUrlWithoutFragement);
	}

	@Test
	public void testReferencesSamePage() throws Exception {
		assertTrue(referencesSamePage("http://bkahlert.com",
				"http://bkahlert.com#fragment"));
		assertTrue(referencesSamePage("http://bkahlert.com", "#fragment"));
		assertFalse(referencesSamePage("http://bkahlert.com",
				"http://bkahlert.com/"));
		assertFalse(referencesSamePage("http://bkahlert.com",
				"http://google.com"));
	}

	/**
	 * Returns true if an authentication is needed if the given {@link URL} is
	 * accessed with the given method.
	 * 
	 * @param url
	 * @param method
	 * @return
	 * @throws IOException
	 */
	public static boolean needsAuthentication(URL url, String method)
			throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		int responseCode = connection.getResponseCode();
		return responseCode == 401;
	}

}
