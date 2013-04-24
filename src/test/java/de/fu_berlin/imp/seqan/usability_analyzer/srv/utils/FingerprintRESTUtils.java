package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.seleniumhq.jetty7.util.ajax.JSON;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;

public class FingerprintRESTUtils {

	public static WebResource getFingerprintREST(URL suaSrv)
			throws URISyntaxException {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		return client.resource(new URI(suaSrv.toExternalForm()
				+ "/rest/fingerprint"));
	}

	/**
	 * @param fingerprint
	 * @return
	 */
	public static ID getID(URL suaSrv, Fingerprint fingerprint)
			throws Exception {
		WebResource fingerprintREST = getFingerprintREST(suaSrv);

		@SuppressWarnings("unchecked")
		Map<String, String> response = (Map<String, String>) JSON
				.parse(fingerprintREST.path(fingerprint.toString())
						.accept(MediaType.APPLICATION_JSON).get(String.class));
		if (response.containsKey("identifier")) {
			return new ID(response.get("identifier"));
		} else {
			return null;
		}
	}

	/**
	 * @param fingerprint
	 * @return
	 */
	public static ID getID(Fingerprint fingerprint) throws Exception {
		return getID(TestConfiguration.getDefaultSUAsrvURL(), fingerprint);
	}

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
	public static boolean associate(URL suaSrv, Fingerprint fingerprint1,
			Object fingerprint2) throws Exception {
		WebResource fingerprintREST = getFingerprintREST(suaSrv);

		@SuppressWarnings("unchecked")
		Map<String, String> response = (Map<String, String>) JSON
				.parse(fingerprintREST.path(fingerprint1.toString())
						.path("associate").path(fingerprint2.toString())
						.accept(MediaType.APPLICATION_JSON).get(String.class));
		return response.keySet().size() == 1
				&& response.containsKey("timestamp");
	}

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
	public static boolean associate(Fingerprint fingerprint1,
			Fingerprint fingerprint2) throws Exception {
		return associate(TestConfiguration.getDefaultSUAsrvURL(), fingerprint1,
				fingerprint2);
	}
}
