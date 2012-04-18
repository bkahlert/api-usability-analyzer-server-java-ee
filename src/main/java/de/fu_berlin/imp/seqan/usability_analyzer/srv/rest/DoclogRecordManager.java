package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecordTest;

@Path("/doclogrecord")
public class DoclogRecordManager {

	@GET
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public DoclogRecord getDoclogRecord() {
		return DoclogRecordTest.DOCLOG_RECORD;
	}

}