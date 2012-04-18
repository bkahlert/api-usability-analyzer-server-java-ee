package de.fu_berlin.imp.seqan.usability_analyzer.srv.servlets;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.servlets.DefaultServlet;

/**
 * Makes static content available.
 * <p>
 * The request <code>http://host/app/path/file.xml</code> will make this servlet
 * return the file <code>[webapps]/app/file.xml</code>.
 * <p>
 * In contrast to the {@link DefaultServlet} this servlet ignores the servlet
 * mapping's path portion. In the above mentioned example {@link DefaultServlet}
 * would return <code>[webapps]/app/path/file.xml</code>.
 * 
 * @author bkahlert
 * 
 */
public class StaticServlet extends DefaultServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getRelativePath(HttpServletRequest request) {
		// e.g. https://host/app/path, whereas path is the prefix
		int prefixLength = request.getServletPath().length();
		return super.getRelativePath(request).substring(prefixLength);
	}

}
