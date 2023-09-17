package com.ziotic.link.network;

import com.ziotic.network.Frame;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class FrameWaiter {
    public static final FrameWaiter INSTANCE = new FrameWaiter();

    private Map<String, Frame> nameFrameMap = new HashMap<String, Frame>();

    public Frame waitForFrame(String userName) throws InterruptedException {
        while (true) {
            synchronized (nameFrameMap) {
                nameFrameMap.wait();
                Frame frame = nameFrameMap.get(userName);
                if (frame != null) {
                    return frame;
                }
            }
        }
    }

    public void submitFrame(String userName, Frame frame) {
        synchronized (nameFrameMap) {
            nameFrameMap.put(userName, frame);
            nameFrameMap.notifyAll();
        }
    }
}
