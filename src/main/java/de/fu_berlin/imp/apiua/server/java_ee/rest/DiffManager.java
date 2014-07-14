package de.fu_berlin.imp.apiua.server.java_ee.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.apiua.server.java_ee.model.ID;

// TODO public API provided by DiffServlet; make this the public API (needs APIUAclt.js to be adapted)
// TODO client testen
@Path("/diff")
public class DiffManager {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(DiffManager.class);

	public static class DiffManagerException extends Exception {
		private static final long serialVersionUID = -6155306816795534666L;

		public DiffManagerException(String message) {
			super(message);
		}
	}

	private static File getDiffLocation(ServletContext context) {
		File diffLocation = new File(context.getInitParameter("diffLocation"));
		diffLocation.mkdirs();
		return diffLocation;
	}

	public static File getDiffLocation(ServletContext context, ID id) {
		File diffLocation = new File(getDiffLocation(context), id.toString());
		diffLocation.mkdirs();
		return diffLocation;
	}

	public static ID[] getIDs(ServletContext context) {
		List<ID> ids = new ArrayList<ID>();
		File diffLocation = getDiffLocation(context);
		for (File file : diffLocation.listFiles()) {
			if (ID.isValid(file.getName())) {
				ids.add(new ID(file.getName()));
			}
		}
		return ids.toArray(new ID[0]);
	}

	public static List<File> listFiles(ServletContext context, ID id)
			throws DiffManagerException {
		File diffLocation = DiffManager.getDiffLocation(context, id);
		if (!diffLocation.isDirectory()) {
			throw new DiffManagerException("Diff directory \"" + diffLocation
					+ "\" is no directory.");
		}
		if (!diffLocation.canRead()) {
			throw new DiffManagerException("Diff directory \"" + diffLocation
					+ "\" can't be accessed.");
		}

		List<File> files = new ArrayList<File>();
		for (File file : diffLocation.listFiles()) {
			if (!file.isFile())
				continue;
			files.add(file);
		}
		return files;
	}

}