package com.sismics.util;

import com.sismics.util.adblock.Subscription;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class TestAdblockUtil {

    @Test
    public void test() throws Exception {
        AdblockUtil adblock = new AdblockUtil();
        adblock.start();
        Subscription subscription = adblock.offerSubscription();
        adblock.setSubscription(subscription);
    }
}
