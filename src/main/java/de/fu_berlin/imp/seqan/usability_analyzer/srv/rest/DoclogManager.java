package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogKeyMap;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.FinterprintAlreadyMappedException;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Rectangle;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.adapters.DateTimeAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.JAXBUtils;

@Path("/doclog")
public class DoclogManager {

	private static final Logger LOGGER = Logger.getLogger(DoclogManager.class);

	private static ConcurrentMap<File, ReentrantReadWriteLock> LOCKS = new ConcurrentHashMap<File, ReentrantReadWriteLock>();

	private static ReentrantReadWriteLock getLock(File file) {
		LOCKS.putIfAbsent(file, new ReentrantReadWriteLock());
		return LOCKS.get(file);
	}

	static ReadLock getReadLock(File file) {
		return getLock(file).readLock();
	}

	static WriteLock getWriteLock(File file) {
		return getLock(file).writeLock();
	}

	private static File getDoclogLocation(@Context ServletContext context)
			throws IOException {
		if (context != null) {
			File file = new File(context.getInitParameter("doclogLocation"));
			if (!file.exists())
				file.mkdirs();
			if (!file.isDirectory())
				throw new IOException(file.getAbsolutePath()
						+ " is no directory");
			if (!file.canRead())
				throw new IOException("Can't read + " + file.getAbsolutePath());
			return file;
		}
		return null;
	}

	static File getDoclogLocation(@Context ServletContext context, Object key)
			throws IOException {
		if (!(key instanceof ID) && !(key instanceof Fingerprint))
			throw new InvalidParameterException();
		return new File(getDoclogLocation(context), key.toString() + ".doclog");
	}

	private static DoclogKeyMap doclogKeyMap = null;

	private static DoclogKeyMap getDoclogKeyMap(ServletContext context)
			throws IOException {
		if (doclogKeyMap == null) {
			try {
				doclogKeyMap = DoclogKeyMap.load(new File(
						getDoclogLocation(context), "mapping.xml"));
			} catch (FileNotFoundException e) {
				doclogKeyMap = new DoclogKeyMap();
			}
		}
		return doclogKeyMap;
	}

	static private void saveDoclogKeyMap(ServletContext context)
			throws JAXBException, IOException {
		if (doclogKeyMap != null) {
			doclogKeyMap.save(new File(getDoclogLocation(context),
					"mapping.xml"));
		}
	}

	static void addMapping(ServletContext context, Fingerprint fingerprint,
			ID id) throws JAXBException, IOException,
			FinterprintAlreadyMappedException {
		getDoclogKeyMap(context).associate(fingerprint, id);
		saveDoclogKeyMap(context);
	}

	static ID getMapping(ServletContext context, Fingerprint fingerprint)
			throws JAXBException, IOException {
		return getDoclogKeyMap(context).getID(fingerprint);
	}

	@GET
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public static List<Doclog> getDoclogs(@Context ServletContext context)
			throws IOException {
		File doclogLocation = getDoclogLocation(context);
		List<Doclog> doclogs = new ArrayList<Doclog>();
		for (String doclogName : doclogLocation.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".doclog");
			}
		})) {
			File doclogFile = new File(doclogLocation, doclogName);
			getReadLock(doclogFile).lock();
			Doclog doclog = new Doclog(Doclog.getKey(doclogFile));
			getReadLock(doclogFile).unlock();
			doclogs.add(doclog);
		}
		return doclogs;
	}

	/*
	 * FIXME: This method respect the added /full since the same url needs to
	 * handle the @GET createDoclogRecord method. Explicitly setting the accept
	 * header of ajax requests is not working on all browsers.
	 */
	@GET
	@Path("{key}/test")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public static Doclog getDoclog(@Context ServletContext context,
			@PathParam("key") String key) throws IOException, JAXBException {
		Object typedKey = ID.isValid(key) ? new ID(key) : new Fingerprint(key);
		return getDoclog(context, typedKey);
	}

	public static Doclog getDoclog(ServletContext context,
			@PathParam("key") Object typedKey) throws IOException,
			JAXBException {
		if (typedKey instanceof Fingerprint) {
			File fingerprintDoclogFile = getDoclogLocation(context, typedKey);

			ID mappedID = getMapping(context, (Fingerprint) typedKey);
			if (mappedID != null) {
				File idFileDoclog = getDoclogLocation(context, mappedID);

				if (fingerprintDoclogFile.exists()) {
					mergeDoclogs(context, fingerprintDoclogFile, idFileDoclog);
					typedKey = mappedID;
				} else {
					if (idFileDoclog.exists()) {
						typedKey = mappedID;
					} else {
						return null;
					}
				}
			}
		}

		File doclogFile = getDoclogLocation(context, typedKey);
		try {
			getReadLock(doclogFile).lock();
			return JAXBUtils.unmarshall(Doclog.class, doclogFile);
		} catch (FileNotFoundException e) {
			return null;
		} finally {
			getReadLock(doclogFile).unlock();
		}
	}

	/**
	 * Creates a {@link DoclogRecord}.
	 * <p>
	 * Since cross-domains are not supported by all browsers we also accept the
	 * {@link DoclogRecord} creation by GET calls. TODO: TEST TEST TEST!
	 * 
	 * @param context
	 * @param request
	 * @param key
	 * @param id
	 * @param url
	 * @param ip
	 *            (optional)
	 * @param proxyIp
	 *            (optional)
	 * @param event
	 * @param action
	 *            (optional)
	 * @param actionParameter
	 *            (optional)
	 * @param dateTime
	 * @param boundsX
	 * @param boundsY
	 * @param boundsWidth
	 * @param boundsHeight
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	@GET
	@Path("{key}")
	@Produces({ "text/javascript" })
	public String createDoclogRecordGET(@Context ServletContext context,
			@Context HttpServletRequest request, @PathParam("key") String key,
			@QueryParam("id") final ID id, @QueryParam("url") final String url,
			@QueryParam("ip") String ip, @QueryParam("proxyIp") String proxyIp,
			@QueryParam("event") final String event,
			@QueryParam("action") DoclogAction action,
			@QueryParam("actionParameter") String actionParameter,
			@QueryParam("dateTime") final String dateTime,
			@QueryParam("bounds.x") final Integer boundsX,
			@QueryParam("bounds.y") final Integer boundsY,
			@QueryParam("bounds.width") final Integer boundsWidth,
			@QueryParam("bounds.height") final Integer boundsHeight)
			throws IOException, JAXBException {
		DoclogRecord doclogRecord = createDoclogRecord(context, request, key,
				id, url, ip, proxyIp, event, action, actionParameter, dateTime,
				boundsX, boundsY, boundsWidth, boundsHeight);
		if (doclogRecord != null)
			return "true;";
		else
			return "false;";
	}

	/**
	 * Creates a {@link DoclogRecord}
	 * 
	 * @param context
	 * @param request
	 * @param key
	 * @param id
	 * @param url
	 * @param ip
	 *            (optional)
	 * @param proxyIp
	 *            (optional)
	 * @param event
	 * @param action
	 *            (optional)
	 * @param actionParameter
	 *            (optional)
	 * @param dateTime
	 * @param boundsX
	 * @param boundsY
	 * @param boundsWidth
	 * @param boundsHeight
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	@POST
	@Path("{key}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public DoclogRecord createDoclogRecord(@Context ServletContext context,
			@Context HttpServletRequest request, @PathParam("key") String key,
			@QueryParam("id") ID id, @QueryParam("url") final String url,
			@QueryParam("ip") String ip, @QueryParam("proxyIp") String proxyIp,
			@QueryParam("event") final String event,
			@QueryParam("action") DoclogAction action,
			@QueryParam("actionParameter") String actionParameter,
			@QueryParam("dateTime") final String dateTime,
			@QueryParam("bounds.x") final Integer boundsX,
			@QueryParam("bounds.y") final Integer boundsY,
			@QueryParam("bounds.width") final Integer boundsWidth,
			@QueryParam("bounds.height") final Integer boundsHeight)
			throws IOException {
		if (key == null || url == null || (event == null && action == null)
				|| dateTime == null || boundsX == null || boundsY == null
				|| boundsWidth == null || boundsHeight == null) {
			LOGGER.error("Missing parameter on " + Doclog.class.getSimpleName()
					+ " creation:\n\tid=" + id + "\n\turl=" + url
					+ "\n\tevent=" + event + "\n\taction=" + action
					+ "\n\tdateTime=" + dateTime + "\n\tbounds.x=" + boundsX
					+ "\n\tbounds.y=" + boundsY + "\n\tbounds.width="
					+ boundsWidth + "\n\tbounds.height=" + boundsHeight);
			return null;
		}
		Object typedKey = ID.isValid(key) ? new ID(key) : new Fingerprint(key);
		LOGGER.info("Create " + Doclog.class.getSimpleName() + " " + typedKey
				+ ((id != null) ? " (ID: " + id + ")" : ""));

		if (ip == null || proxyIp == null) {
			if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
				if (ip == null)
					ip = request.getRemoteAddr();
				if (proxyIp == null)
					proxyIp = null;
			} else {
				if (ip == null)
					ip = request.getHeader("HTTP_X_FORWARDED_FOR");
				if (proxyIp == null)
					proxyIp = request.getRemoteAddr();
			}
		}

		if (event != null) {
			String[] action_ = event.split("-", 2);
			if (action == null)
				try {
					action = DoclogAction.valueOf(action_[0].toUpperCase());
				} catch (IllegalArgumentException e) {
					LOGGER.error(
							"Unknown " + DoclogAction.class.getSimpleName()
									+ " " + action_[0] + " provided", e);
					action = DoclogAction.UNKNOWN;
				}
			if (actionParameter == null)
				actionParameter = action_.length > 1 && !action_[1].isEmpty() ? action_[1]
						: null;
		}

		DoclogRecord doclogRecord = new DoclogRecord();
		doclogRecord.setUrl(url);
		doclogRecord.setIp(ip);
		doclogRecord.setProxyIp(proxyIp);
		doclogRecord.setAction(action);
		doclogRecord.setActionParameter(actionParameter);
		doclogRecord.setDateTime(new DateTimeAdapter().unmarshal(dateTime));
		doclogRecord.setBounds(new Rectangle(boundsX, boundsY, boundsWidth,
				boundsHeight));

		Fingerprint fingerprint = (typedKey instanceof Fingerprint) ? (Fingerprint) typedKey
				: null;
		if (typedKey instanceof ID)
			id = (ID) typedKey;

		synchronized (DoclogManager.class) {
			if (id == null) {
				try {
					id = getMapping(context, fingerprint);
				} catch (JAXBException e1) {
					LOGGER.fatal("Can't read "
							+ Fingerprint.class.getSimpleName() + "-"
							+ ID.class.getSimpleName() + " mappings");
				}
			}

			if (fingerprint != null) {
				LOGGER.debug("Locking " + fingerprint);
				getWriteLock(getDoclogLocation(context, fingerprint)).lock();
				LOGGER.debug("Locked " + fingerprint);
			}
			if (id != null) {
				LOGGER.debug("Locking " + id);
				getWriteLock(getDoclogLocation(context, id)).lock();
				LOGGER.debug("Locked " + id);
			}
		}

		if (fingerprint != null && id != null) {
			try {
				addMapping(context, (Fingerprint) typedKey, id);
			} catch (FinterprintAlreadyMappedException e) {
				LOGGER.fatal("One " + Fingerprint.class.getSimpleName()
						+ " pointing to multiple " + ID.class.getSimpleName()
						+ "s detected!", e);
				if (fingerprint != null) {
					LOGGER.debug("Unlocking " + fingerprint);
					getWriteLock(getDoclogLocation(context, fingerprint))
							.unlock();
					LOGGER.debug("Unlocked " + fingerprint);
				}
				if (id != null) {
					LOGGER.debug("Unlocking " + id);
					getWriteLock(getDoclogLocation(context, id)).unlock();
					LOGGER.debug("Unlocked " + id);
				}
				return null;
			} catch (JAXBException e) {
				LOGGER.error("Can't persists mapping for " + typedKey + " -> "
						+ id, e);
			}
			try {
				mergeDoclogs(context, typedKey, id);
				typedKey = id;
			} catch (JAXBException e) {
				LOGGER.error("Error merging " + Doclog.class.getSimpleName()
						+ "s.\nFingerprint: " + typedKey + "\nID: " + id, e);
			}
		}

		Doclog doclog = null;
		try {
			doclog = getDoclog(context, typedKey);
		} catch (JAXBException e) {
			LOGGER.warn("Creating backup of " + Doclog.class.getSimpleName()
					+ " for " + typedKey);
			createBackup(context, typedKey);
		}
		if (doclog == null)
			doclog = new Doclog(typedKey);
		doclog.add(doclogRecord);
		File file = getDoclogLocation(context, typedKey);
		try {
			JAXBUtils.marshall(doclog, file);
			LOGGER.info("Created " + Doclog.class.getSimpleName() + " "
					+ typedKey);
			return doclogRecord;
		} catch (Exception e) {
			LOGGER.error(doclog + " could not be written to " + file);
		} finally {
			if (fingerprint != null) {
				LOGGER.debug("Unlocking " + fingerprint);
				getWriteLock(getDoclogLocation(context, fingerprint)).unlock();
				LOGGER.debug("Unlocked " + fingerprint);
			}
			if (id != null) {
				LOGGER.debug("Unlocking " + id);
				getWriteLock(getDoclogLocation(context, id)).unlock();
				LOGGER.debug("Unlocked " + id);
			}
		}
		return null;
	}

	private void createBackup(ServletContext context, Object typedKey)
			throws IOException {
		File location;
		try {
			if (typedKey instanceof Fingerprint
					&& getMapping(context, (Fingerprint) typedKey) != null) {
				location = getDoclogLocation(context,
						getMapping(context, (Fingerprint) typedKey));
			} else {
				location = getDoclogLocation(context, typedKey);
			}
		} catch (JAXBException e1) {
			LOGGER.fatal("Error getting " + Fingerprint.class.getSimpleName()
					+ " -> " + ID.class.getSimpleName() + " mapping", e1);
			return;
		}

		File backupLocation = null;
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			backupLocation = new File(location.getAbsolutePath() + "." + i);
			if (!backupLocation.exists())
				break;
		}
		if (backupLocation == null) {
			String err = "Could not calculate backup location for " + location;
			LOGGER.error(err);
			throw new RuntimeException(err);
		}
		try {
			getWriteLock(location).lock();
			getWriteLock(backupLocation).lock();
			FileUtils.moveFile(location, backupLocation);
			LOGGER.warn("Since " + Doclog.class.getSimpleName() + " "
					+ location + " could not be read and was moved to "
					+ backupLocation);
		} finally {
			getWriteLock(backupLocation).unlock();
			getWriteLock(location).unlock();
		}
	}

	static void mergeDoclogs(ServletContext context, Object typedKey1,
			Object typedKey2) throws IOException, JAXBException {
		mergeDoclogs(context, getDoclogLocation(context, typedKey1),
				getDoclogLocation(context, typedKey2));
	}

	private static void mergeDoclogs(ServletContext context, File srcFile,
			File destFile) throws JAXBException, IOException {
		if (!srcFile.isFile())
			return;
		try {
			getWriteLock(srcFile).lock();
			getWriteLock(destFile).lock();
			Doclog srcDoclog = JAXBUtils.unmarshall(Doclog.class, srcFile);
			Doclog destDoclog = destFile.isFile() ? JAXBUtils.unmarshall(
					Doclog.class, destFile) : new Doclog(
					Doclog.getKey(destFile));
			LOGGER.debug("Merging " + srcDoclog + " into " + destDoclog);
			for (DoclogRecord doclogRecord : srcDoclog) {
				destDoclog.add(doclogRecord);
			}

			JAXBUtils.marshall(destDoclog,
					getDoclogLocation(context, destDoclog.getKey()));
			srcFile.delete();
			LOGGER.debug("Merged " + srcDoclog + " into " + destDoclog);

			/*
			 * Move files of the form [KEY].doclog.[num] to [KEY].doclog.[num]
			 */
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				File fingerprintFragment = new File(srcFile.getAbsolutePath()
						+ "." + i);
				if (!fingerprintFragment.exists())
					break;
				File idFragment = new File(destFile.getAbsolutePath() + "." + i);
				FileUtils.moveFile(fingerprintFragment, idFragment);
			}
		} finally {
			getWriteLock(destFile).unlock();
			getWriteLock(srcFile).unlock();
		}
	}

	/**
	 * Returns a {@link DoclogRecord} from a specified {@link Doclog}
	 * 
	 * @param key
	 *            the {@link Doclog}'s {@link ID} or {@link Fingerprint}
	 * @param index
	 *            which record to return; if negative the records are counted
	 *            from the most recent one (e.g. -2 would mean the second recent
	 *            one)
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	@GET
	@Path("{key}/{index}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public DoclogRecord getDoclogRecord(@Context ServletContext context,
			@PathParam("key") String key, @PathParam("index") int index)
			throws IOException, JAXBException {
		Object typedKey = ID.isValid(key) ? new ID(key) : new Fingerprint(key);
		Doclog doclog = getDoclog(context, typedKey);
		if (doclog == null)
			return null;
		if (index >= doclog.size() || index < -doclog.size())
			return null;
		if (index < 0)
			return doclog.get(doclog.size() + index);
		else
			return doclog.get(index);
	}

	/**
	 * Deleted a {@link DoclogRecord} from a specified {@link Doclog}
	 * 
	 * @param key
	 *            the {@link Doclog}'s {@link ID} or {@link Fingerprint}
	 * @param index
	 *            which record to delete; if negative the records are counted
	 *            from the most recent one (e.g. -2 would mean the second recent
	 *            one)
	 * @return true if successfully deleted
	 * @throws IOException
	 * @throws JAXBException
	 */
	@DELETE
	@Path("{key}/{index}")
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_XML,
			MediaType.APPLICATION_XML })
	public String deleteDoclogRecord(@Context ServletContext context,
			@PathParam("key") String key, @PathParam("index") int index)
			throws IOException {
		Object typedKey = ID.isValid(key) ? new ID(key) : new Fingerprint(key);

		try {
			typedKey = getDoclog(context, typedKey).getKey();
		} catch (NullPointerException e) {
			return Boolean.FALSE.toString();
		} catch (Exception e) {
			LOGGER.error("Can't read " + Doclog.class.getSimpleName() + " of "
					+ key, e);
			return Boolean.FALSE.toString();
		}

		File file = getDoclogLocation(context, typedKey);
		getWriteLock(file).lock();

		try {
			Doclog doclog = null;
			try {
				doclog = getDoclog(context, typedKey);
			} catch (JAXBException e) {
				LOGGER.error("Can't read " + Doclog.class.getSimpleName()
						+ " of " + key, e);
			}
			if (doclog == null)
				return Boolean.FALSE.toString();
			if (index >= doclog.size() || index < -doclog.size())
				return Boolean.FALSE.toString();
			if (index < 0)
				doclog.remove(doclog.size() + index);
			else
				doclog.remove(index);

			if (doclog.size() > 0) {
				try {
					JAXBUtils.marshall(doclog, file);
				} catch (JAXBException e) {
					getWriteLock(file).unlock();
					LOGGER.error(
							"Can't write "
									+ Doclog.class.getSimpleName()
									+ " of "
									+ key
									+ ".\nRemoved index: "
									+ index
									+ "\nContent:\n"
									+ FileUtils
											.readFileToString(getDoclogLocation(
													context, typedKey)), e);
				}
			} else {
				file.delete();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			getWriteLock(file).unlock();
		}

		return Boolean.TRUE.toString();
	}
}