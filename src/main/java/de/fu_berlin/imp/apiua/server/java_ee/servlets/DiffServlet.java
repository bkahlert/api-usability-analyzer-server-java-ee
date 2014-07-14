package de.fu_berlin.imp.apiua.server.java_ee.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.apiua.server.java_ee.model.ID;
import de.fu_berlin.imp.apiua.server.java_ee.rest.DiffManager;
import de.fu_berlin.imp.apiua.server.java_ee.rest.DiffManager.DiffManagerException;

public class DiffServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(DiffServlet.class);

	private String[] getPath(HttpServletRequest request) {
		String prefix = request.getContextPath() + request.getServletPath();
		return request.getRequestURI().substring(prefix.length()).split("/");
	}

	private String calculateMd5(File file) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			String md5 = DigestUtils.md5Hex(fis);
			return md5;
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	/**
	 * Checks the request for a valid {@link ID} and returns a list of all
	 * stored files for this {@link ID} and their file sizes.
	 * <p>
	 * The {@link URL} is of the format http://host/webapp/diff/username.<br/>
	 * e.g. <code>http://dalak.imp.fu-berlin.de/APIUAsrv/diff/bkahlert</code>
	 * <p>
	 * If no valid {@link ID} is provided error 403 is returned.
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		String[] path = getPath(request);
		if (path.length < 2) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			LOGGER.info("Invalid GET request for " + ID.class.getSimpleName()
					+ " "
					+ ((path.length >= 2) ? "\"" + path[1] + "\"" : "null")
					+ ".");
			return;
		} else if (!ID.isValid(path[1])) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No valid "
					+ ID.class + " provided");
			LOGGER.info("Invalid GET request for " + ID.class.getSimpleName()
					+ " \"" + ((path.length >= 2) ? path[1] : "null") + "\".");
			return;
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain; charset=UTF-8");

		ID id = new ID(path[1]);

		ServletOutputStream out = response.getOutputStream();
		try {
			for (File file : DiffManager.listFiles(getServletContext(), id)) {
				String md5 = "ERROR";
				try {
					md5 = calculateMd5(file);
				} catch (IOException e) {
					LOGGER.error(
							"Error calculating MD5 for "
									+ file.getAbsolutePath(), e);
				}
				out.print(file.getName() + "\t" + file.length() + "\t" + md5
						+ "\n");
			}
		} catch (DiffManagerException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			LOGGER.fatal(e);
			return;
		} finally {
			out.close();
		}
	}

	/**
	 * Checks the request for a valid {@link ID} and puts the specified
	 * {@link File}s to the {@link ID}'s diff directory.
	 * <p>
	 * If no file is provided a file list as is described in
	 * {@link #doGet(HttpServletRequest, HttpServletResponse)} is returned.
	 * <p>
	 * The {@link URL} is of the format http://host/webapp/diff/username.<br/>
	 * e.g. <code>http://dalak.imp.fu-berlin.de/APIUAsrv/diff/bkahlert</code>
	 * <p>
	 * If no valid {@link ID} is provided error 403 is returned.
	 * <p>
	 * <strong>It is strongly recommended to first request the stored files
	 * first using {@link #doGet(HttpServletRequest, HttpServletResponse)}. This
	 * way you can make sure to upload all missing files.</strong>
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			doGet(request, response);
			return;
		}

		ID id = null;
		String[] path = getPath(request);
		if (path.length >= 2 && ID.isValid(path[1])) {
			id = new ID(path[1]);
		}

		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024 * 1024); // 1MB

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		try {
			List<?> items = upload.parseRequest(request);
			Iterator<?> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();

				if (item.isFormField()) {
					System.err.println(item);
				} else {
					String fieldName = item.getFieldName();
					File fileName = new File(item.getName().replace('\\', '/'));
					if (id == null) {
						if (ID.isValid(fieldName)) {
							id = new ID(fieldName);
						} else {
							response.sendError(HttpServletResponse.SC_FORBIDDEN);
							LOGGER.error("Diff upload was of invalid "
									+ ID.class.getSimpleName() + " \""
									+ fieldName + "\". " + fileName);
							return;
						}
					}

					File diffDest = new File(DiffManager.getDiffLocation(
							getServletContext(), id), fileName.getName());
					try {
						item.write(diffDest);
					} catch (Exception e) {
						diffDest.delete();
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						LOGGER.fatal("Diff upload with "
								+ ID.class.getSimpleName() + "\"" + id
								+ "\" could not be saved "
								+ ID.class.getSimpleName() + " \"" + fieldName
								+ "\". " + fileName);
						return;
					} finally {
						item.delete();
					}
				}
			}
		} catch (FileUploadException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			LOGGER.fatal(
					"Diff upload did not succeed for "
							+ ID.class.getSimpleName() + " " + id, e);
			return;
		}
	}

}
