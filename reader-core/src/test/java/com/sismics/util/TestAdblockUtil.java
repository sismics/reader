package com.sismics.util;

import org.junit.Test;

import com.sismics.util.adblock.Subscription;

public class TestAdblockUtil {

    @Test
    public void test() throws Exception {
        AdblockUtil adblock = new AdblockUtil();
        adblock.start();
        Subscription subscription = adblock.offerSubscription();
        adblock.setSubscription(subscription);
    }
}
