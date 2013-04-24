package de.fu_berlin.imp.seqan.usability_analyzer.srv.integration.SUAclt.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.TestConfiguration;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.Utils;

@RunWith(value = Parameterized.class)
public class DiffClientTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(DiffClientTest.class);

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() throws Exception {

		// TODO
		// System.setProperty("config", "system_test");

		List<Object[]> parameters = new ArrayList<Object[]>();
		for (URL diffUrl : TestConfiguration.getSUAsrvURLs("/diff")) {
			parameters.add(new Object[] { diffUrl });
		}
		return parameters;
	}

	private static class FileDescriptor {
		private String name;
		private long size;
		private String md5;

		public FileDescriptor(String name, long size, String md5) {
			super();
			this.name = name;
			this.size = size;
			this.md5 = md5;
		}

		@Override
		public String toString() {
			return FileDescriptor.class.getSimpleName() + "(" + name + ", "
					+ size + ", " + md5 + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((md5 == null) ? 0 : md5.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + (int) (size ^ (size >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FileDescriptor other = (FileDescriptor) obj;
			if (md5 == null) {
				if (other.md5 != null)
					return false;
			} else if (!md5.equals(other.md5))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (size != other.size)
				return false;
			return true;
		}

	}

	private URL diffUrl;

	public DiffClientTest(URL diffUrl) {
		this.diffUrl = diffUrl;
	}

	private URL getHomeUrl(IIdentifier identifier) throws MalformedURLException {
		if (identifier == null)
			throw new IllegalArgumentException();
		return new URL(this.diffUrl + "/" + identifier.toString());
	}

	private FileDescriptor[] getFileDescriptors(IIdentifier identifier)
			throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) getHomeUrl(
				identifier).openConnection();
		assertEquals(200, connection.getResponseCode());
		List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
		assertEquals("text/plain;charset=UTF-8", connection.getContentType());
		for (String line : IOUtils.toString(
				(InputStream) connection.getContent()).split("\n")) {
			if (line.isEmpty())
				continue;
			String[] lineParts = line.split("\t");
			assertEquals(3, lineParts.length);
			fileDescriptors.add(new FileDescriptor(lineParts[0], new Long(
					lineParts[1]), lineParts[2]));
		}
		return fileDescriptors.toArray(new FileDescriptor[0]);
	}

	private void postFile(IIdentifier identifier, File file, String mimeType)
			throws MalformedURLException, IOException {
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
				HttpVersion.HTTP_1_1);

		HttpPost post = new HttpPost(getHomeUrl(identifier).toString());
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		// For File parameters
		entity.addPart(Utils.getRandomString(8), new FileBody((file), mimeType));

		post.setEntity(entity);

		// Here we go!
		String response = EntityUtils.toString(
				client.execute(post).getEntity(), "UTF-8");
		assertEquals("", response);

		file.delete();

		client.getConnectionManager().shutdown();
	}

	private FileDescriptor postTextFile(IIdentifier identifier, String name,
			int size) throws IOException {
		File textFile = temporaryFolder.newFile(name);
		String textFileContent = Utils.getRandomString(size);
		FileUtils.write(textFile, textFileContent);
		postFile(identifier, textFile, "text/plain");
		return new FileDescriptor(name, size,
				DigestUtils.md5Hex(textFileContent));
	}

	private FileDescriptor postZipFile(IIdentifier identifier, String name)
			throws IOException {
		File zipFile = temporaryFolder.newFile(name);
		FileUtils
				.copyURLToFile(DiffClientTest.class.getResource(name), zipFile);
		byte[] zipFileContent = FileUtils.readFileToByteArray(zipFile);
		postFile(identifier, zipFile, "application/zip");
		return new FileDescriptor(name, zipFileContent.length,
				DigestUtils.md5Hex(zipFileContent));
	}

	private boolean isSame(FileDescriptor[] files1, FileDescriptor[] files2) {
		Set<FileDescriptor> set1 = new HashSet<FileDescriptor>(
				Arrays.asList(files1));
		Set<FileDescriptor> set2 = new HashSet<FileDescriptor>(
				Arrays.asList(files2));
		return set1.equals(set2);
	}

	@Test
	public void forbiddenIfNoIdentifierSupplied() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) this.diffUrl
				.openConnection();
		assertEquals(403, connection.getResponseCode());
	}

	@Test
	public void unsupportedIfFingerprintSupplied() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) getHomeUrl(
				Utils.getTestFingerprint()).openConnection();
		assertEquals(400, connection.getResponseCode());
	}

	@Test
	public void allowedIfIdSupplied() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) getHomeUrl(
				Utils.getTestID()).openConnection();
		assertEquals(200, connection.getResponseCode());
	}

	@Test
	public void checkIfTestSiteSendsData() throws Exception {
		ID id = Utils.getTestID();

		assertTrue(isSame(new FileDescriptor[] {}, getFileDescriptors(id)));

		// text file
		FileDescriptor textFile = postTextFile(id, "textFile.doclog",
				1024 * 1024 * 2);
		assertTrue(isSame(new FileDescriptor[] { textFile },
				getFileDescriptors(id)));

		// zip file
		FileDescriptor zipFile = postZipFile(id, "h5bp-html5-boilerplate.zip");
		assertTrue(isSame(new FileDescriptor[] { textFile, zipFile },
				getFileDescriptors(id)));

		// overwrite file
		FileDescriptor textFile2 = postTextFile(id, "textFile.doclog", 5374);
		assertTrue(isSame(new FileDescriptor[] { textFile2, zipFile },
				getFileDescriptors(id)));
	}
}
