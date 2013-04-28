package com.sismics.reader.agent.deployer;

/**
 * Callback interface implemented by classes that wants to be notified when the state
 * of the server deployment changes.
 *
 * @author jtremeaux
 */
public interface DeploymentStatusListener {

    /**
     * Method invoked when information about the server deployment is available
     * 
     * @param deploymentStatus Deployment status
     */
    void notifyDeploymentStatus(DeploymentStatus deploymentStatus);
}
