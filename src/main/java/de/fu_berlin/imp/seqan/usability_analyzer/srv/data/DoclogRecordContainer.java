package de.fu_berlin.imp.seqan.usability_analyzer.srv.data;import java.io.Serializable;import org.apache.log4j.Logger;import com.vaadin.data.util.BeanItemContainer;import de.fu_berlin.imp.seqan.usability_analyzer.srv.AppData;import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Doclog;import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;import de.fu_berlin.imp.seqan.usability_analyzer.srv.rest.DoclogManager;public class DoclogRecordContainer extends BeanItemContainer<DoclogRecord>		implements Serializable {	private static final long serialVersionUID = -5893691842287912978L;	private static final Logger LOGGER = Logger			.getLogger(DoclogRecordContainer.class);	public static DoclogRecordContainer create() {		DoclogRecordContainer container;		try {			container = new DoclogRecordContainer();			for (Doclog doclog : DoclogManager.getDoclogs(AppData					.getServletContext())) {				container.addAll(doclog);			}			return container;		} catch (Exception e) {			LOGGER.error("Error getting " + Doclog.class.getSimpleName() + "s",					e);		}		return null;	}	public DoclogRecordContainer() throws InstantiationException,			IllegalAccessException {		super(DoclogRecord.class);	}}