package de.fu_berlin.imp.apiua.server.java_ee.utils;

import java.io.File;

import javax.servlet.ServletContext;

import de.fu_berlin.imp.apiua.server.java_ee.persistence.DoclogFile;
import de.fu_berlin.imp.apiua.server.java_ee.persistence.MappingDoclogPersistence;

/**
 * Returns all relevant locations in where to operate.
 * 
 * @author bkahlert
 * 
 */
public class LocationUtils {

	/**
	 * Returns the directory in where to save {@link DoclogFile}s.
	 * 
	 * @param context
	 * @return
	 */
	public static File getDoclogLocation(ServletContext context) {
		return new File(context.getInitParameter("doclogLocation"));
	}

	/**
	 * Returns the {@link MappingDoclogPersistence} responsible for the given
	 * {@link ServletContext}.
	 * 
	 * @param context
	 * @return
	 */
	public static MappingDoclogPersistence getDoclogPersistence(
			ServletContext context) {
		File dir = getDoclogLocation(context);
		if (!dir.exists())
			dir.mkdirs();
		return new MappingDoclogPersistence(dir);
	}

	private LocationUtils() {

	}

}
