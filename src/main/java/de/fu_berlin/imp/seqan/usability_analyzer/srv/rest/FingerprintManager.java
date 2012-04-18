package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.FinterprintAlreadyMappedException;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;

@Path("/fingerprint")
public class FingerprintManager {

	private static final Logger LOGGER = Logger
			.getLogger(FingerprintManager.class);

	/**
	 * Associates a {@link Fingerprint}'s mapped {@link ID} with another
	 * {@link Fingerprint}
	 * 
	 * @param fingerprint1
	 *            the {@link Fingerprint} the {@link ID} is read from
	 * @param fingerprint2
	 *            the new {@link Fingerprint}
	 * @return
	 */
	@GET
	@Path("{fingerprint1}/associate/{fingerprint2}")
	@Produces({ "text/javascript", MediaType.TEXT_PLAIN })
	public String associateFingerprint(@Context ServletContext context,
			@PathParam("fingerprint1") Fingerprint fingerprint1,
			@PathParam("fingerprint2") Fingerprint fingerprint2) {
		ID id = null;
		try {
			DoclogManager.getWriteLock(
					DoclogManager.getDoclogLocation(context, fingerprint1))
					.lock();
			DoclogManager.getWriteLock(
					DoclogManager.getDoclogLocation(context, fingerprint2))
					.lock();
			id = DoclogManager.getMapping(context, fingerprint1);
			if (id != null) {
				DoclogManager.getWriteLock(
						DoclogManager.getDoclogLocation(context, id)).lock();
				try {
					DoclogManager.addMapping(context, fingerprint2, id);
					return Boolean.TRUE.toString();
				} catch (FinterprintAlreadyMappedException e) {
					LOGGER.error(
							"An attempt to associate "
									+ Fingerprint.class.getSimpleName()
									+ " "
									+ fingerprint2
									+ " ("
									+ ID.class.getSimpleName()
									+ ": "
									+ id
									+ ") with new "
									+ Fingerprint.class.getSimpleName()
									+ " "
									+ fingerprint2
									+ " was made the latter is already mapped to "
									+ ID.class.getSimpleName() + " "
									+ e.getOldID(), e);
				}
			} else {
				DoclogManager.mergeDoclogs(context, fingerprint1, fingerprint2);
				return Boolean.TRUE.toString();
			}
		} catch (JAXBException e) {
			LOGGER.error(e);
		} catch (IOException e) {
			LOGGER.error(e);
		} finally {
			try {
				DoclogManager.getWriteLock(
						DoclogManager.getDoclogLocation(context, fingerprint1))
						.unlock();
				DoclogManager.getWriteLock(
						DoclogManager.getDoclogLocation(context, fingerprint2))
						.unlock();
				if (id != null) {
					DoclogManager.getWriteLock(
							DoclogManager.getDoclogLocation(context, id))
							.unlock();
				}
			} catch (IOException e) {
				LOGGER.fatal("Could not unlock");
			}
		}
		return Boolean.FALSE.toString();
	}
}