package de.fu_berlin.imp.seqan.usability_analyzer.srv;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;

/**
 * Holds data for one user session. src:
 * https://vaadin.com/book/-/page/advanced.global.html
 */
@SuppressWarnings("serial")
public class AppData implements TransactionListener, Serializable {
	private ResourceBundle bundle;
	private Locale locale; // Current locale
	private String userData; // Trivial data model for the user

	private Application app; // For distinguishing between apps

	private static ThreadLocal<AppData> instance = new ThreadLocal<AppData>();

	public AppData(Application app) {
		this.app = app;

		// It's usable from now on in the current request
		instance.set(this);
	}

	public void transactionStart(Application application, Object transactionData) {
		// Set this data instance of this application
		// as the one active in the current thread.
		if (this.app == application)
			instance.set(this);
	}

	public void transactionEnd(Application application, Object transactionData) {
		// Clear the reference to avoid potential problems
		if (this.app == application)
			instance.set(null);
	}

	public static void initLocale(Locale locale, String bundleName) {
		instance.get().locale = locale;
		instance.get().bundle = ResourceBundle.getBundle(bundleName, locale);
	}

	public static Locale getLocale() {
		return instance.get().locale;
	}

	public static String getMessage(String msgId) {
		return instance.get().bundle.getString(msgId);
	}

	public static ServletContext getServletContext() {
		return ((WebApplicationContext) instance.get().app.getContext())
				.getHttpSession().getServletContext();
	}

	public static String getAppProperty(String name) {
		return instance.get().app.getProperty(name);
	}

	public static String getUserData() {
		return instance.get().userData;
	}

	public static void setUserData(String userData) {
		instance.get().userData = userData;
	}
}