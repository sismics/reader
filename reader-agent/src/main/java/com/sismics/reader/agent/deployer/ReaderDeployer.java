package com.sismics.reader.agent.deployer;

import java.net.BindException;
import java.util.Date;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

import com.sismics.reader.agent.WindowsAgent;
import com.sismics.reader.agent.deployer.DeploymentStatus.ServerState;
import com.sismics.reader.agent.model.Setting;
import com.sismics.util.MessageUtil;

/**
 * Reader standalone deployer with an embedded Jetty. 
 *
 * @author jtremeaux
 */
public class ReaderDeployer implements LifeCycle.Listener {

    /**
     * WAR file to deploy.
     */
    private static final String READER_WAR = "reader.war";
    
    /**
     * Startup time.
     */
    private Date startTime;
    
    /**
     * Jetty server.
     */
    private Server server;

    /**
     * Windows agent.
     */
    private WindowsAgent windowsAgent;
    
    /**
     * Exception occuring during server startup.
     */
    private Exception exception;
    
    /**
     * Current server state.
     */
    private ServerState serverState;
    
    /**
     * Constructor of ReaderDeployer.
     * 
     * @param setting Settings
     */
    public ReaderDeployer(WindowsAgent windowsAgent) {
        this.windowsAgent = windowsAgent;
    }
    
    /**
     * Start the server.
     */
    public void start() {
        System.setProperty("reader.home", System.getenv("APPDATA") + "\\Sismics\\Reader");
        
        startTime = new Date();
        try {
            lifeCycleStarting(null);

            server = new Server();
            SelectChannelConnector connector = new SelectChannelConnector();
            final Setting setting = windowsAgent.getSetting();
            connector.setHost(setting.getHost());
            connector.setPort(setting.getPort());
            server.addConnector(connector);

            WebAppContext webapp = new WebAppContext();
            webapp.setContextPath(setting.getContextPath());
            webapp.setWar(READER_WAR);

            // Start the server
            server.addLifeCycleListener(this);
            server.setHandler(webapp);
            server.start();

            System.out.println("Reader running on: " + getUrl());
        } catch (Exception e) {
            e.printStackTrace(System.err);
            exception = e;
        }
    }
    
    /**
     * Stop the server.
     */
    public void stop() {
        if (server == null) {
            return;
        }
        
        try {
            lifeCycleStopping(null);
            
            server.stop();
            server = null;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Get a human readable error message from the server startup.
     * 
     * @return Error message
     */
    public String getErrorMessage() {
        if (exception == null) {
            return null;
        }
        if (exception instanceof BindException) {
            return MessageUtil.getMessage("agent.deployer.error.bind");
        }

        return exception.toString();
    }

    /**
     * Get the memory used.
     * 
     * @return Memory used
     */
    public int getMemoryUsed() {
        long freeBytes = Runtime.getRuntime().freeMemory();
        long totalBytes = Runtime.getRuntime().totalMemory();
        long usedBytes = totalBytes - freeBytes;
        
        return (int) Math.round(usedBytes / 1024.0 / 1024.0);
    }

    /**
     * Get the deployment URL.
     * 
     * @return URL
     */
    public String getUrl() {
        final Setting setting = windowsAgent.getSetting();
        StringBuilder sb = new StringBuilder("http://");
        sb.append(Setting.DEFAULT_HOST.equals(setting.getHost()) ? "localhost" : setting.getHost());
        if (setting.getPort() != 80) {
            sb.append(":");
            sb.append(setting.getPort());
        }
        sb.append(setting.getContextPath());
        return sb.toString();
    }

    /**
     * Get the current deployment status.
     * 
     * @return Deployment status
     */
    public DeploymentStatus getDeploymentStatus() {
        return new DeploymentStatus(serverState, startTime, getUrl(), getMemoryUsed(), getErrorMessage());
    }

    @Override
    public void lifeCycleStopping(LifeCycle event) {
        serverState = ServerState.STOPPING;
        windowsAgent.notifyDeploymentInfo();
    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
        serverState = ServerState.STOPPED;
        windowsAgent.notifyDeploymentInfo();
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {
        serverState = ServerState.STARTING;
        windowsAgent.notifyDeploymentInfo();
    }

    @Override
    public void lifeCycleStarted(LifeCycle event) {
        serverState = ServerState.STARTED;
        windowsAgent.notifyDeploymentInfo();
    }

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        serverState = ServerState.STOPPED;
        windowsAgent.notifyDeploymentInfo();
    }
}
