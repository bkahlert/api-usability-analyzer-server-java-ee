package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.sun.jersey.api.json.JSONWithPadding;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Rectangle;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.adapters.DateTimeAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.DoclogPersistence.DoclogPersistenceException;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.MappingDoclogPersistence;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.LocationUtils;

@Path("/doclog")
public class DoclogREST {

	private static final Logger LOGGER = Logger.getLogger(DoclogREST.class);

	/*
	 * FIXME: This method respect the added /full since the same url needs to
	 * handle the @GET createDoclogRecord method. Explicitly setting the accept
	 * header of ajax requests is not working on all browsers.
	 */
	@GET
	@Path("{key}/test")
	@Produces({ "application/x-javascript", "application/javascript",
			MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public static JSONWithPadding getDoclog(
			@Context ServletContext context,
			@Context HttpServletResponse response,
			@QueryParam("callback") @DefaultValue("callback") final String callback,
			@PathParam("key") String key) throws IOException, JAXBException {
		IIdentifier identifier = IdentifierFactory.createFrom(key);
		MappingDoclogPersistence persistence = LocationUtils
				.getDoclogPersistence(context);
		try {
			return new JSONWithPadding(persistence.getDoclog(identifier),
					callback);
		} catch (DoclogPersistenceException e) {
			String error = "Could not get " + Doclog.class.getSimpleName()
					+ " for " + IIdentifier.class.getSimpleName() + " "
					+ identifier;
			LOGGER.error(error, e);
			return new JSONWithPadding(new ERR(error), callback);
		}
	}

	/**
	 * Creates a {@link DoclogRecord}.
	 * <p>
	 * Since cross-domains are not supported by all browsers we also accept the
	 * {@link DoclogRecord} creation by GET calls.
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
	@Produces({ "application/x-javascript", "application/javascript",
			MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public static JSONWithPadding createDoclogRecordGET(
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@PathParam("key") String key,
			@QueryParam("callback") @DefaultValue("callback") final String callback,
			@QueryParam("id") final String id,
			@QueryParam("url") final String url, @QueryParam("ip") String ip,
			@QueryParam("proxyIp") String proxyIp,
			@QueryParam("event") final String event,
			@QueryParam("action") DoclogAction action,
			@QueryParam("actionParameter") String actionParameter,
			@QueryParam("dateTime") final String dateTime,
			@QueryParam("bounds.x") final Integer boundsX,
			@QueryParam("bounds.y") final Integer boundsY,
			@QueryParam("bounds.width") final Integer boundsWidth,
			@QueryParam("bounds.height") final Integer boundsHeight)
			throws IOException, JAXBException {
		JSONWithPadding jsonWithPadding = createDoclogRecord(context, request,
				response, callback, key, id, url, ip, proxyIp, event, action,
				actionParameter, dateTime, boundsX, boundsY, boundsWidth,
				boundsHeight);
		return jsonWithPadding;
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
	@Produces({ "application/x-javascript", "application/javascript",
			MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public static JSONWithPadding createDoclogRecord(
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@QueryParam("callback") @DefaultValue("callback") final String callback,
			@PathParam("key") final String key,
			@QueryParam("id") final String idString,
			@QueryParam("url") final String url, @QueryParam("ip") String ip,
			@QueryParam("proxyIp") String proxyIp,
			@QueryParam("event") final String event,
			@QueryParam("action") DoclogAction action,
			@QueryParam("actionParameter") String actionParameter,
			@QueryParam("dateTime") final String dateTime,
			@QueryParam("bounds.x") final Integer boundsX,
			@QueryParam("bounds.y") final Integer boundsY,
			@QueryParam("bounds.width") final Integer boundsWidth,
			@QueryParam("bounds.height") final Integer boundsHeight)
			throws IOException {
		LOGGER.info("--------------------------------------------");
		LOGGER.info("Creating " + DoclogRecord.class.getSimpleName());
		final IIdentifier identifier = IdentifierFactory.createFrom(key);
		final ID id;
		if (ID.isValid(idString)) {
			id = new ID(idString);
		} else {
			id = null;
			if (idString != null) {
				LOGGER.error("Invalid " + ID.class + " submitted: " + idString);
			}
		}
		if (identifier == null || url == null
				|| (event == null && action == null) || dateTime == null
				|| boundsX == null || boundsY == null || boundsWidth == null
				|| boundsHeight == null) {
			String error = "Missing parameter on "
					+ Doclog.class.getSimpleName() + " creation:\n\tip=" + ip
					+ "\n\tid=" + id + "\n\turl=" + url + "\n\tevent=" + event
					+ "\n\taction=" + action + "\n\tdateTime=" + dateTime
					+ "\n\tbounds.x=" + boundsX + "\n\tbounds.y=" + boundsY
					+ "\n\tbounds.width=" + boundsWidth + "\n\tbounds.height="
					+ boundsHeight;
			LOGGER.error(error);
			return new JSONWithPadding(new ERR(error), callback);
		}
		LOGGER.info("... in " + Doclog.class.getSimpleName() + " " + identifier
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
			final String[] action_ = event.split("-", 2);
			if (actionParameter == null) {
				actionParameter = action_.length > 1 && !action_[1].isEmpty() ? action_[1]
						: null;
			}
			if (action == null) {
				try {
					action = DoclogAction.valueOf(action_[0].toUpperCase());
				} catch (IllegalArgumentException e) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							LOGGER.error("Unknown "
									+ DoclogAction.class.getSimpleName() + " "
									+ action_[0] + " provided");
						}
					}).start();
					action = DoclogAction.UNKNOWN;
					actionParameter = actionParameter != null ? action_[0]
							+ "-" + actionParameter : action_[0];
				}
			}
		}

		DoclogRecord doclogRecord = new DoclogRecord(url, ip, proxyIp, action,
				actionParameter, new DateTimeAdapter().unmarshal(dateTime),
				new Rectangle(boundsX, boundsY, boundsWidth, boundsHeight));

		MappingDoclogPersistence persistence = LocationUtils
				.getDoclogPersistence(context);
		persistence.write(identifier, id, doclogRecord);
		LOGGER.info("Created " + DoclogRecord.class.getSimpleName() + " in "
				+ Doclog.class.getSimpleName() + " " + identifier
				+ ((id != null) ? " (ID: " + id + ")" : ""));
		return new JSONWithPadding(new ACK(), callback);
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
	@Produces({ "application/x-javascript", "application/javascript",
			MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public static JSONWithPadding getDoclogRecord(
			@Context ServletContext context,
			@Context HttpServletResponse response,
			@QueryParam("callback") @DefaultValue("callback") final String callback,
			@PathParam("key") final String key, @PathParam("index") int index)
			throws IOException, JAXBException {
		final IIdentifier identifier = IdentifierFactory.createFrom(key);

		MappingDoclogPersistence persistence = LocationUtils
				.getDoclogPersistence(context);
		try {
			DoclogRecord doclogRecord = persistence
					.getRecord(identifier, index);
			if (doclogRecord != null) {
				return new JSONWithPadding(doclogRecord, callback);
			} else {
				return new JSONWithPadding(new GenericEntity<DoclogRecord[]>(
						new DoclogRecord[0], DoclogRecord[].class), callback);
			}
		} catch (DoclogPersistenceException e) {
			String error = "Error retrieving the " + index + "-th "
					+ DoclogRecord.class.getSimpleName() + " for "
					+ IIdentifier.class.getSimpleName() + " " + identifier;
			LOGGER.error(error, e);
			return new JSONWithPadding(new ERR(error), callback);
		}
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
	@Produces({ "application/x-javascript", "application/javascript",
			MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public static JSONWithPadding deleteDoclogRecord(
			@Context ServletContext context,
			@Context HttpServletResponse response,
			@QueryParam("callback") @DefaultValue("callback") final String callback,
			@PathParam("key") String key, @PathParam("index") int index)
			throws IOException {
		final IIdentifier identifier = IdentifierFactory.createFrom(key);

		MappingDoclogPersistence persistence = LocationUtils
				.getDoclogPersistence(context);
		try {
			boolean success = persistence.deleteRecord(identifier, index);
			if (success) {
				return new JSONWithPadding(new ACK(), callback);
			} else {
				String error = "Could not delete " + index + "-th "
						+ DoclogRecord.class + " from " + identifier
						+ " because the element does not exist.";
				LOGGER.warn(error);
				return new JSONWithPadding(new ERR(error), callback);
			}
		} catch (DoclogPersistenceException e) {
			String error = "Could not delete " + index + "-th "
					+ DoclogRecord.class + " from " + identifier;
			LOGGER.error(error, e);
			return new JSONWithPadding(new ERR(error), callback);
		}
	}
}