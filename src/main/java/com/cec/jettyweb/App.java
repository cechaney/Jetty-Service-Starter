package com.cec.jettyweb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class);

    private static Properties props = null;
    private static String resourceDir = null;

    public static void main(String[] args) throws Exception {

        try {

            configureLogging();
            configureProperties();
            configureResourcesDir();

            Server server = new Server(createThreadPool());

            ServerConnector connector = createConnector(server);
            server.addConnector(connector);

            ServletContextHandler context = createContext();
            HandlerList handlers = createHandlers(context);

            server.setHandler(handlers);

            addServlets(context);

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

    private static void addServlets(ServletContextHandler context) {
        context.addServlet(createRootServlet(), "/");
        context.addServlet(createProxyServlet(), "/hfapp");
    }

    private static HandlerList createHandlers(ServletContextHandler context) {

        HandlerList handlers = new HandlerList();

        GzipHandler gzipHandler = createGzipHandler(context);
        RewriteHandler rewriteHandler = createRewriteHandler(context);

        handlers.setHandlers(
                new Handler[]{
                    gzipHandler,
                    rewriteHandler,});

        return handlers;
    }

    private static void configureProperties() throws IOException {

        InputStream inputStream = App.class.getClassLoader().getResourceAsStream(AppSettings.propertiesFile);

        props = new Properties();
        props.load(inputStream);

    }

    private static void configureLogging() throws Exception {
        //Set the Jetty log to the log4j logger config via slf4j
        Log.setLog(new Slf4jLog());
    }

    private static void configureResourcesDir() {

        resourceDir = App.class.
                getClassLoader().
                getResource(props.getProperty(AppSettings.resourceDir)).
                toExternalForm();
    }

    private static QueuedThreadPool createThreadPool() {

        QueuedThreadPool threadPool = new QueuedThreadPool();

        threadPool.setMaxThreads(Integer.parseInt(props.getProperty(AppSettings.minThreads)));
        threadPool.setMinThreads(Integer.parseInt(props.getProperty(AppSettings.maxThreads)));

        return threadPool;
    }

    private static ServerConnector createConnector(Server server) {

        ServerConnector connector = new ServerConnector(server);

        connector.setPort(Integer.parseInt(props.get(AppSettings.port).toString()));
        connector.setIdleTimeout(Integer.parseInt(props.get(AppSettings.idleTimeout).toString()));

        return connector;
    }

    private static ServletContextHandler createContext() {

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

        context.setContextPath(props.getProperty(AppSettings.contextPath));

        return context;
    }

    private static ServletHolder createRootServlet() {

        ServletHolder rootServlet = new ServletHolder("default", DefaultServlet.class);

        rootServlet.setInitParameter("resourceBase", resourceDir);

        return rootServlet;
    }

    private static ServletHolder createProxyServlet() {

        ServletHolder proxyServlet = new ServletHolder(ContentProxy.class);

        return proxyServlet;
    }

    private static GzipHandler createGzipHandler(ServletContextHandler context) {

        GzipHandler gzipHandler = new GzipHandler();

        gzipHandler.setHandler(context);

        return gzipHandler;
    }

    private static RewriteHandler createRewriteHandler(ServletContextHandler context) {

        RewriteHandler rewriteHandler = new RewriteHandler();

        rewriteHandler.setRewriteRequestURI(true);
        rewriteHandler.setRewritePathInfo(false);
        rewriteHandler.setOriginalPathAttribute("requestedPath");

        RedirectPatternRule redirect = new RedirectPatternRule();

        redirect.setPattern("/");
        redirect.setLocation(props.get(AppSettings.contextPath).toString());
        rewriteHandler.addRule(redirect);

        rewriteHandler.setHandler(context);

        return rewriteHandler;
    }

}
