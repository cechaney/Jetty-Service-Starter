package com.cec.jettyweb.proxy;

import com.cec.jettyweb.App;
import com.cec.jettyweb.AppSettings;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.jetty.proxy.ProxyServlet;


public class ContentProxyServlet extends ProxyServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "ContentProxyServlet";
	public static final String PATH = "/proxy";

	private static final Logger LOGGER = Logger.getLogger(ContentProxyServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
	    super.init(config);
	}

	@Override
	protected String rewriteTarget(HttpServletRequest clientRequest) {

            if(App.getProps() != null){

                return App.getProps().getProperty(AppSettings.PROXY_URL);

            } else {

                LOGGER.error("No proxy endpoint configured");

                return null;
            }
        }

}
