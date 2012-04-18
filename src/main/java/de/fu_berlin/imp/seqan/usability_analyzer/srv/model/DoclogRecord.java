package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
@XmlType(propOrder = { "url", "ip", "proxyIp", "action", "actionParameter",
		"dateTime", "bounds" })
public class DoclogRecord implements Comparable<DoclogRecord>, Serializable {

	private static final long serialVersionUID = -7179575943640177616L;
	public static final DateTimeFormatter ISO8601_simple = DateTimeFormat
			.forPattern("yyyy-MM-dd'T'HH-mm-ss").withOffsetParsed();
	public static final DateTimeFormatter ISO8601 = ISODateTimeFormat
			.dateTime().withOffsetParsed();

	private String url;
	private String ip;
	private String proxyIp;
	private DoclogAction action;
	private String actionParameter;
	private DateTime dateTime;
	private Rectangle bounds;

	public DoclogRecord() {

	}

	public DoclogRecord(String url, String ip, String proxyIp,
			DoclogAction action, String actionParameter, DateTime date,
			Rectangle bounds) {
		super();
		this.url = url;
		this.ip = ip;
		this.proxyIp = proxyIp;
		this.action = action;
		this.actionParameter = actionParameter;
		this.dateTime = date;
		this.bounds = bounds;
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
		return this.getClass().getSimpleName() + ": " + this.url;
	}

}
