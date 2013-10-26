package com.sismics.util.adblock;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class Helper {

    private static final Logger log = LoggerFactory.getLogger(Helper.class);
    
    private JSEngine js;
    
    public Helper(JSEngine js) {
        this.js = js;
    }
    
    public void log(Object msg) {
        log.debug(msg.toString());
    }
    
    public void load(String path) throws Exception {
        URL url = Resources.getResource("adblock" + File.separator + "js" + File.separator + path);
        js.evaluate(Resources.toString(url, Charsets.UTF_8));
    }
    
    public boolean fileExists(String stringPath) {
        Path path = Paths.get(stringPath);
        return Files.exists(path);
    }
    
    public boolean canAutoupdate() {
        return true;
    }
    
    public TimerTask timerTask(final Runnable runnable) {
        return new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }
}
