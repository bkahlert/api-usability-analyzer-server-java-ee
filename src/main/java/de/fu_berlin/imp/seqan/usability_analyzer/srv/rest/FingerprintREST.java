package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.sun.jersey.api.json.JSONWithPadding;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.LocationUtils;

@Path("/fingerprint")
public class FingerprintREST {

	private static final Logger LOGGER = Logger
			.getLogger(FingerprintREST.class);

	@GET
	@Path("{fingerprint}")
	@Produces({ "application/x-javascript", "application/javascript",
			MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public JSONWithPadding getID(
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@QueryParam("callback") @DefaultValue("callback") final String callback,
			@PathParam("fingerprint") Fingerprint fingerprint)
			throws IOException {
		ID id = LocationUtils.getDoclogPersistence(context).getMapping(
				fingerprint);
		if (id != null) {
			return new JSONWithPadding(id, callback);
		} else {
			return new JSONWithPadding(new ERR("The "
					+ Fingerprint.class.getSimpleName() + " \"" + fingerprint
					+ "\" is not matched, yet."), callback);
		}
	}

	/**
	 * Associates a {@link Fingerprint}'s mapped {@link ID} with another
	 * {@link Fingerprint}
	 * 
	 * @param oldFingerprint
	 *            the {@link Fingerprint} the {@link ID} is read from
	 * @param newFingerprint
	 *            the new {@link Fingerprint}
	 * @return
	 * @throws IOException
	 */
	@GET
	@Path("{fingerprint1}/associate/{fingerprint2}")
	@Produces({ "application/x-javascript", "application/javascript",
			MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public JSONWithPadding associateFingerprint(
			@Context ServletContext context,
			@Context HttpServletResponse response,
			@QueryParam("callback") @DefaultValue("callback") final String callback,
			@PathParam("fingerprint1") Fingerprint oldFingerprint,
			@PathParam("fingerprint2") Fingerprint newFingerprint)
			throws IOException {
		LOGGER.info("Associating from " + oldFingerprint + " to "
				+ newFingerprint);
		try {
			LocationUtils.getDoclogPersistence(context).associateFingerprints(
					oldFingerprint, newFingerprint);
			LOGGER.info("Updated old " + Fingerprint.class.getSimpleName()
					+ " \"" + oldFingerprint + "\" to new "
					+ Fingerprint.class.getSimpleName() + " \""
					+ newFingerprint + "\"!");
			return new JSONWithPadding(new ACK(), callback);
		} catch (Throwable e) {
			String error = "Could not update the old "
					+ Fingerprint.class.getSimpleName() + " \""
					+ oldFingerprint + "\" to the new "
					+ Fingerprint.class.getSimpleName() + " \""
					+ newFingerprint + "\"!";
			LOGGER.error(error, e);
			return new JSONWithPadding(new ERR(error), callback);
		}
	}
}