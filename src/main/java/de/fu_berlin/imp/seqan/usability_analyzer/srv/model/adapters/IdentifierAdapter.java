package de.fu_berlin.imp.seqan.usability_analyzer.srv.model.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.IIdentifier;

public class IdentifierAdapter extends XmlAdapter<String, IIdentifier> {

	public String marshal(IIdentifier v) throws Exception {
		if (v == null)
			return "";
		return v.toString();
	}

	public IIdentifier unmarshal(String v) throws Exception {
		if (v == null || v.isEmpty()
				|| (!ID.isValid(v) && !Fingerprint.isValid(v)))
			return null;
		return ID.isValid(v) ? new ID(v) : new Fingerprint(v);
	}

}