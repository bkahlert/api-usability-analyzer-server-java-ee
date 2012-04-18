package de.fu_berlin.imp.seqan.usability_analyzer.srv.model.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeAdapter extends XmlAdapter<String, DateTime> {

	public String marshal(DateTime v) {
		if (v == null)
			return "";
		return ISODateTimeFormat.dateTime().withOffsetParsed().print(v);
	}

	public DateTime unmarshal(String v) {
		if (v == null || v.isEmpty())
			return null;
		return ISODateTimeFormat.dateTime().withOffsetParsed().parseDateTime(v);
	}

}