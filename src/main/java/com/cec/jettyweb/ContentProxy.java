package com.cec.jettyweb;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.jetty.proxy.ProxyServlet;


public class ContentProxy extends ProxyServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(ContentProxy.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
	    super.init(config);
	}

	@Override
	protected String rewriteTarget(HttpServletRequest clientRequest) {

            if(App.getProps() != null){

                return App.getProps().getProperty(AppSettings.proxyUrl);

            } else {

                LOGGER.error("No proxy endpoint configured");

                return null;
            }
        }

}
