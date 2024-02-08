package com.runescape.utility;

import java.util.concurrent.ThreadFactory;

/**
 * @author Lazaro
 */
public class ManipulativeThreadFactory implements ThreadFactory {
    private String name;
    private int priority;
    private int count = 1;

    public ManipulativeThreadFactory(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(new StringBuilder(name).append("-").append(count++).toString());
        thread.setPriority(priority);
        return thread;
    }
}
