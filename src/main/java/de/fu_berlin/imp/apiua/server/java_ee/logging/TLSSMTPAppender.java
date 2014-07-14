package de.fu_berlin.imp.apiua.server.java_ee.logging;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.log4j.net.SMTPAppender;

public class TLSSMTPAppender extends SMTPAppender {
	protected Boolean isTLS;

	public void setTLS(boolean isTLS) {
		this.isTLS = isTLS;
	}

	@Override
	protected Session createSession() {
		Properties props = null;
		try {
			props = new Properties(System.getProperties());
		} catch (SecurityException ex) {
			props = new Properties();
		}
		if (getSMTPHost() != null) {
			props.put("mail.smtp.host", getSMTPHost());
		}
		Authenticator auth = null;
		if (getSMTPUsername() != null && getSMTPPassword() != null) {
			props.put("mail.smtp.auth", "true");
			auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(getSMTPUsername(),
							getSMTPPassword());
				}
			};
		}
		if (isTLS != null && isTLS) {
			props.put("mail.smtp.starttls.enable", "true");
		}
		Session session = Session.getInstance(props, auth);
		if (getSMTPDebug()) {
			session.setDebug(getSMTPDebug());
		}
		return session;
	}
}
