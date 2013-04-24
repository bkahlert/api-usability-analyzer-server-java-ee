package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.URIHelper;

@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
@XmlType(propOrder = { "url", "ip", "proxyIp", "action", "actionParameter",
		"dateTime", "bounds" })
public class DoclogRecord implements Comparable<DoclogRecord>, Serializable {

	private static final long serialVersionUID = -7179575943640177616L;
	public static final DateTimeFormatter ISO8601_simple = DateTimeFormat
			.forPattern("yyyy-MM-dd'T'HH-mm-ssZ").withOffsetParsed();
	public static final DateTimeFormatter ISO8601 = ISODateTimeFormat
			.dateTime().withOffsetParsed();

	// [^\\t] selects everything but a tabulator
	// line #1: date + optional milliseconds + optional time zone
	public static final Pattern PATTERN = Pattern
			.compile("([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2})[-:]([\\d]{2})[-:]([\\d]{2})(\\.[\\d]{3})?(([\\+-][\\d]{2}):?([\\d]{2}))?"
					+ "\\t([^\\t]+?)(-([^\\t]+?))?" // action + param
					+ "\\t([^\\t]+)" // url
					+ "\\t([^\\t]+)\\t([^\\t]+)" // ip + proxy ip
					+ "\\t(-?\\d+)\\t(-?\\d+)\\t(\\d+)\\t(\\d+)"); // scroll x,y
																	// +
																	// window
																	// w,h

	private String url;
	private String ip;
	private String proxyIp;
	private DoclogAction action;
	private String actionParameter;
	private DateTime dateTime;
	private Rectangle bounds;

	@SuppressWarnings("unused")
	private DoclogRecord() {

	}

	public DoclogRecord(String url, String ip, String proxyIp,
			DoclogAction action, String actionParameter, DateTime dateTime,
			Rectangle bounds) {
		super();
		this.url = url != null ? cleanUrl(url) : null;
		this.ip = ip;
		if (proxyIp != null && proxyIp.equals("-"))
			this.proxyIp = null;
		else
			this.proxyIp = proxyIp;
		this.action = action;
		this.actionParameter = actionParameter;
		this.dateTime = dateTime;
		this.bounds = bounds;
	}

	public DoclogRecord(String line) {
		line = line.replaceFirst("(\\d{2}:\\d{2}:\\d{2}.\\d+?)Z", "$1+00:00");
		Matcher matcher = PATTERN.matcher(line);
		if (matcher.find()) {
			url = cleanUrl(matcher.group(14));
			if (url == null)
				throw new IllegalArgumentException("The url is invalid");
			ip = matcher.group(15);
			proxyIp = matcher.group(16);
			if (proxyIp != null && proxyIp.equals("-"))
				proxyIp = null;

			action = DoclogAction.getByString(matcher.group(11));
			actionParameter = unescapeActionParameter(matcher.group(13));

			dateTime = ISODateTimeFormat.dateTime().withOffsetParsed()
					.parseDateTime(line.split("\t")[0]);

			bounds = new Rectangle(Integer.parseInt(matcher.group(17)),
					Integer.parseInt(matcher.group(18)),
					Integer.parseInt(matcher.group(19)),
					Integer.parseInt(matcher.group(20)));
		} else {
			throw new IllegalArgumentException(
					"The doclog line did not match to the expected format:\n"
							+ line);
		}
	}

	protected static String cleanUrl(String url) {
		try {
			String noAngleBrackets = url.replace("<", "%3C")
					.replace(">", "%3E");
			String noWhitespaces = noAngleBrackets.replace(" ", "%20");
			String onlyOneSharp = noWhitespaces.replaceFirst("#", "^^^")
					.replaceAll("#", "%23").replaceFirst("\\^\\^\\^", "#");
			String noId = new URIHelper(onlyOneSharp).removeParameter("id")
					.toString();
			return noId;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getProxyIp() {
		return proxyIp;
	}

	public void setProxyIp(String proxyIp) {
		this.proxyIp = proxyIp;
	}

	public DoclogAction getAction() {
		return action;
	}

	public void setAction(DoclogAction action) {
		this.action = action;
	}

	public String getActionParameter() {
		return actionParameter;
	}

	public void setActionParameter(String actionParameter) {
		this.actionParameter = "".equals(actionParameter) ? null
				: actionParameter;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	public int compareTo(DoclogRecord doclogRecord) {
		if (this.getDateTime() == null) {
			if (doclogRecord.getDateTime() == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (doclogRecord.getDateTime() == null) {
			return 1;
		} else {
			return this.getDateTime().compareTo(doclogRecord.getDateTime());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result
				+ ((actionParameter == null) ? 0 : actionParameter.hashCode());
		result = prime * result + ((bounds == null) ? 0 : bounds.hashCode());
		result = prime * result
				+ ((dateTime == null) ? 0 : dateTime.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((proxyIp == null) ? 0 : proxyIp.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DoclogRecord other = (DoclogRecord) obj;
		if (action != other.action)
			return false;
		if (actionParameter == null) {
			if (other.actionParameter != null)
				return false;
		} else if (!actionParameter.equals(other.actionParameter))
			return false;
		if (bounds == null) {
			if (other.bounds != null)
				return false;
		} else if (!bounds.equals(other.bounds))
			return false;
		if (dateTime == null) {
			if (other.dateTime != null)
				return false;
		} else if (!dateTime.equals(other.dateTime))
			return false;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (proxyIp == null) {
			if (other.proxyIp != null)
				return false;
		} else if (!proxyIp.equals(other.proxyIp))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (dateTime != null)
			sb.append(ISODateTimeFormat.dateTime().withOffsetParsed()
					.print(dateTime));
		sb.append("\t");
		if (action != null) {
			sb.append(action.toString());
			if (actionParameter != null && !actionParameter.trim().isEmpty())
				sb.append("-" + escapeActionParameter(actionParameter));
		}
		sb.append("\t");
		if (url != null)
			sb.append(url.toString());
		sb.append("\t");
		if (ip != null)
			sb.append(ip.toString());
		sb.append("\t");
		if (proxyIp != null)
			sb.append(proxyIp.toString());
		else
			sb.append("-");
		sb.append("\t");
		if (bounds != null)
			sb.append(bounds.getX());
		sb.append("\t");
		if (bounds != null)
			sb.append(bounds.getY());
		sb.append("\t");
		if (bounds != null)
			sb.append(bounds.getWidth());
		sb.append("\t");
		if (bounds != null)
			sb.append(bounds.getHeight());
		return sb.toString();
	}

	public static String escapeActionParameter(String actionParameter) {
		if (actionParameter == null)
			return null;
		return actionParameter.replace("\n", "\\n").replace("\t", "\\t");
	}

	public static String unescapeActionParameter(String actionParameter) {
		if (actionParameter == null)
			return null;
		return actionParameter.replace("\\n", "\n").replace("\\t", "\t");
	}

}
