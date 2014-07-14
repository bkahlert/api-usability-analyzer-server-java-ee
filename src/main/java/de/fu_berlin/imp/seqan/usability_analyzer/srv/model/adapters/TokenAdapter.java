package de.fu_berlin.imp.seqan.usability_analyzer.srv.model.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Token;

public class TokenAdapter extends XmlAdapter<String, Token> {

	public String marshal(Token v) throws Exception {
		if (v == null)
			return "";
		return v.toString();
	}

	public Token unmarshal(String v) throws Exception {
		if (v == null || v.isEmpty())
			return null;
		return new Token(v);
	}

}