package com.sismics.reader.agent.deployer;

import java.util.Date;

/**
 * Deployment status.
 *
 * @author jtremeaux
 */
public class DeploymentStatus {
    /**
     * Server lifecycle states.
     */
    public enum ServerState {
        STOPPED,
        
        STARTING,
        
        STARTED,

        STOPPING,
    }
    
    private final ServerState serverState;
    
    /**
     * Startup time.
     */
    private final Date startTime;
    
    /**
     * Server URL.
     */
    private final String url;
    
    /**
     * Memory used.
     */
    private final int memoryUsed;
    
    /**
     * Error that occured during server deployment.
     */
    private final String errorMessage;

    /**
     * Constructor of DeploymentStatus.
     * 
     * @param serverState Server lifecycle state
     * @param startTime Startup time
     * @param url Server URL
     * @param memoryUsed Memory used
     * @param errorMessage Error that occured during server deployment 
     */
    public DeploymentStatus(ServerState serverState, Date startTime, String url, int memoryUsed, String errorMessage) {
        this.serverState = serverState;
        this.startTime = startTime;
        this.url = url;
        this.memoryUsed = memoryUsed;
        this.errorMessage = errorMessage;
    }

    /**
     * Getter of serverState.
     *
     * @return serverState
     */
    public ServerState getServerState() {
        return serverState;
    }

    /**
     * Getter of startTime.
     *
     * @return startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Getter of url.
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Getter of memoryUsed.
     *
     * @return memoryUsed
     */
    public int getMemoryUsed() {
        return memoryUsed;
    }

    /**
     * Getter of errorMessage.
     *
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
