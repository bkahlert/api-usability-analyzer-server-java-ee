package de.fu_berlin.imp.apiua.server.java_ee.utils;

import org.joda.time.DateTime;

import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogAction;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogRecord;
import de.fu_berlin.imp.apiua.server.java_ee.model.Rectangle;

public class DoclogRecordBuilder {

	private String url;
	private String ip;
	private String proxyIp;
	private DoclogAction action;
	private String actionParameter;
	private DateTime dateTime;
	private Rectangle bounds;

	public DoclogRecordBuilder() {
		this.url = null;
		this.ip = null;
		this.proxyIp = null;
		this.action = null;
		this.actionParameter = null;
		this.dateTime = null;
		this.bounds = null;
	}

	public DoclogRecordBuilder(DoclogRecord doclogRecord) {
		this.url = doclogRecord.getUrl();
		this.ip = doclogRecord.getIp();
		this.proxyIp = doclogRecord.getProxyIp();
		this.action = doclogRecord.getAction();
		this.actionParameter = doclogRecord.getActionParameter();
		this.dateTime = doclogRecord.getDateTime();
		this.bounds = doclogRecord.getBounds();
	}

	public DoclogRecordBuilder setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getUrl() {
		return this.url;
	}

	public DoclogRecordBuilder setIp(String ip) {
		this.ip = ip;
		return this;
	}

	public DoclogRecordBuilder setProxyIp(String proxyIp) {
		this.proxyIp = proxyIp;
		return this;
	}

	public DoclogRecordBuilder setAction(DoclogAction action) {
		this.action = action;
		return this;
	}

	public DoclogRecordBuilder setActionParameter(String actionParameter) {
		this.actionParameter = actionParameter;
		return this;
	}

	public DoclogRecordBuilder setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
		return this;
	}

	public DoclogRecordBuilder setBounds(Rectangle bounds) {
		this.bounds = bounds;
		return this;
	}

	public DoclogRecordBuilder setX(int x) {
		this.bounds = new Rectangle(x, this.bounds.getY(),
				this.bounds.getWidth(), this.bounds.getHeight());
		return this;
	}

	public DoclogRecordBuilder setY(int y) {
		this.bounds = new Rectangle(this.bounds.getX(), y,
				this.bounds.getWidth(), this.bounds.getHeight());
		return this;
	}

	public DoclogRecordBuilder setWidth(int width) {
		this.bounds = new Rectangle(this.bounds.getX(), this.bounds.getY(),
				width, this.bounds.getHeight());
		return this;
	}

	public int getWidth() {
		return this.bounds.getWidth();
	}

	public DoclogRecordBuilder setScrollPosition(int x, int y) {
		this.setX(x);
		this.setY(y);
		return this;
	}

	public DoclogRecordBuilder setHeight(int height) {
		this.bounds = new Rectangle(this.bounds.getX(), this.bounds.getY(),
				this.bounds.getWidth(), height);
		return this;
	}

	public int getHeight() {
		return this.bounds.getHeight();
	}

	public DoclogRecord create() {
		return new DoclogRecord(url, ip, proxyIp, action, actionParameter,
				dateTime, bounds);
	}

}
