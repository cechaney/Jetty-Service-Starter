package com.cec.jettyweb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.cec.jettyweb.proxy.ContentProxyServlet;
import com.cec.jettyweb.web.HelloServlet;

public class App {

    private static Logger LOGGER = Logger.getLogger(App.class);

    private static Properties props = null;

    public static void main(String[] args) throws Exception {

        try {

        	Log.setLog(new Slf4jLog());

            try{InputStream inputStream = App.class.getClassLoader().getResourceAsStream(AppSettings.PROPERTIES_FILE);

	            props = new Properties();
	            props.load(inputStream);

            } catch(IOException ioe){
            	LOGGER.error("Unable to load properties. Startup aborted", ioe);
            	return;
            }

            ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            servletContext.setContextPath(props.getProperty(AppSettings.CONTEXT_PATH));
            servletContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
            
            Server server = new Server(createThreadPool());
            server.addConnector(createConnector(server));            
            
            GzipHandler gzipHandler = new GzipHandler();
            
            server.setHandler(gzipHandler);

            HandlerList handlers = new HandlerList();

            handlers.setHandlers(
                    new Handler[]{
                        servletContext,
                        new DefaultHandler()
                    });

            gzipHandler.setHandler(handlers);
            
            addServlets(servletContext);

            server.start();
            server.join();

        } catch (Exception ex) {
            LOGGER.error("Startup FAILED!", ex);
        }

    }

    public static Properties getProps() {
        return props;
    }

    public static void setProps(Properties props) {
        App.props = props;
    }
    
    private static QueuedThreadPool createThreadPool() {

        QueuedThreadPool threadPool = new QueuedThreadPool();

        threadPool.setMaxThreads(Integer.parseInt(props.getProperty(AppSettings.MIN_THREADS)));
        threadPool.setMinThreads(Integer.parseInt(props.getProperty(AppSettings.MAX_THREADS)));

        return threadPool;
    }

    private static ServerConnector createConnector(Server server) {

        ServerConnector connector = new ServerConnector(server);

        connector.setPort(Integer.parseInt(props.get(AppSettings.PORT).toString()));
        connector.setIdleTimeout(Integer.parseInt(props.get(AppSettings.IDLE_TIMEOUT).toString()));

        return connector;
    }

    private static void addServlets(ServletContextHandler context) {
    	
    	context.addServlet(createDefaultServletHolder(), "/");

        context.addServlet(
        		new ServletHolder(ContentProxyServlet.NAME, ContentProxyServlet.class),
        		ContentProxyServlet.PATH);
        
        context.addServlet(
        		new ServletHolder(HelloServlet.NAME, HelloServlet.class),
        		HelloServlet.PATH);
    }
    
    private static ServletHolder createDefaultServletHolder(){
    	
    	ServletHolder defaultHolder = null;
    	
        String resourceBase = App.class.
                getClassLoader().
                getResource(props.getProperty(AppSettings.RESOURCE_DIR)).
                toExternalForm();    	
    	
        defaultHolder = new ServletHolder("Default", DefaultServlet.class);

        defaultHolder.setInitParameter("resourceBase", resourceBase);
        defaultHolder.setInitParameter("dirAllowed", "false");
        defaultHolder.setInitParameter("gzip", "false");    	
    	
    	return defaultHolder;
    	
    }

//    private static RewriteHandler createRewriteHandler(ServletContextHandler context) {
//
//        RewriteHandler rewriteHandler = new RewriteHandler();
//
//        rewriteHandler.setRewriteRequestURI(true);
//        rewriteHandler.setRewritePathInfo(false);
//        rewriteHandler.setOriginalPathAttribute("requestedPath");
//
//        RedirectPatternRule rootRedirect = new RedirectPatternRule();
//        rootRedirect.setPattern(ROOT_PATH);
//        rootRedirect.setLocation(props.get(AppSettings.contextPath).toString() + SBOTD_PATH);
//        rewriteHandler.addRule(rootRedirect);
//
//        rewriteHandler.setHandler(context);
//
//        return rewriteHandler;
//    }
}
