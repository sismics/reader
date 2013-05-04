package com.sismics.util;

import java.net.InetAddress;
import java.text.MessageFormat;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network utilities.
 * 
 * @author bgamard
 */
public class NetworkUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(NetworkUtil.class);
    
    /**
     * Attempt to map a TCP port from the gateway with UPnP.
     * 
     * @param port Port number
     * @return True if port mapping is successful.
     */
    public static boolean mapTcpPort(int port) {
        try {
            // Discover gateway
            GatewayDiscover discover = new GatewayDiscover();
            discover.discover();
            GatewayDevice device = discover.getValidGateway();
            
            if (device != null) {
                // Gateway found
                PortMappingEntry portMapping = new PortMappingEntry();
                
                if (device.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
                    // Mapping already registered
                    log.info(MessageFormat.format("Port TCP {0} already mapped", port));
                    return true;
                } else {
                    // Adding mapping
                    InetAddress localAddress = device.getLocalAddress();
                    return device.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", "Sismics Reader");
                }
            }
        } catch (Exception e) {
            log.info("Unable to map TCP port", e);
        }
        
        return false;
    }
}
