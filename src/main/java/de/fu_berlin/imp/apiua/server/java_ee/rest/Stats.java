package de.fu_berlin.imp.apiua.server.java_ee.rest;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.apiua.server.java_ee.model.Statistics;

@Path("/stats")
public class Stats {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Stats.class);

	@GET
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public static Statistics getDoclogs(@Context ServletContext context,
			@Context HttpServletResponse response) throws IOException {
		return new Statistics(context);
	}

}