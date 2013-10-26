package com.sismics.util.adblock;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JSEngine {

    private ScriptEngine engine;
    
    public JSEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("JavaScript");
    }
    
    public Object evaluate(String script) throws ScriptException {
        return engine.eval(script);
    }
    
    public void put(String key, Object value) {
        engine.put(key, value);
    }
}
