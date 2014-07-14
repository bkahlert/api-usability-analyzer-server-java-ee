package de.fu_berlin.imp.seqan.usability_analyzer.srv.rest;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class QueryFormMerger implements ContainerRequestFilter {

	@Override
	public ContainerRequest filter(ContainerRequest request) {
		MultivaluedMap<String, String> qParams = request.getQueryParameters();
		Form fParams = request.getFormParameters();
		for (String key : fParams.keySet()) {
			String value = fParams.get(key).get(0);
			qParams.add(key, value);
		}
		return request;
	}

}
