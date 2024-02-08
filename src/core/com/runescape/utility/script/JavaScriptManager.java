package com.runescape.utility.script;

import org.apache.log4j.Logger;
import org.mozilla.javascript.*;

import com.runescape.Static;
import com.runescape.utility.Logging;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class JavaScriptManager extends ImporterTopLevel {
    private static final Logger logger = Logging.log();

    private static final long serialVersionUID = 7548502465572769157L;

    private Map<String, Boolean> loadedScriptMap = new HashMap<String, Boolean>();
    private Map<String, Script> scriptMap = new HashMap<String, Script>();

    private File scriptDir;

    public JavaScriptManager() {
        Context ctx = Context.enter();
        ctx.initStandardObjects(this);
        initStandardObjects(ctx, true);
        String[] names = {"logger", "invoke", "trace", "random", "allocateBytes", "castByte", "castInt", "castString"};
        defineFunctionProperties(names, JavaScriptManager.class, ScriptableObject.DONTENUM);
        scriptDir = new File(Static.conf.getString("script_dir"));
        loopDir(ctx, scriptDir);
        logger.info("Loaded " + scriptMap.size() + " script(s)");
        Context.exit();
    }

    public Object call(String call, boolean loadAutomatically, Object... args) {
        Context ctx = Context.enter();
        Object results;
        int dotIndex = call.indexOf('.');
        String scriptName = call.substring(0, (dotIndex != -1 ? dotIndex : call.length()));
        String funcName = null;
        if (dotIndex != -1) {
            funcName = call.substring(dotIndex + 1, call.length());
        }
        Script script = scriptMap.get(scriptName);
        if (script == null) {
            throw new IllegalArgumentException("Script is not loaded : " + scriptName + "!");
        }
        if (funcName == null) {
            if (args.length > 0) {
                throw new RuntimeException("Arguments are not supported when calling scripts from a static context!");
            }
            results = script.exec(ctx, this);
            synchronized (loadedScriptMap) {
                loadedScriptMap.put(scriptName, true);
            }
        } else {
            Boolean loaded;
            synchronized (loadedScriptMap) {
                loaded = loadedScriptMap.get(scriptName);
                if (loaded == null) {
                    loaded = Boolean.FALSE;
                }
                if (!loaded && loadAutomatically) {
                    script.exec(ctx, this);
                    loadedScriptMap.put(scriptName, true);
                }
            }
            Scriptable scriptObj = (Scriptable) script;
            Object obj = this.get(funcName, this);
            if (!(obj instanceof Function)) {
                throw new RuntimeException("Requested function is not a function or does not exist");
            }
            Function func = (Function) obj;
            Object[] jsArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Object) {
                    arg = Context.javaToJS(arg, this);
                }
                jsArgs[i] = arg;
            }
            results = func.call(ctx, this, scriptObj, jsArgs);
        }
        results = Context.jsToJava(results, Object.class);
        Context.exit();
        return results;
    }

    public Object call(String call, Object... args) {
        return call(call, true, args);
    }

    public Object invoke(String call) {
        return call(call);
    }

    @Override
    public String getClassName() {
        return "global";
    }

    private void loopDir(Context ctx, File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (f.isFile() && f.getName().endsWith(".js")) {
                    String name = f.getAbsolutePath().substring(f.getAbsolutePath().indexOf(scriptDir.getAbsolutePath()) + scriptDir.getAbsolutePath().length() + 1, f.getAbsolutePath().lastIndexOf(".js")).replace(File.separatorChar, '/');
                    try {
                        Script script = ctx.compileReader(new FileReader(f), name, 1, null);
                        Scriptable scriptObj = (Scriptable) script;
                        scriptObj.setParentScope(this);
                        scriptMap.put(name, script);
                    } catch (IOException e) {
                        logger.error("Error loading script : " + name, e);
                    }
                } else if (f.isDirectory()) {
                    loopDir(ctx, f);
                }
            }
        }
    }

    public Logger logger() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[9];
        Logger logger = Logger.getLogger(caller.getFileName());
        return logger;
    }

    public void trace(Object msg, Object o) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[9];
        Logger logger = Logger.getLogger(caller.getFileName());
        logger.error(msg, new Throwable(o.toString()));
    }

    public int random(int startRange, int endRange) {
        return Static.random.nextInt((endRange - startRange) + 1) + startRange;
    }

    public byte[] allocateBytes(int length) {
        return new byte[length];
    }

    public byte castByte(Object o) {
        return ((Number) o).byteValue();
    }

    public int castInt(Object o) {
        return ((Number) o).intValue();
    }

    public String castString(Object o) {
        return o.toString();
    }
}
