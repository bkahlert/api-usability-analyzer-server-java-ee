package de.fu_berlin.imp.seqan.usability_analyzer.srv.model.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeAdapter extends XmlAdapter<String, DateTime> {

	public String marshal(DateTime v) {
		if (v == null)
			return "";
		String line = ISODateTimeFormat.dateTime().withOffsetParsed().print(v);
		line = line.replaceFirst("(\\d{2}:\\d{2}:\\d{2}.\\d+?)Z", "$1+00:00");
		return line;
	}

	public DateTime unmarshal(String v) {
		if (v == null || v.isEmpty())
			return null;
		v = v.replaceFirst("(\\d{2}:\\d{2}:\\d{2}.\\d+?)Z", "$1+00:00");
		return ISODateTimeFormat.dateTime().withOffsetParsed().parseDateTime(v);
	}

}