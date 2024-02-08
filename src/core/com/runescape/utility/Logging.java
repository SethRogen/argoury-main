package com.runescape.utility;

import org.apache.log4j.Logger;

/**
 * @author Lazaro
 */
public class Logging {
    /**
     * Creates a new <code>Logger</code> object and names it based on the class
     * that this method was called.
     *
     * @return The <code>Logger</code> object
     */
    public static Logger log() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        Logger logger = Logger.getLogger(caller.getClassName());
        return logger;
    }
}
