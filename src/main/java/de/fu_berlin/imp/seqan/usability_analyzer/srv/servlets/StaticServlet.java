package de.fu_berlin.imp.seqan.usability_analyzer.srv.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Makes static content available.
 * <p>
 * The request <code>http://host/app/path/file.xml</code> will make this servlet
 * return the file <code>[webapps]/app/file.xml</code>.
 * <p>
 * In contrast to the {@link DefaultServlet} this servlet ignores the servlet
 * mapping's path portion. In the above mentioned example {@link DefaultServlet}
 * would return <code>[webapps]/app/path/file.xml</code>.
 * 
 * @author bkahlert
 * 
 */
public class StaticServlet extends HttpServlet {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(StaticServlet.class);
	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String requestedFile = request.getPathInfo();
		if (requestedFile == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}

		File file = new File(getServletContext().getRealPath(
				URLDecoder.decode(requestedFile, "UTF-8")));
		if (!file.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}

		String contentType = getServletContext().getMimeType(file.getName());
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setContentType(contentType);
		response.setHeader("Content-Length", String.valueOf(file.length()));
		// response.setHeader("Content-Disposition", "attachment; filename=\""
		// + file.getName() + "\"");

		BufferedInputStream input = null;
		BufferedOutputStream output = null;

		try {
			// Open streams.
			input = new BufferedInputStream(new FileInputStream(file),
					DEFAULT_BUFFER_SIZE);
			output = new BufferedOutputStream(response.getOutputStream(),
					DEFAULT_BUFFER_SIZE);

			// Write file contents to response.
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
		} finally {
			// Gently close streams.
			close(output);
			close(input);
		}
	}

	// Helpers (can be refactored to public utility class)
	// ----------------------------------------

	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				// Do your thing with the exception. Print it, log it or mail
				// it.
				e.printStackTrace();
			}
		}
	}

	// protected String getRelativePath(HttpServletRequest request) {
	// // e.g. https://host/app/path, whereas path is the prefix
	// int prefixLength = request.getServletPath().length();
	// String newRelativePath = super.getRelativePath(request).substring(
	// prefixLength);
	//
	// LOGGER.debug("Request " + request.getPathInfo() + " mapped to "
	// + newRelativePath);
	// return newRelativePath;
	// }

}
