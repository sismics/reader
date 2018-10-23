package com.sismics.util;

import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Test of the network utilities.
 * 
 * @author bgamard
 */
public class TestNetworkUtil {

    @Test
    @Ignore
    public void testUpnp() throws Exception {
        assertTrue(NetworkUtil.mapTcpPort(4040));
    }
}
