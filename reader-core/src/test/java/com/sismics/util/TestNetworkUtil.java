package com.sismics.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test of the network utilities.
 * 
 * @author bgamard
 */
public class TestNetworkUtil {

    @Test
    @Ignore
    public void testUpnp() throws Exception {
        Assert.assertTrue(NetworkUtil.mapTcpPort(4040));
    }
}
