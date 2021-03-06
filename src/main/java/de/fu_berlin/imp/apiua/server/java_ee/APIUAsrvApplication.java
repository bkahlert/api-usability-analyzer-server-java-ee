package de.fu_berlin.imp.apiua.server.java_ee;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.fu_berlin.imp.apiua.server.java_ee.persistence.db.DoclogRecordContainer;
import de.fu_berlin.imp.apiua.server.java_ee.ui.DoclogRecordViewer;
import de.fu_berlin.imp.apiua.server.java_ee.ui.EntityViewer;

@SuppressWarnings("serial")
public class APIUAsrvApplication extends Application implements
		Property.ValueChangeListener {

	private DoclogRecordViewer doclogViewer;

	@Override
	public void init() {
		Window mainWindow = new Window(
				"API Usability Analyzer Server - Java EE");
		setMainWindow(mainWindow);
		getContext().addTransactionListener(new AppData(this));
		// AppData.initLocale(getLocale(), APIUAsrvApplication.class.getName());

		HorizontalSplitPanel panel = new HorizontalSplitPanel();
		panel.setSplitPosition(30);
		panel.setSizeFull();

		VerticalLayout left = new VerticalLayout();
		EntityViewer entityViewer = new EntityViewer();
		entityViewer.setHeight("300px");
		left.addComponent(entityViewer);

		Button button = new Button("Hello");
		left.addComponent(button);
		panel.addComponent(left);

		VerticalLayout right = new VerticalLayout();
		doclogViewer = new DoclogRecordViewer(this);
		doclogViewer.setContainerDataSource(getDoclogRecordContainer());
		right.addComponent(doclogViewer);
		panel.addComponent(right);

		/*
		 * TODO Load JavaScript only if necessary <script
		 * src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"
		 * ></script> <script>window.jQuery || document.write('<script
		 * src="js/libs/jquery-1.7.1.min.js"><\/script>')</script>
		 */

		mainWindow.setContent(panel);
		// mainWindow
		// .executeJavaScript("var s=document.createElement(\"script\");s.type=\"text/javascript\";s.src=\""
		// + this.getURL()
		// + "static/js/APIUAclt.js\";document.body.appendChild(s);");
		// mainWindow
		// .executeJavaScript("var s=document.createElement(\"script\");s.type=\"text/javascript\";s.src=\""
		// + this.getURL()
		// + "static/js/APIUAclt.js\";document.body.appendChild(s);");
	}

	public Container getDoclogRecordContainer() {
		return DoclogRecordContainer.create();
	}

	public void valueChange(ValueChangeEvent event) {
		Property property = event.getProperty();
		if (property == doclogViewer) {
			Item item = doclogViewer.getItem(doclogViewer.getValue());
			System.err.println(item);
		}
	}
}
