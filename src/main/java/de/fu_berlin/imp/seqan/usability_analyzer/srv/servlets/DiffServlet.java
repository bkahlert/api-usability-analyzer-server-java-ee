package de.fu_berlin.imp.seqan.usability_analyzer.srv.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlets.DefaultServlet;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;

public class DiffServlet extends DefaultServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(DiffServlet.class);

	private File getDiffLocation() {
		File diffLocation = new File(getServletContext().getInitParameter(
				"diffLocation"));
		diffLocation.mkdirs();
		return diffLocation;
	}

	private File getDiffLocation(ID id) {
		File diffLocation = new File(getDiffLocation(), id.toString());
		diffLocation.mkdirs();
		return diffLocation;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		String prefix = request.getContextPath() + request.getServletPath();
		String[] path = request.getRequestURI().substring(prefix.length())
				.split("/");
		if (path.length < 2 || !ID.isValid(path[1])) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			LOGGER.info("Invalid GET request for " + ID.class.getSimpleName()
					+ " \"" + ((path.length >= 2) ? path[1] : "null") + "\".");
			return;
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain; charset=UTF-8");

		ID id = new ID(path[1]);
		File diffLocation = getDiffLocation(id);
		if (!diffLocation.isDirectory()) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			LOGGER.fatal("Diff directory \"" + diffLocation
					+ "\" is no directory.");
			return;
		}
		if (!diffLocation.canRead()) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			LOGGER.fatal("Diff directory \"" + diffLocation
					+ "\" can't be accessed.");
			return;
		}

		ServletOutputStream out = response.getOutputStream();
		for (File file : diffLocation.listFiles()) {
			if (!file.isFile())
				continue;
			out.print(file.getName() + "\t" + file.length() + "\n");
		}
		out.close();
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			doGet(request, response);
			return;
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
					if (!ID.isValid(fieldName)) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
						LOGGER.error("Diff upload was of invalid "
								+ ID.class.getSimpleName() + " \"" + fieldName
								+ "\". " + fileName);
						return;
					}

					ID id = new ID(fieldName);
					File diffDest = new File(getDiffLocation(id),
							fileName.getName());
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
			LOGGER.fatal("Diff upload did not succeed", e);
			return;
		}
	}

}
