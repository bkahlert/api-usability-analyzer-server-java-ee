package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.testsite;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.TestConfiguration;

class TestSiteContainer implements Container {
	private static final Logger LOGGER = Logger
			.getLogger(TestSiteContainer.class);

	public static File getFile(Class<?> clazz, String clazzRelativePath) {
		clazzRelativePath = "html"
				+ (clazzRelativePath.startsWith("/") ? "" : "/")
				+ clazzRelativePath;
		URL url = clazz.getResource(clazzRelativePath);
		if (url == null)
			return null;
		return new File(url.toString().replace("file:", ""));
	}

	public void handle(Request request, Response response) {
		try {
			if (!request.getMethod().equals("GET")) {
				Assert.fail("Requests other than GET are not expected!");
			}

			String relPath = request.getAddress().getPath().getPath();
			if (relPath.equals("/"))
				relPath = "index.html";

			File absPath = getFile(this.getClass(), relPath);
			if (absPath != null) {
				PrintStream body = response.getPrintStream();
				long time = System.currentTimeMillis();

				response.setValue("Server", this.getClass().getSimpleName()
						+ "/1.0 (Simple 4.0)");
				response.setDate("Date", time);
				response.setDate("Last-Modified", time);

				String ext = FilenameUtils.getExtension(absPath.toString());
				String mimeType = null;
				if (ext.equals("html")) {
					mimeType = "text/html";
				} else if (ext.equals("css")) {
					mimeType = "text/css";
				} else if (ext.equals("js")) {
					mimeType = "application/javascript";
				} else if (ext.equals("png")) {
					mimeType = "image/png";
				} else {
					Assert.fail("Unknown mime type for " + relPath);
				}
				response.setValue("Content-Type", mimeType);
				String content = FileUtils.readFileToString(absPath);

				/*
				 * make SUAclt include code point to the correct server; since
				 * we want to use protocol-relative URLs we remove an eventually
				 * specified protocol
				 */
				URI[] suaSrvUris = TestConfiguration
						.getSchemaRelativeSUAsrvURIs();
				Assert.assertTrue(
						"You must at least configure one SUAsrv to be tested",
						suaSrvUris.length > 0);
				if (suaSrvUris.length > 1) {
					LOGGER.fatal("There is more than one SUAsrv to be tested. Only testing the first the following SUAsrvs:\n"
							+ StringUtils.join(suaSrvUris, "\n"));
				}
				content = content.replace("${SUAsrv.url}",
						suaSrvUris[0].toString());

				body.println(content);
				body.close();
			} else {
				response.setStatus(Status.NOT_FOUND);
			}
		} catch (Exception e) {
			Assert.fail("Failed to serve file " + request.getTarget() + ". "
					+ e.getMessage());
		}
	}
}