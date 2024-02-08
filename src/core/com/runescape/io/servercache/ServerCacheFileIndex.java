package com.runescape.io.servercache;

/**
 * @author Lazaro
 */
public class ServerCacheFileIndex {
    private String name;
    private int position;
    private int length;

    public ServerCacheFileIndex(String name, int position, int length) {
        this.name = name;
        this.position = position;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public int getPosition() {
        return position;
    }
}
