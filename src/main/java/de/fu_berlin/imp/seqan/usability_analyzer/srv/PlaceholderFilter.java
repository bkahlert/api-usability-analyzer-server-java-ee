package de.fu_berlin.imp.seqan.usability_analyzer.srv;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.servlets.BufferedServletResponse;

/**
 * Replaces placeholders in the SUA client javascript according to the requested
 * url.
 * <p>
 * If a user asks for
 * <code>http://dalak.imp.fu-berlin.de/static/js/SUAclt.js</code> the returned
 * javascript communicates with <code>http://dalak.imp.fu-berlin.de/</code>.
 * 
 * @author bkahlert
 * 
 */
public class PlaceholderFilter implements Filter {

	private ServletContext context;

	public void init(FilterConfig filterConfig) throws ServletException {
		context = filterConfig.getServletContext();
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		String uri = request.getRequestURI();
		if ((uri.contains("register.html") || uri.contains("SUAclt") || uri
				.contains("SUAsrv"))
				&& !(uri.endsWith("png") || uri.endsWith("css"))) {
			// process request as usual but buffer the output
			OutputStream out = response.getOutputStream();
			BufferedServletResponse wrapper = new BufferedServletResponse(
					response);
			filterChain.doFilter(request, wrapper);

			response.setContentType(response.getContentType()
					+ "; charset=UTF-8");
			response.setHeader("Cache-Control",
					"no-cache, no-store, must-revalidate");
			response.setDateHeader("Expires", 0L);

			// the wrapper now contains the normally shipped data
			// let's modify it
			String host = request.getServerName();
			int port = request.getServerPort();

			String data = new String(wrapper.getData());
			data = data.replace("${HOST}", host
					+ ((port == 80 || port == 443) ? "" : ":" + port));
			data = data.replace("${CONTEXT_PATH}", context.getContextPath());
			data = data.replace("${productionMode}",
					context.getInitParameter("productionMode"));

			// send modified data
			byte[] bytes = data.getBytes();
			response.setContentLength(bytes.length);
			out.write(bytes);

			out.close();
		} else {
			filterChain.doFilter(request, response);
		}
	}

	public void destroy() {
	}

}