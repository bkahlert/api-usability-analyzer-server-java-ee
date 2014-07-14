package de.fu_berlin.imp.apiua.server.java_ee.model.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.fu_berlin.imp.apiua.server.java_ee.model.ID;

public class IDAdapter extends XmlAdapter<String, ID> {

	public String marshal(ID v) throws Exception {
		if (v == null)
			return "";
		return v.toString();
	}

	public ID unmarshal(String v) throws Exception {
		if (v == null || v.isEmpty())
			return null;
		return new ID(v);
	}

}